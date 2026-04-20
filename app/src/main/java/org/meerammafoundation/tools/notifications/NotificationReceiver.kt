package org.meerammafoundation.tools.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.meerammafoundation.tools.ui.quickaction.reminder.ReminderDatabase
import org.meerammafoundation.tools.ui.quickaction.reminder.ReminderRepository

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MARK_AS_COMPLETED = "MARK_AS_COMPLETED"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        when (intent.action) {
            ACTION_MARK_AS_COMPLETED -> {
                val reminderId = intent.getLongExtra("reminder_id", -1)
                val reminderTitle = intent.getStringExtra("reminder_title") ?: "Reminder"

                if (reminderId != -1L) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val database = ReminderDatabase.Companion.getDatabase(
                                context.applicationContext
                            )
                            val repository = ReminderRepository(database)
                            repository.markAsCompleted(reminderId)

                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    "$reminderTitle marked as completed!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            // Cancel notification
                            NotificationHelper.cancelNotification(context, reminderId)
                        } finally {
                            pendingResult.finish()
                        }
                    }
                } else {
                    pendingResult.finish()
                }
            }
            else -> pendingResult.finish()
        }
    }
}