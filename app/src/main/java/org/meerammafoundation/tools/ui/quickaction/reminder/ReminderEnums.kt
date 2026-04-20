package org.meerammafoundation.tools.ui.quickaction.reminder

/**
 * Reminder Status for UI display
 */
enum class ReminderStatus {
    OVERDUE,
    DUE_TODAY,
    UPCOMING,
    COMPLETED
}

/**
 * Priority levels for reminders
 */
enum class Priority {
    HIGH,
    MEDIUM,
    LOW
}

/**
 * Types of reminders
 */
enum class ReminderType {
    BILL,
    TASK,
    EVENT,
    HABIT,
    CUSTOM
}

/**
 * How often the reminder repeats
 */
enum class RecurrenceType {
    ONE_TIME,
    DAILY,      // ✅ Added DAILY
    MONTHLY,
    QUARTERLY,
    YEARLY
}

/**
 * Categories for bill-type reminders only
 */
enum class BillCategory {
    RENT, ELECTRICITY, WATER, GAS, INTERNET, PHONE, INSURANCE, SUBSCRIPTION, LOAN, OTHER
}