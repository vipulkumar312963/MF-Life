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

data class GoalWithProgress(
    val goal: Goal,
    val progressPercentage: Double,
    val remainingValue: Double,
    val daysRemaining: Int?,
    val weeklyNeeded: Double?,
    val monthlyNeeded: Double?
)