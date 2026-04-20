package org.meerammafoundation.tools.ui.quickaction.goals

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class GoalViewModel(application: Application) : AndroidViewModel(application) {

    private val database = GoalDatabase.getDatabase(application)
    private val repository = GoalRepository(database)

    val allGoals: LiveData<List<Goal>> = repository.getAllGoals().asLiveData()
    val activeGoals: LiveData<List<Goal>> = repository.getActiveGoals().asLiveData()
    val completedGoals: LiveData<List<Goal>> = repository.getCompletedGoals().asLiveData()

    private val _selectedGoal = MutableLiveData<Goal?>()
    val selectedGoal: LiveData<Goal?> = _selectedGoal

    init {
        viewModelScope.launch {
            repository.regenerateRecurringGoalsIfNeeded()
        }
    }

    fun selectGoal(goal: Goal) {
        _selectedGoal.value = goal
    }

    fun regenerateRecurringGoalsIfNeeded() = viewModelScope.launch {
        repository.regenerateRecurringGoalsIfNeeded()
    }

    fun createGoal(
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
    ) = viewModelScope.launch {
        repository.createGoal(
            title = title,
            goalType = goalType,
            targetValue = targetValue,
            unit = unit,
            currentValue = currentValue,
            targetDate = targetDate,
            description = description,
            icon = icon,
            recurrence = recurrence,
            customDurationDays = customDurationDays
        )
    }

    fun updateGoal(goal: Goal) = viewModelScope.launch {
        repository.updateGoal(goal)
    }

    fun addProgress(goalId: Long, amount: Double) = viewModelScope.launch {
        repository.addProgress(goalId, amount)
    }

    fun removeProgress(goalId: Long, amount: Double) = viewModelScope.launch {
        repository.removeProgress(goalId, amount)
    }

    fun deleteGoal(goal: Goal) = viewModelScope.launch {
        repository.deleteGoal(goal)
    }

    fun markGoalAsCompleted(goalId: Long) = viewModelScope.launch {
        repository.markGoalAsCompleted(goalId)
    }

    fun getGoalWithProgress(goal: Goal): GoalWithProgress {
        return repository.getGoalWithProgress(goal)
    }
}