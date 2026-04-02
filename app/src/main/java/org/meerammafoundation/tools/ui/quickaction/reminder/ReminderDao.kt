package org.meerammafoundation.tools.ui.quickaction.reminder

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: Reminder): Long

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Query("DELETE FROM reminders WHERE id = :reminderId")
    suspend fun deleteReminderById(reminderId: Long)

    @Query("SELECT * FROM reminders ORDER BY due_date ASC")
    fun getAllReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE is_completed = 0 ORDER BY due_date ASC")
    fun getUncompletedReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE is_completed = 1 ORDER BY completed_date DESC")
    fun getCompletedReminders(): Flow<List<Reminder>>

    @Query("SELECT * FROM reminders WHERE id = :reminderId")
    fun getReminderById(reminderId: Long): Flow<Reminder?>

    @Query("""
        UPDATE reminders 
        SET is_completed = 1, 
            completed_date = :completedDate, 
            snoozed_until = NULL, 
            last_notified_at = NULL, 
            updated_at = :updatedAt 
        WHERE id = :reminderId
    """)
    suspend fun markAsCompleted(reminderId: Long, completedDate: Long, updatedAt: Long)

    @Query("UPDATE reminders SET is_completed = 0, completed_date = NULL, updated_at = :updatedAt WHERE id = :reminderId")
    suspend fun markAsUncompleted(reminderId: Long, updatedAt: Long)

    @Query("""
        UPDATE reminders 
        SET snoozed_until = :snoozedUntil, 
            last_notified_at = NULL, 
            updated_at = :updatedAt 
        WHERE id = :reminderId
    """)
    suspend fun snoozeReminder(reminderId: Long, snoozedUntil: Long, updatedAt: Long)

    @Query("UPDATE reminders SET last_notified_at = :lastNotifiedAt, updated_at = :updatedAt WHERE id IN (:reminderIds)")
    suspend fun updateLastNotifiedAtBatch(reminderIds: List<Long>, lastNotifiedAt: Long, updatedAt: Long)
}