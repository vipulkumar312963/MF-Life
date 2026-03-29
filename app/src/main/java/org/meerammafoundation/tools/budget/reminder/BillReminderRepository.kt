package org.meerammafoundation.tools.budget.reminder

import kotlinx.coroutines.flow.Flow

class BillReminderRepository(private val db: BillReminderDatabase) {

    private val dao = db.billReminderDao()

    companion object {
        private const val DAY_MS = 24L * 60L * 60L * 1000L
    }

    fun getAllBills(): Flow<List<BillReminder>> = dao.getAllBills()

    fun getUnpaidBills(): Flow<List<BillReminder>> = dao.getUnpaidBills()

    fun getPaidBills(): Flow<List<BillReminder>> = dao.getPaidBills()

    fun getBillById(billId: Long): Flow<BillReminder?> = dao.getBillById(billId)

    suspend fun createBill(
        name: String,
        amount: Double,
        dueDate: Long,
        category: BillCategory,
        recurrence: RecurrenceType,
        notes: String = ""
    ): Long {
        val now = System.currentTimeMillis()
        val bill = BillReminder(
            name = name,
            amount = amount,
            dueDate = dueDate,
            category = category,
            recurrence = recurrence,
            notes = notes,
            createdAt = now,
            updatedAt = now
        )
        return dao.insertBill(bill)
    }

    suspend fun updateBill(bill: BillReminder) {
        val updatedBill = bill.copy(updatedAt = System.currentTimeMillis())
        dao.updateBill(updatedBill)
    }

    suspend fun deleteBill(bill: BillReminder) {
        dao.deleteBillById(bill.id)
    }

    suspend fun markAsPaid(billId: Long) {
        val now = System.currentTimeMillis()
        dao.markAsPaid(billId, now, now)
    }

    suspend fun markAsUnpaid(billId: Long) {
        dao.markAsUnpaid(billId, System.currentTimeMillis())
    }

    suspend fun snoozeBill(billId: Long, days: Int) {
        val snoozedUntil = System.currentTimeMillis() + (days * DAY_MS)
        dao.snoozeBill(billId, snoozedUntil, System.currentTimeMillis())
    }

    suspend fun updateLastNotifiedAtBatch(billIds: List<Long>, lastNotifiedAt: Long) {
        if (billIds.isNotEmpty()) {
            dao.updateLastNotifiedAtBatch(billIds, lastNotifiedAt, System.currentTimeMillis())
        }
    }
}