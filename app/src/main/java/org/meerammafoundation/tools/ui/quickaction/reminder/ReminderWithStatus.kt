package org.meerammafoundation.tools.ui.quickaction.reminder

data class ReminderWithStatus(
    val reminder: Reminder,
    val daysUntilDue: Int,
    val status: ReminderStatus
)