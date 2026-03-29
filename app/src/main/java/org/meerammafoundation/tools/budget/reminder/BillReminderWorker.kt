package org.meerammafoundation.tools.budget.reminder

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import org.meerammafoundation.tools.BuildConfig
import java.util.Calendar

class BillReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "BillReminderWorker"
        private const val DAY_MS = 24L * 60L * 60L * 1000L
        private const val NOTIFY_DAYS_BEFORE = 1
        private const val MAX_ITEMS_PER_CATEGORY = 3
        private const val COOLDOWN_OVERDUE = 6
        private const val COOLDOWN_DUE_TODAY = 6
        private const val COOLDOWN_DUE_TOMORROW = 12
        private const val COOLDOWN_UPCOMING = 24
    }

    override suspend fun doWork(): Result {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "🚀 WORKER STARTED - Checking bills for notifications...")
        }

        return try {
            // ✅ Fix: Pass database, not DAO
            val database = BillReminderDatabase.getDatabase(applicationContext)
            val repository = BillReminderRepository(database)

            if (!hasNotificationPermission()) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "❌ No notification permission, skipping work")
                }
                return Result.success()
            }

            val allBills = repository.getAllBills().first()

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "📊 Found ${allBills.size} bills total")
            }

            val currentTime = System.currentTimeMillis()
            val todayStart = getStartOfDay(currentTime)

            val billsToNotify = allBills.filter { bill ->
                val isUnpaid = !bill.isPaid
                val isNotSnoozed = bill.snoozedUntil == null || bill.snoozedUntil <= currentTime

                val dueDayStart = getStartOfDay(bill.dueDate)
                val daysUntilDue = ((dueDayStart - todayStart) / DAY_MS).toInt()

                val cooldownHours = when {
                    daysUntilDue < 0 -> COOLDOWN_OVERDUE
                    daysUntilDue == 0 -> COOLDOWN_DUE_TODAY
                    daysUntilDue == 1 -> COOLDOWN_DUE_TOMORROW
                    else -> COOLDOWN_UPCOMING
                }
                val cooldownMs = cooldownHours * 60L * 60L * 1000L

                val isNotOnCooldown = if (bill.lastNotifiedAt != null) {
                    val timeSinceLastNotify = currentTime - bill.lastNotifiedAt
                    timeSinceLastNotify >= cooldownMs
                } else {
                    true
                }

                val shouldNotifyByDays = when {
                    daysUntilDue < 0 -> true
                    daysUntilDue == 0 -> true
                    daysUntilDue == 1 -> true
                    daysUntilDue <= NOTIFY_DAYS_BEFORE -> true
                    else -> false
                }

                val shouldNotify = isUnpaid && isNotSnoozed && isNotOnCooldown && shouldNotifyByDays

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "  Bill: ${bill.name}, isPaid: ${bill.isPaid}, daysUntilDue: $daysUntilDue, cooldown: ${cooldownHours}h, shouldNotify: $shouldNotify")
                }
                shouldNotify
            }

            if (billsToNotify.isEmpty()) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "✅ No bills to notify")
                }
                return Result.success()
            }

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "🔔 Found ${billsToNotify.size} bills to notify")
            }

            val overdueBills = mutableListOf<BillReminder>()
            val dueTodayBills = mutableListOf<BillReminder>()
            val dueTomorrowBills = mutableListOf<BillReminder>()
            val upcomingBills = mutableListOf<BillReminder>()

            billsToNotify.forEach { bill ->
                val dueDayStart = getStartOfDay(bill.dueDate)
                val daysUntilDue = ((dueDayStart - todayStart) / DAY_MS).toInt()
                when {
                    daysUntilDue < 0 -> overdueBills.add(bill)
                    daysUntilDue == 0 -> dueTodayBills.add(bill)
                    daysUntilDue == 1 -> dueTomorrowBills.add(bill)
                    daysUntilDue <= NOTIFY_DAYS_BEFORE -> upcomingBills.add(bill)
                }
            }

            val notificationMessage = buildString {
                if (overdueBills.isNotEmpty()) {
                    val displayCount = minOf(overdueBills.size, MAX_ITEMS_PER_CATEGORY)
                    append("⚠️ OVERDUE (${overdueBills.size}):\n")
                    overdueBills.take(displayCount).forEach { bill ->
                        val dueDayStart = getStartOfDay(bill.dueDate)
                        val days = ((todayStart - dueDayStart) / DAY_MS).toInt()
                        append("  • ${bill.name}: ₹${String.format("%.2f", bill.amount)} (${days} days overdue)\n")
                    }
                    if (overdueBills.size > MAX_ITEMS_PER_CATEGORY) {
                        append("  • and ${overdueBills.size - MAX_ITEMS_PER_CATEGORY} more...\n")
                    }
                    append("\n")
                }

                if (dueTodayBills.isNotEmpty()) {
                    val displayCount = minOf(dueTodayBills.size, MAX_ITEMS_PER_CATEGORY)
                    append("🔔 DUE TODAY (${dueTodayBills.size}):\n")
                    dueTodayBills.take(displayCount).forEach { bill ->
                        append("  • ${bill.name}: ₹${String.format("%.2f", bill.amount)}\n")
                    }
                    if (dueTodayBills.size > MAX_ITEMS_PER_CATEGORY) {
                        append("  • and ${dueTodayBills.size - MAX_ITEMS_PER_CATEGORY} more...\n")
                    }
                    append("\n")
                }

                if (dueTomorrowBills.isNotEmpty()) {
                    val displayCount = minOf(dueTomorrowBills.size, MAX_ITEMS_PER_CATEGORY)
                    append("⏰ DUE TOMORROW (${dueTomorrowBills.size}):\n")
                    dueTomorrowBills.take(displayCount).forEach { bill ->
                        append("  • ${bill.name}: ₹${String.format("%.2f", bill.amount)}\n")
                    }
                    if (dueTomorrowBills.size > MAX_ITEMS_PER_CATEGORY) {
                        append("  • and ${dueTomorrowBills.size - MAX_ITEMS_PER_CATEGORY} more...\n")
                    }
                    append("\n")
                }

                if (upcomingBills.isNotEmpty()) {
                    val displayCount = minOf(upcomingBills.size, MAX_ITEMS_PER_CATEGORY)
                    append("📅 UPCOMING (${upcomingBills.size}):\n")
                    upcomingBills.take(displayCount).forEach { bill ->
                        val dueDayStart = getStartOfDay(bill.dueDate)
                        val days = ((dueDayStart - todayStart) / DAY_MS).toInt()
                        append("  • ${bill.name}: ₹${String.format("%.2f", bill.amount)} (in $days days)\n")
                    }
                    if (upcomingBills.size > MAX_ITEMS_PER_CATEGORY) {
                        append("  • and ${upcomingBills.size - MAX_ITEMS_PER_CATEGORY} more...\n")
                    }
                }
            }

            if (notificationMessage.isNotEmpty()) {
                val title = when {
                    overdueBills.isNotEmpty() -> "⚠️ ${overdueBills.size} Bill(s) Overdue!"
                    dueTodayBills.isNotEmpty() -> "🔔 ${dueTodayBills.size} Bill(s) Due Today!"
                    dueTomorrowBills.isNotEmpty() -> "⏰ ${dueTomorrowBills.size} Bill(s) Due Tomorrow!"
                    else -> "📅 ${upcomingBills.size} Upcoming Bill(s)"
                }

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "📢 SENDING NOTIFICATION: $title")
                }

                TestNotificationHelper.showCombinedNotification(
                    applicationContext,
                    title,
                    notificationMessage.trim(),
                    billsToNotify
                )

                val notifiedIds = billsToNotify.map { it.id }
                repository.updateLastNotifiedAtBatch(notifiedIds, currentTime)
            }

            Result.success()
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "❌ Error checking bills", e)
            }
            Result.retry()
        }
    }

    private fun getStartOfDay(time: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = time
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun hasNotificationPermission(): Boolean {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            return android.content.pm.PackageManager.PERMISSION_GRANTED ==
                    applicationContext.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
        }
        return true
    }
}