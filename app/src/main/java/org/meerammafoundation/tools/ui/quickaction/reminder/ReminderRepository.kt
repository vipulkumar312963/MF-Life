package org.meerammafoundation.tools.ui.quickaction.reminder

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class ReminderRepository(private val db: ReminderDatabase) {

    private val dao = db.reminderDao()

    fun getAllReminders(): Flow<List<Reminder>> = dao.getAllReminders()

    fun getUncompletedReminders(): Flow<List<Reminder>> = dao.getUncompletedReminders()

    fun getCompletedReminders(): Flow<List<Reminder>> = dao.getCompletedReminders()

    fun getReminderById(reminderId: Long): Flow<Reminder?> = dao.getReminderById(reminderId)

    suspend fun createReminder(
        title: String,
        description: String? = null,
        dueDate: Long,
        reminderType: ReminderType,
        priority: Priority,
        recurrence: RecurrenceType,
        metadata: String? = null
    ): Long {
        val now = System.currentTimeMillis()
        val reminder = Reminder(
            title = title,
            description = description,
            dueDate = dueDate,
            reminderType = reminderType,
            priority = priority,
            recurrence = recurrence,
            metadata = metadata,
            createdAt = now,
            updatedAt = now
        )
        return dao.insertReminder(reminder)
    }

    suspend fun updateReminder(reminder: Reminder) {
        val updatedReminder = reminder.copy(updatedAt = System.currentTimeMillis())
        dao.updateReminder(updatedReminder)
    }

    suspend fun deleteReminder(reminder: Reminder) {
        dao.deleteReminderById(reminder.id)
    }

    suspend fun markAsCompleted(reminderId: Long) {
        val now = System.currentTimeMillis()
        val reminder = getReminderById(reminderId).first()

        if (reminder != null) {
            // For daily reminders, create the next occurrence
            if (reminder.recurrence == RecurrenceType.DAILY) {
                val nextDueDate = getNextDueDate(reminder.dueDate, reminder.recurrence)

                // Create a new reminder for the next day
                val newReminder = Reminder(
                    title = reminder.title,
                    description = reminder.description,
                    dueDate = nextDueDate,
                    reminderType = reminder.reminderType,
                    priority = reminder.priority,
                    recurrence = reminder.recurrence,
                    metadata = reminder.metadata,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                dao.insertReminder(newReminder)
            }

            // Mark current as completed
            dao.markAsCompleted(reminderId, now, now)
        }
    }

    suspend fun markAsUncompleted(reminderId: Long) {
        dao.markAsUncompleted(reminderId, System.currentTimeMillis())
    }

    suspend fun snoozeReminder(reminderId: Long, days: Int) {
        val snoozedUntil = System.currentTimeMillis() + (days * 24L * 60L * 60L * 1000L)
        dao.snoozeReminder(reminderId, snoozedUntil, System.currentTimeMillis())
    }

    suspend fun updateLastNotifiedAtBatch(reminderIds: List<Long>, lastNotifiedAt: Long) {
        if (reminderIds.isNotEmpty()) {
            dao.updateLastNotifiedAtBatch(reminderIds, lastNotifiedAt, System.currentTimeMillis())
        }
    }

    // Helper function to calculate next due date based on recurrence
    private fun getNextDueDate(currentDueDate: Long, recurrence: RecurrenceType): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentDueDate
        }

        return when (recurrence) {
            RecurrenceType.DAILY -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                calendar.timeInMillis
            }
            RecurrenceType.MONTHLY -> {
                calendar.add(Calendar.MONTH, 1)
                calendar.timeInMillis
            }
            RecurrenceType.QUARTERLY -> {
                calendar.add(Calendar.MONTH, 3)
                calendar.timeInMillis
            }
            RecurrenceType.YEARLY -> {
                calendar.add(Calendar.YEAR, 1)
                calendar.timeInMillis
            }
            else -> currentDueDate
        }
    }
}