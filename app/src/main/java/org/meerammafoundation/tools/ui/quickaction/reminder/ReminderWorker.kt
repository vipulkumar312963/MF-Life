package org.meerammafoundation.tools.ui.quickaction.reminder

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import org.meerammafoundation.tools.BuildConfig
import java.util.Calendar

class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "ReminderWorker"
        private const val DAY_MS = 24L * 60L * 60L * 1000L
        private const val NOTIFY_DAYS_BEFORE = 1
        private const val MAX_ITEMS_PER_CATEGORY = 3
        private const val COOLDOWN_OVERDUE = 6
        private const val COOLDOWN_DUE_TODAY = 6
        private const val COOLDOWN_DUE_TOMORROW = 12
        private const val COOLDOWN_UPCOMING = 24
        private const val COOLDOWN_DAILY = 24  // Daily reminders: once per day
    }

    override suspend fun doWork(): Result {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "🚀 WORKER STARTED - Checking reminders for notifications...")
        }

        return try {
            val database = ReminderDatabase.getDatabase(applicationContext)
            val repository = ReminderRepository(database)

            if (!hasNotificationPermission()) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "❌ No notification permission, skipping work")
                }
                return Result.success()
            }

            val allReminders = repository.getAllReminders().first()

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "📊 Found ${allReminders.size} reminders total")
            }

            val currentTime = System.currentTimeMillis()
            val todayStart = getStartOfDay(currentTime)

            val remindersToNotify = allReminders.filter { reminder ->
                val isNotCompleted = !reminder.isCompleted
                val isNotSnoozed = reminder.snoozedUntil == null || reminder.snoozedUntil <= currentTime

                val dueDayStart = getStartOfDay(reminder.dueDate)
                val daysUntilDue = ((dueDayStart - todayStart) / DAY_MS).toInt()

                // Different cooldown for daily reminders
                val cooldownHours = when {
                    reminder.recurrence == RecurrenceType.DAILY -> COOLDOWN_DAILY
                    daysUntilDue < 0 -> COOLDOWN_OVERDUE
                    daysUntilDue == 0 -> COOLDOWN_DUE_TODAY
                    daysUntilDue == 1 -> COOLDOWN_DUE_TOMORROW
                    else -> COOLDOWN_UPCOMING
                }
                val cooldownMs = cooldownHours * 60L * 60L * 1000L

                val isNotOnCooldown = if (reminder.lastNotifiedAt != null) {
                    val timeSinceLastNotify = currentTime - reminder.lastNotifiedAt
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

                val shouldNotify = isNotCompleted && isNotSnoozed && isNotOnCooldown && shouldNotifyByDays

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "  Reminder: ${reminder.title}, recurrence: ${reminder.recurrence}, daysUntilDue: $daysUntilDue, shouldNotify: $shouldNotify")
                }
                shouldNotify
            }

            if (remindersToNotify.isEmpty()) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "✅ No reminders to notify")
                }
                return Result.success()
            }

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "🔔 Found ${remindersToNotify.size} reminders to notify")
            }

            val overdueReminders = mutableListOf<Reminder>()
            val dueTodayReminders = mutableListOf<Reminder>()
            val dueTomorrowReminders = mutableListOf<Reminder>()
            val upcomingReminders = mutableListOf<Reminder>()

            remindersToNotify.forEach { reminder ->
                val dueDayStart = getStartOfDay(reminder.dueDate)
                val daysUntilDue = ((dueDayStart - todayStart) / DAY_MS).toInt()
                when {
                    daysUntilDue < 0 -> overdueReminders.add(reminder)
                    daysUntilDue == 0 -> dueTodayReminders.add(reminder)
                    daysUntilDue == 1 -> dueTomorrowReminders.add(reminder)
                    daysUntilDue <= NOTIFY_DAYS_BEFORE -> upcomingReminders.add(reminder)
                }
            }

            val notificationMessage = buildString {
                if (overdueReminders.isNotEmpty()) {
                    val displayCount = minOf(overdueReminders.size, MAX_ITEMS_PER_CATEGORY)
                    append("⚠️ OVERDUE (${overdueReminders.size}):\n")
                    overdueReminders.take(displayCount).forEach { reminder ->
                        val dueDayStart = getStartOfDay(reminder.dueDate)
                        val days = ((todayStart - dueDayStart) / DAY_MS).toInt()
                        val details = getReminderDetails(reminder)
                        append("  • ${reminder.title}$details ($days days overdue)\n")
                    }
                    if (overdueReminders.size > MAX_ITEMS_PER_CATEGORY) {
                        append("  • and ${overdueReminders.size - MAX_ITEMS_PER_CATEGORY} more...\n")
                    }
                    append("\n")
                }

                if (dueTodayReminders.isNotEmpty()) {
                    val displayCount = minOf(dueTodayReminders.size, MAX_ITEMS_PER_CATEGORY)
                    append("🔔 DUE TODAY (${dueTodayReminders.size}):\n")
                    dueTodayReminders.take(displayCount).forEach { reminder ->
                        val details = getReminderDetails(reminder)
                        append("  • ${reminder.title}$details\n")
                    }
                    if (dueTodayReminders.size > MAX_ITEMS_PER_CATEGORY) {
                        append("  • and ${dueTodayReminders.size - MAX_ITEMS_PER_CATEGORY} more...\n")
                    }
                    append("\n")
                }

                if (dueTomorrowReminders.isNotEmpty()) {
                    val displayCount = minOf(dueTomorrowReminders.size, MAX_ITEMS_PER_CATEGORY)
                    append("⏰ DUE TOMORROW (${dueTomorrowReminders.size}):\n")
                    dueTomorrowReminders.take(displayCount).forEach { reminder ->
                        val details = getReminderDetails(reminder)
                        append("  • ${reminder.title}$details\n")
                    }
                    if (dueTomorrowReminders.size > MAX_ITEMS_PER_CATEGORY) {
                        append("  • and ${dueTomorrowReminders.size - MAX_ITEMS_PER_CATEGORY} more...\n")
                    }
                    append("\n")
                }

                if (upcomingReminders.isNotEmpty()) {
                    val displayCount = minOf(upcomingReminders.size, MAX_ITEMS_PER_CATEGORY)
                    append("📅 UPCOMING (${upcomingReminders.size}):\n")
                    upcomingReminders.take(displayCount).forEach { reminder ->
                        val dueDayStart = getStartOfDay(reminder.dueDate)
                        val days = ((dueDayStart - todayStart) / DAY_MS).toInt()
                        val details = getReminderDetails(reminder)
                        append("  • ${reminder.title}$details (in $days days)\n")
                    }
                    if (upcomingReminders.size > MAX_ITEMS_PER_CATEGORY) {
                        append("  • and ${upcomingReminders.size - MAX_ITEMS_PER_CATEGORY} more...\n")
                    }
                }
            }

            if (notificationMessage.isNotEmpty()) {
                val title = when {
                    overdueReminders.isNotEmpty() -> "⚠️ ${overdueReminders.size} Reminder(s) Overdue!"
                    dueTodayReminders.isNotEmpty() -> "🔔 ${dueTodayReminders.size} Reminder(s) Due Today!"
                    dueTomorrowReminders.isNotEmpty() -> "⏰ ${dueTomorrowReminders.size} Reminder(s) Due Tomorrow!"
                    else -> "📅 ${upcomingReminders.size} Upcoming Reminder(s)"
                }

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "📢 SENDING NOTIFICATION: $title")
                }

                TestNotificationHelper.showCombinedNotification(
                    applicationContext,
                    title,
                    notificationMessage.trim(),
                    remindersToNotify
                )

                val notifiedIds = remindersToNotify.map { it.id }
                repository.updateLastNotifiedAtBatch(notifiedIds, currentTime)
            }

            Result.success()
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "❌ Error checking reminders", e)
            }
            Result.retry()
        }
    }

    private fun getReminderDetails(reminder: Reminder): String {
        return when (reminder.reminderType) {
            ReminderType.BILL -> {
                try {
                    val metadata = Converters().jsonToBillMetadata(reminder.metadata ?: return "")
                    ": ₹${String.format("%.2f", metadata.amount)}"
                } catch (e: Exception) {
                    ""
                }
            }
            ReminderType.TASK -> {
                if (reminder.priority == Priority.HIGH) " (High Priority)" else ""
            }
            ReminderType.EVENT -> {
                reminder.description?.let { ": $it" } ?: ""
            }
            else -> ""
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return PackageManager.PERMISSION_GRANTED ==
                    applicationContext.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
        }
        return true
    }
}