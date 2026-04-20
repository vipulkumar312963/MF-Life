package org.meerammafoundation.tools.notifications

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import org.meerammafoundation.tools.MainActivity
import org.meerammafoundation.tools.R

object NotificationManager {

    private const val NOTIFICATION_ID_OFFSET = 1000

    fun showNotification(
        context: Context,
        item: NotificationItem
    ) {
        val channelId = NotificationChannelManager.getChannelId(item.type)
        val notificationId = (NOTIFICATION_ID_OFFSET + item.id).toInt()

        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("load_fragment", when (item.sourceType) {
                "reminder" -> "reminder"
                "goal" -> "goals"
                else -> "main"
            })
            putExtra("source_id", item.sourceId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(item.title)
            .setContentText(item.message)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(getPriority(item.priority))
            .build()

        val manager = context.getSystemService(android.app.NotificationManager::class.java)
        manager.notify(notificationId, notification)
    }

    private fun getPriority(priority: NotificationPriority): Int {
        return when (priority) {
            NotificationPriority.HIGH -> NotificationCompat.PRIORITY_HIGH
            NotificationPriority.MEDIUM -> NotificationCompat.PRIORITY_DEFAULT
            NotificationPriority.LOW -> NotificationCompat.PRIORITY_LOW
        }
    }

    fun cancelNotification(context: Context, id: Long) {
        val manager = context.getSystemService(android.app.NotificationManager::class.java)
        manager.cancel((NOTIFICATION_ID_OFFSET + id).toInt())
    }

    fun cancelAllNotifications(context: Context) {
        val manager = context.getSystemService(android.app.NotificationManager::class.java)
        manager.cancelAll()
    }
}