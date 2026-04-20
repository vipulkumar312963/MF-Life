package org.meerammafoundation.tools.ui.quickaction.goals

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "goal_type")
    val goalType: GoalType,

    // Progress tracking
    @ColumnInfo(name = "target_value")
    val targetValue: Double,

    @ColumnInfo(name = "current_value")
    val currentValue: Double = 0.0,

    @ColumnInfo(name = "unit")
    val unit: String,  // "₹", "kg", "books", "hours", "km", "days", etc.

    // Recurrence (NEW)
    @ColumnInfo(name = "recurrence")
    val recurrence: GoalRecurrence = GoalRecurrence.ONE_TIME,

    @ColumnInfo(name = "custom_duration_days")
    val customDurationDays: Int? = null,  // For CUSTOM recurrence

    // Timeline
    @ColumnInfo(name = "target_date")
    val targetDate: Long? = null,

    @ColumnInfo(name = "start_date")
    val startDate: Long = System.currentTimeMillis(),

    // Status
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "completed_date")
    val completedDate: Long? = null,

    // Visual
    @ColumnInfo(name = "icon")
    val icon: String = "🎯",

    @ColumnInfo(name = "color")
    val color: String? = null,

    // Notification tracking
    @ColumnInfo(name = "last_progress_notified_at")
    val lastProgressNotifiedAt: Long? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

enum class GoalType {
    FINANCIAL,   // Save money
    HABIT,       // Daily habits (track streak)
    LEARNING,    // Learn new skills
    HEALTH,      // Fitness/wellness
    READING,     // Books/articles
    PRODUCTIVITY,// Tasks/projects
    CUSTOM       // User defined
}

// NEW: Goal recurrence options
enum class GoalRecurrence {
    ONE_TIME,    // One-time goal
    DAILY,       // Daily recurring goal
    WEEKLY,      // Weekly recurring goal
    MONTHLY,     // Monthly recurring goal
    YEARLY,      // Yearly recurring goal
    CUSTOM       // Custom duration (days)
}

data class GoalWithProgress(
    val goal: Goal,
    val progressPercentage: Double,
    val remainingValue: Double,
    val daysRemaining: Int?,
    val weeklyNeeded: Double?,
    val monthlyNeeded: Double?
)