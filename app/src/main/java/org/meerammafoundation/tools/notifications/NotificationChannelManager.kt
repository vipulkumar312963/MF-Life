package org.meerammafoundation.tools.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build

object NotificationChannelManager {

    const val CHANNEL_REMINDER = "reminder_channel"
    const val CHANNEL_GOAL = "goal_channel"
    const val CHANNEL_BIRTHDAY = "birthday_channel"
    const val CHANNEL_GENERAL = "general_channel"

    fun createAllChannels(context: Context) {
        createReminderChannel(context)
        createGoalChannel(context)
        createBirthdayChannel(context)
        createGeneralChannel(context)
    }

    private fun createReminderChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_REMINDER,
                "Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for your reminders"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(soundUri, audioAttributes)
            }
            getManager(context).createNotificationChannel(channel)
        }
    }

    private fun createGoalChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_GOAL,
                "Goals",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for your goals progress"
                enableVibration(true)
                setSound(soundUri, audioAttributes)
            }
            getManager(context).createNotificationChannel(channel)
        }
    }

    private fun createBirthdayChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_BIRTHDAY,
                "Birthdays",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Birthday reminders"
                enableVibration(true)
                setSound(soundUri, audioAttributes)
            }
            getManager(context).createNotificationChannel(channel)
        }
    }

    private fun createGeneralChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General app notifications"
            }
            getManager(context).createNotificationChannel(channel)
        }
    }

    private fun getManager(context: Context): NotificationManager {
        return context.getSystemService(NotificationManager::class.java)
    }

    fun getChannelId(type: NotificationType): String {
        return when (type) {
            NotificationType.REMINDER -> CHANNEL_REMINDER
            NotificationType.GOAL -> CHANNEL_GOAL
            NotificationType.BIRTHDAY -> CHANNEL_BIRTHDAY
            else -> CHANNEL_GENERAL
        }
    }
}