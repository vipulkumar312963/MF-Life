package org.meerammafoundation.tools.ui.quickaction.reminder

import kotlinx.coroutines.flow.Flow

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
        recurrence: RecurrenceType,  // Keep recurrence parameter
        metadata: String? = null
    ): Long {
        val now = System.currentTimeMillis()
        val reminder = Reminder(
            title = title,
            description = description,
            dueDate = dueDate,
            reminderType = reminderType,
            priority = priority,
            recurrence = recurrence,  // Make sure Reminder class has this field
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
        dao.markAsCompleted(reminderId, now, now)
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
}