package org.meerammafoundation.tools.notifications

import android.content.Context
import org.meerammafoundation.tools.ui.quickaction.goals.GoalDatabase
import org.meerammafoundation.tools.ui.quickaction.goals.GoalRepository
import org.meerammafoundation.tools.ui.quickaction.goals.Goal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object GoalNotificationHelper {

    fun getGoalNotifications(context: Context): List<NotificationItem> {
        val notifications = mutableListOf<NotificationItem>()

        try {
            val database = GoalDatabase.getDatabase(context)
            val repository = GoalRepository(database)

            // Run blocking for simplicity in notification context
            val allGoals = runBlocking {
                repository.getAllGoals().first()
            }

            val currentTime = System.currentTimeMillis()
            val dayMs = 24L * 60L * 60L * 1000L

            allGoals.forEach { goal ->
                if (!goal.isCompleted) {
                    val progress = (goal.currentValue / goal.targetValue * 100).toInt()

                    // Goal completed notification
                    if (progress >= 100 && !goal.isCompleted) {
                        notifications.add(
                            NotificationItem(
                                id = goal.id,
                                title = "🎉 Goal Completed! 🎉",
                                message = "Congratulations! You've completed '${goal.title}'!",
                                time = System.currentTimeMillis(),
                                type = NotificationType.GOAL_COMPLETED,
                                priority = NotificationPriority.HIGH,
                                sourceId = goal.id,
                                sourceType = "goal",
                                isRead = false
                            )
                        )
                    }
                    // Milestone notifications (25%, 50%, 75%)
                    else if (progress >= 75 && progress < 80) {
                        notifications.add(
                            NotificationItem(
                                id = goal.id,
                                title = "Goal Milestone: 75%",
                                message = "You're 75% done with '${goal.title}'! Keep going!",
                                time = System.currentTimeMillis(),
                                type = NotificationType.GOAL_MILESTONE,
                                priority = NotificationPriority.MEDIUM,
                                sourceId = goal.id,
                                sourceType = "goal",
                                isRead = false
                            )
                        )
                    } else if (progress >= 50 && progress < 55) {
                        notifications.add(
                            NotificationItem(
                                id = goal.id,
                                title = "Goal Milestone: 50%",
                                message = "Halfway there! '${goal.title}' is 50% complete!",
                                time = System.currentTimeMillis(),
                                type = NotificationType.GOAL_MILESTONE,
                                priority = NotificationPriority.MEDIUM,
                                sourceId = goal.id,
                                sourceType = "goal",
                                isRead = false
                            )
                        )
                    } else if (progress >= 25 && progress < 30) {
                        notifications.add(
                            NotificationItem(
                                id = goal.id,
                                title = "Goal Milestone: 25%",
                                message = "Great start! You're 25% done with '${goal.title}'!",
                                time = System.currentTimeMillis(),
                                type = NotificationType.GOAL_MILESTONE,
                                priority = NotificationPriority.MEDIUM,
                                sourceId = goal.id,
                                sourceType = "goal",
                                isRead = false
                            )
                        )
                    }
                    // Regular progress notification (weekly)
                    else if (shouldSendWeeklyProgress(goal)) {
                        notifications.add(
                            NotificationItem(
                                id = goal.id,
                                title = "Goal Progress Update",
                                message = "'${goal.title}': ${progress}% complete (${formatValue(goal.currentValue, goal.unit)} of ${formatValue(goal.targetValue, goal.unit)})",
                                time = System.currentTimeMillis(),
                                type = NotificationType.GOAL,
                                priority = NotificationPriority.LOW,
                                sourceId = goal.id,
                                sourceType = "goal",
                                isRead = false
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Handle error silently
        }

        return notifications
    }

    private fun shouldSendWeeklyProgress(goal: Goal): Boolean {
        // Check if last progress notification was sent more than 7 days ago
        // For simplicity, we'll use a simple approach
        val lastProgressNotified = goal.lastProgressNotifiedAt ?: 0L
        val weekMs = 7 * 24L * 60L * 60L * 1000L
        return System.currentTimeMillis() - lastProgressNotified > weekMs
    }

    private fun formatValue(value: Double, unit: String): String {
        return when (unit) {
            "₹" -> "₹${String.format("%.0f", value)}"
            "%" -> "${String.format("%.0f", value)}%"
            "kg" -> "${String.format("%.1f", value)} kg"
            "km" -> "${String.format("%.1f", value)} km"
            "books" -> "${String.format("%.0f", value)} books"
            else -> "${String.format("%.0f", value)} $unit"
        }
    }
}