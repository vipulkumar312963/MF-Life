package org.meerammafoundation.tools.budget.reminder

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BillReminderDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: BillReminder): Long

    @Update
    suspend fun updateBill(bill: BillReminder)

    // ✅ Fixed: Use @Query, not @Delete
    @Query("DELETE FROM bill_reminders WHERE id = :billId")
    suspend fun deleteBillById(billId: Long)

    @Query("SELECT * FROM bill_reminders ORDER BY due_date ASC")
    fun getAllBills(): Flow<List<BillReminder>>

    @Query("SELECT * FROM bill_reminders WHERE is_paid = 0 ORDER BY due_date ASC")
    fun getUnpaidBills(): Flow<List<BillReminder>>

    @Query("SELECT * FROM bill_reminders WHERE is_paid = 1 ORDER BY paid_date DESC")
    fun getPaidBills(): Flow<List<BillReminder>>

    @Query("SELECT * FROM bill_reminders WHERE id = :billId")
    fun getBillById(billId: Long): Flow<BillReminder?>

    @Query("""
        UPDATE bill_reminders 
        SET is_paid = 1, 
            paid_date = :paidDate, 
            snoozed_until = NULL, 
            last_notified_at = NULL, 
            updated_at = :updatedAt 
        WHERE id = :billId
    """)
    suspend fun markAsPaid(billId: Long, paidDate: Long, updatedAt: Long)

    @Query("UPDATE bill_reminders SET is_paid = 0, paid_date = NULL, updated_at = :updatedAt WHERE id = :billId")
    suspend fun markAsUnpaid(billId: Long, updatedAt: Long)

    @Query("""
        UPDATE bill_reminders 
        SET snoozed_until = :snoozedUntil, 
            last_notified_at = NULL, 
            updated_at = :updatedAt 
        WHERE id = :billId
    """)
    suspend fun snoozeBill(billId: Long, snoozedUntil: Long, updatedAt: Long)

    @Query("UPDATE bill_reminders SET last_notified_at = :lastNotifiedAt, updated_at = :updatedAt WHERE id IN (:billIds)")
    suspend fun updateLastNotifiedAtBatch(billIds: List<Long>, lastNotifiedAt: Long, updatedAt: Long)
}