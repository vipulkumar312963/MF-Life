package org.meerammafoundation.tools.notifications

enum class NotificationType {
    REMINDER,      // From Reminder feature
    GOAL,          // From Goals feature
    GOAL_MILESTONE, // Goal progress milestone
    GOAL_COMPLETED, // Goal completed
    BIRTHDAY,      // Birthday reminders
    CUSTOM         // Custom notifications
}

enum class NotificationPriority {
    HIGH,      // Overdue, Urgent, Goal completed
    MEDIUM,    // Due today, Important, Goal milestone
    LOW        // Upcoming, Regular, Goal progress
}

data class NotificationItem(
    val id: Long,
    val title: String,
    val message: String,
    val time: Long,
    val type: NotificationType,
    val priority: NotificationPriority,
    val sourceId: Long,        // ID of the source (reminderId, goalId, etc.)
    val sourceType: String,    // "reminder", "goal", "birthday"
    val isRead: Boolean = false
)

data class NotificationGroup(
    val type: NotificationType,
    val items: List<NotificationItem>,
    val unreadCount: Int
)