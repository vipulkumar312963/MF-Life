package org.meerammafoundation.tools.ui.quickaction.reminder

object ReminderConstants {

    // ========== General Reminder Constants ==========
    const val MAX_TITLE_LENGTH = 100
    const val MAX_DESCRIPTION_LENGTH = 500

    // Default reminder time (9:00 AM)
    const val DEFAULT_REMINDER_HOUR = 9
    const val DEFAULT_REMINDER_MINUTE = 0

    // ========== Bill-Specific Constants ==========
    const val MAX_AMOUNT = 9_99_999.0  // Max 9,99,999
    const val MIN_AMOUNT = 1.0

    // ========== Task-Specific Constants ==========
    const val MAX_TASK_DURATION_HOURS = 24
    const val MIN_TASK_DURATION_MINUTES = 5

    // ========== Event-Specific Constants ==========
    const val MAX_EVENT_DURATION_HOURS = 72
    const val MAX_LOCATION_LENGTH = 200

    // ========== Habit-Specific Constants ==========
    const val MAX_HABIT_STREAK = 365
    const val DEFAULT_HABIT_WEEKLY_TARGET = 7

    // ========== Notification Constants ==========
    const val NOTIFICATION_ID_OFFSET = 1000
    const val NOTIFICATION_COOLDOWN_HOURS = 1

    // ========== Database Constants ==========
    const val DATABASE_VERSION = 1
    const val DATABASE_NAME = "reminder_database"
}