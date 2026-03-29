package org.meerammafoundation.tools.budget.reminder

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bill_reminders",
    indices = [
        Index(value = ["due_date"], name = "idx_bill_reminders_due_date"),
        Index(value = ["is_paid"], name = "idx_bill_reminders_is_paid"),
        Index(value = ["snoozed_until"], name = "idx_bill_reminders_snoozed_until"),
        Index(value = ["last_notified_at"], name = "idx_bill_reminders_last_notified_at")
    ]
)
data class BillReminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val amount: Double,
    @ColumnInfo(name = "due_date")
    val dueDate: Long,
    val category: BillCategory,
    val recurrence: RecurrenceType,
    @ColumnInfo(name = "is_paid")
    val isPaid: Boolean = false,
    @ColumnInfo(name = "paid_date")
    val paidDate: Long? = null,
    @ColumnInfo(name = "snoozed_until")
    val snoozedUntil: Long? = null,
    @ColumnInfo(name = "last_notified_at")
    val lastNotifiedAt: Long? = null,
    @ColumnInfo(name = "notes")
    val notes: String = "",
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

enum class BillCategory {
    RENT, ELECTRICITY, WATER, GAS, INTERNET, PHONE, INSURANCE, SUBSCRIPTION, LOAN, OTHER
}

enum class RecurrenceType {
    ONE_TIME, MONTHLY, QUARTERLY, YEARLY
}