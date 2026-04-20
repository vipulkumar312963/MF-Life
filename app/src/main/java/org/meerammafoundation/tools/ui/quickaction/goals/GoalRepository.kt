package org.meerammafoundation.tools.ui.quickaction.goals

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.Calendar

class GoalRepository(private val db: GoalDatabase) {

    private val dao = db.goalDao()
    private val TAG = "GoalRepository"

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
        icon: String = "🎯",
        recurrence: GoalRecurrence = GoalRecurrence.ONE_TIME,
        customDurationDays: Int? = null
    ): Long {
        val now = System.currentTimeMillis()
        val goal = Goal(
            title = title,
            description = description,
            goalType = goalType,
            targetValue = targetValue,
            currentValue = currentValue,
            unit = unit,
            targetDate = targetDate,
            icon = icon,
            recurrence = recurrence,
            customDurationDays = customDurationDays,
            createdAt = now,
            updatedAt = now
        )
        val goalId = dao.insertGoal(goal)
        Log.d(TAG, "createGoal: Created goal with id=$goalId, recurrence=$recurrence")

        if (currentValue >= targetValue) {
            Log.d(TAG, "createGoal: Goal already completed, marking as completed")
            dao.markGoalCompleted(goalId, now, now)
        }
        return goalId
    }

    suspend fun updateGoal(goal: Goal) {
        val updatedGoal = goal.copy(updatedAt = System.currentTimeMillis())
        dao.updateGoal(updatedGoal)
        Log.d(TAG, "updateGoal: Updated goal id=${goal.id}")

        // ✅ No auto-completion here - fixed!
    }

    suspend fun addProgress(goalId: Long, amount: Double) {
        val goal = getGoalById(goalId).first() ?: return
        Log.d(TAG, "addProgress: Goal id=$goalId, currentValue=${goal.currentValue}, amount=$amount")

        val newTotal = goal.currentValue + amount
        dao.addProgress(goalId, amount, System.currentTimeMillis())

        if (newTotal >= goal.targetValue && !goal.isCompleted) {
            val completedDate = System.currentTimeMillis()
            dao.markGoalCompleted(goalId, completedDate, completedDate)
            Log.d(TAG, "addProgress: Goal completed - will regenerate on next cycle")
        }
    }

    suspend fun markGoalAsCompleted(goalId: Long) {
        val goal = getGoalById(goalId).first() ?: return
        if (!goal.isCompleted) {
            val completedDate = System.currentTimeMillis()
            dao.markGoalCompleted(goalId, completedDate, completedDate)
            Log.d(TAG, "markGoalAsCompleted: Goal completed - will regenerate on next cycle")
        }
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

    suspend fun updateLastProgressNotifiedAt(goalId: Long, notifiedAt: Long) {
        val goal = getGoalById(goalId).first()
        goal?.let {
            val updatedGoal = it.copy(lastProgressNotifiedAt = notifiedAt)
            dao.updateGoal(updatedGoal)
        }
    }

    // ✅ FIXED: Regenerate recurring goals with proper alignment
    suspend fun regenerateRecurringGoalsIfNeeded() {
        val allGoals = dao.getAllGoals().first()
        val now = System.currentTimeMillis()

        // ✅ OPTIMIZATION: Only check completed goals
        val completedGoals = allGoals.filter { it.isCompleted && it.recurrence != GoalRecurrence.ONE_TIME }

        // ✅ OPTIMIZATION: Get active goals once for duplicate checking
        val activeGoals = allGoals.filter { !it.isCompleted }

        completedGoals.forEach { goal ->
            val nextDate = calculateNextDate(goal) ?: return@forEach

            if (now >= nextDate) {
                // ✅ OPTIMIZATION: Check against active goals only
                val exists = activeGoals.any {
                    it.title == goal.title &&
                            it.targetDate == nextDate &&
                            !it.isCompleted
                }

                if (!exists) {
                    val newGoal = goal.copy(
                        id = 0,  // New ID
                        currentValue = 0.0,
                        isCompleted = false,
                        completedDate = null,
                        targetDate = nextDate,
                        startDate = now,
                        createdAt = now,
                        updatedAt = now
                    )

                    dao.insertGoal(newGoal)
                    Log.d(TAG, "regenerateRecurringGoalsIfNeeded: Recreated goal: ${goal.title}")
                }
            }
        }
    }

    // ✅ FIXED: Calculate next date with proper alignment for Weekly and Yearly
    private fun calculateNextDate(goal: Goal): Long? {
        val calendar = Calendar.getInstance()
        val startDate = goal.targetDate ?: System.currentTimeMillis()
        calendar.timeInMillis = startDate

        return when (goal.recurrence) {
            GoalRecurrence.DAILY -> {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
                setToStartOfDay(calendar)
                calendar.timeInMillis
            }
            GoalRecurrence.WEEKLY -> {
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                // ✅ FIXED: Align to next Monday
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                setToStartOfDay(calendar)
                calendar.timeInMillis
            }
            GoalRecurrence.MONTHLY -> {
                calendar.add(Calendar.MONTH, 1)
                setToStartOfDay(calendar)
                calendar.timeInMillis
            }
            GoalRecurrence.YEARLY -> {
                calendar.add(Calendar.YEAR, 1)
                // ✅ FIXED: Align to January 1st
                calendar.set(Calendar.MONTH, Calendar.JANUARY)
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                setToStartOfDay(calendar)
                calendar.timeInMillis
            }
            GoalRecurrence.CUSTOM -> {
                goal.customDurationDays?.let {
                    calendar.add(Calendar.DAY_OF_YEAR, it)
                    setToStartOfDay(calendar)
                    calendar.timeInMillis
                }
            }
            else -> null
        }
    }

    private fun setToStartOfDay(calendar: Calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
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