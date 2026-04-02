package org.meerammafoundation.tools.ui.quickaction.goals

import kotlinx.coroutines.flow.Flow

class GoalRepository(private val db: GoalDatabase) {

    private val dao = db.goalDao()

    fun getAllGoals(): Flow<List<Goal>> = dao.getAllGoals()

    fun getActiveGoals(): Flow<List<Goal>> = dao.getActiveGoals()

    fun getCompletedGoals(): Flow<List<Goal>> = dao.getCompletedGoals()

    fun getGoalById(goalId: Long): Flow<Goal?> = dao.getGoalById(goalId)

    suspend fun createGoal(
        title: String,
        goalType: GoalType,
        targetValue: Double,
        unit: String,
        currentValue: Double = 0.0,
        targetDate: Long? = null,
        description: String? = null,
        icon: String = "🎯"
    ): Long {
        val goal = Goal(
            title = title,
            description = description,
            goalType = goalType,
            targetValue = targetValue,
            currentValue = currentValue,
            unit = unit,
            targetDate = targetDate,
            icon = icon
        )
        val goalId = dao.insertGoal(goal)
        // Auto-complete if target already reached
        if (currentValue >= targetValue) {
            dao.markGoalCompleted(goalId, System.currentTimeMillis(), System.currentTimeMillis())
        }
        return goalId
    }

    suspend fun updateGoal(goal: Goal) {
        val updatedGoal = goal.copy(updatedAt = System.currentTimeMillis())
        dao.updateGoal(updatedGoal)
        // Check if now completed
        if (!updatedGoal.isCompleted && updatedGoal.currentValue >= updatedGoal.targetValue) {
            dao.markGoalCompleted(updatedGoal.id, System.currentTimeMillis(), System.currentTimeMillis())
        }
    }

    suspend fun addProgress(goalId: Long, amount: Double) {
        dao.addProgress(goalId, amount, System.currentTimeMillis())
        dao.markGoalCompleted(goalId, System.currentTimeMillis(), System.currentTimeMillis())
    }

    suspend fun removeProgress(goalId: Long, amount: Double) {
        dao.removeProgress(goalId, amount, System.currentTimeMillis())
    }

    suspend fun deleteGoal(goal: Goal) {
        dao.deleteGoal(goal)
    }

    suspend fun deleteGoalById(goalId: Long) {
        dao.deleteGoalById(goalId)
    }

    fun getGoalWithProgress(goal: Goal): GoalWithProgress {
        val progressPercentage = if (goal.targetValue > 0) {
            (goal.currentValue / goal.targetValue) * 100
        } else 0.0

        val remainingValue = goal.targetValue - goal.currentValue

        val daysRemaining = goal.targetDate?.let { targetDate ->
            val today = System.currentTimeMillis()
            val millisPerDay = 24 * 60 * 60 * 1000L
            ((targetDate - today) / millisPerDay).toInt()
        }

        val weeklyNeeded = if (daysRemaining != null && daysRemaining > 0 && remainingValue > 0) {
            remainingValue / (daysRemaining / 7.0)
        } else null

        val monthlyNeeded = if (daysRemaining != null && daysRemaining > 0 && remainingValue > 0) {
            remainingValue / (daysRemaining / 30.0)
        } else null

        return GoalWithProgress(
            goal = goal,
            progressPercentage = progressPercentage,
            remainingValue = remainingValue,
            daysRemaining = daysRemaining,
            weeklyNeeded = weeklyNeeded,
            monthlyNeeded = monthlyNeeded
        )
    }
}