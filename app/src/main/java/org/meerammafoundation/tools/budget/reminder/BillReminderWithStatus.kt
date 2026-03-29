package org.meerammafoundation.tools.budget.reminder

data class BillReminderWithStatus(
    val bill: BillReminder,
    val daysUntilDue: Int,
    val status: BillStatus
)

enum class BillStatus {
    PAID,
    UPCOMING,
    DUE_TODAY,
    OVERDUE
}