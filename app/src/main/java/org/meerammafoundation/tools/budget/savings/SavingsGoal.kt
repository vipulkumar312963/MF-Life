package org.meerammafoundation.tools.budget.savings

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_goals")
data class SavingsGoal(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "target_amount")
    val targetAmount: Double,
    @ColumnInfo(name = "current_amount")
    val currentAmount: Double = 0.0,
    @ColumnInfo(name = "target_date")
    val targetDate: Long? = null,
    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)

data class SavingsGoalWithProgress(
    val goal: SavingsGoal,
    val progressPercentage: Double,
    val remainingAmount: Double,
    val daysRemaining: Int?,
    val monthlySavingsNeeded: Double?
)