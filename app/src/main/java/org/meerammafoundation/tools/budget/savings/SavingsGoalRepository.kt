package org.meerammafoundation.tools.budget.savings

import kotlinx.coroutines.flow.Flow

class SavingsGoalRepository(private val db: SavingsDatabase) {

    private val dao = db.savingsGoalDao()

    fun getAllGoals(): Flow<List<SavingsGoal>> = dao.getAllGoals()

    fun getActiveGoals(): Flow<List<SavingsGoal>> = dao.getActiveGoals()

    fun getCompletedGoals(): Flow<List<SavingsGoal>> = dao.getCompletedGoals()

    fun getGoalById(goalId: Long): Flow<SavingsGoal?> = dao.getGoalById(goalId)

    suspend fun createGoal(name: String, targetAmount: Double, currentAmount: Double = 0.0, targetDate: Long? = null): Long {
        val goal = SavingsGoal(
            name = name,
            targetAmount = targetAmount,
            currentAmount = currentAmount,
            targetDate = targetDate
        )
        val goalId = dao.insertGoal(goal)
        // Auto-complete if target already reached
        if (currentAmount >= targetAmount) {
            dao.markGoalCompleted(goalId, System.currentTimeMillis())
        }
        return goalId
    }

    suspend fun updateGoal(goal: SavingsGoal) {
        val updatedGoal = goal.copy(updatedAt = System.currentTimeMillis())
        dao.updateGoal(updatedGoal)
    }

    suspend fun addToGoal(goalId: Long, amount: Double) {
        dao.addToGoal(goalId, amount, System.currentTimeMillis())
        dao.markGoalCompleted(goalId, System.currentTimeMillis())
    }

    suspend fun withdrawFromGoal(goalId: Long, amount: Double) {
        dao.withdrawFromGoal(goalId, amount, System.currentTimeMillis())
    }

    suspend fun deleteGoal(goal: SavingsGoal) {
        dao.deleteGoal(goal)
    }

    suspend fun deleteGoalById(goalId: Long) {
        dao.deleteGoalById(goalId)
    }

    fun getGoalWithProgress(goal: SavingsGoal): SavingsGoalWithProgress {
        val progressPercentage = if (goal.targetAmount > 0) {
            (goal.currentAmount / goal.targetAmount) * 100
        } else 0.0

        val remainingAmount = goal.targetAmount - goal.currentAmount

        val daysRemaining = goal.targetDate?.let { targetDate ->
            val today = System.currentTimeMillis()
            val millisPerDay = 24 * 60 * 60 * 1000L
            ((targetDate - today) / millisPerDay).toInt()
        }

        val monthlySavingsNeeded = if (daysRemaining != null && daysRemaining > 0 && remainingAmount > 0) {
            remainingAmount / (daysRemaining / 30.0)
        } else null

        return SavingsGoalWithProgress(
            goal = goal,
            progressPercentage = progressPercentage,
            remainingAmount = remainingAmount,
            daysRemaining = daysRemaining,
            monthlySavingsNeeded = monthlySavingsNeeded
        )
    }
}