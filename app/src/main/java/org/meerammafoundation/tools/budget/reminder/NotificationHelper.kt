package org.meerammafoundation.tools.budget.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import org.meerammafoundation.tools.R

object NotificationHelper {

    private const val CHANNEL_ID = "bill_reminder_channel"
    private const val CHANNEL_NAME = "Bill Reminders"
    private const val CHANNEL_DESCRIPTION = "Notifications for upcoming and overdue bills"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(soundUri, audioAttributes)
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun showBillReminderNotification(
        context: Context,
        billId: Long,
        billName: String,
        amount: Double,
        dueDate: Long,
        daysUntilDue: Int
    ) {
        if (!hasNotificationPermission(context)) {
            return
        }

        val intent = Intent(context, BillReminderActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("bill_id", billId)
            putExtra("notification_action", "view")
        }

        // ✅ Use billId as requestCode (consistent with cancel)
        val pendingIntent = PendingIntent.getActivity(
            context,
            billId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // ✅ Use constant for action
        val markPaidIntent = Intent(context, NotificationReceiver::class.java).apply {
            action = NotificationReceiver.ACTION_MARK_AS_PAID
            putExtra("bill_id", billId)
            putExtra("bill_name", billName)
        }

        val markPaidPendingIntent = PendingIntent.getBroadcast(
            context,
            billId.toInt(),
            markPaidIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when {
            daysUntilDue < 0 -> "⚠️ Overdue Bill!"
            daysUntilDue == 0 -> "🔔 Bill Due Today!"
            else -> "📅 Upcoming Bill"
        }

        val message = when {
            daysUntilDue < 0 -> "$billName is overdue by ${-daysUntilDue} days! Amount: ₹${String.format("%.2f", amount)}"
            daysUntilDue == 0 -> "$billName is due today! Amount: ₹${String.format("%.2f", amount)}"
            else -> "$billName is due in $daysUntilDue days. Amount: ₹${String.format("%.2f", amount)}"
        }

        // ✅ Use custom app icon instead of android.R.drawable
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setLargeIcon(BitmapFactory.decodeResource(context.resources, R.mipmap.ic_launcher))
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_menu_edit,
                "Mark as Paid",
                markPaidPendingIntent
            )
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .setOnlyAlertOnce(true)
            .build()

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.notify(billId.toInt(), notification)
    }

    fun cancelNotification(context: Context, billId: Long) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.cancel(billId.toInt())
    }
}