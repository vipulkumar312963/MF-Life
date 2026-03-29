package org.meerammafoundation.tools.budget.savings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class SavingsGoalViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SavingsDatabase.getDatabase(application)
    private val repository = SavingsGoalRepository(database)

    val allGoals: LiveData<List<SavingsGoal>> = repository.getAllGoals().asLiveData()
    val activeGoals: LiveData<List<SavingsGoal>> = repository.getActiveGoals().asLiveData()
    val completedGoals: LiveData<List<SavingsGoal>> = repository.getCompletedGoals().asLiveData()

    private val _selectedGoal = MutableLiveData<SavingsGoal?>()
    val selectedGoal: LiveData<SavingsGoal?> = _selectedGoal

    fun selectGoal(goal: SavingsGoal) {
        _selectedGoal.value = goal
    }

    fun createGoal(name: String, targetAmount: Double, currentAmount: Double = 0.0, targetDate: Long? = null) = viewModelScope.launch {
        repository.createGoal(name, targetAmount, currentAmount, targetDate)
    }

    fun updateGoal(goal: SavingsGoal) = viewModelScope.launch {
        repository.updateGoal(goal)
    }

    fun addToGoal(goalId: Long, amount: Double) = viewModelScope.launch {
        repository.addToGoal(goalId, amount)
    }

    fun withdrawFromGoal(goalId: Long, amount: Double) = viewModelScope.launch {
        repository.withdrawFromGoal(goalId, amount)
    }

    fun deleteGoal(goal: SavingsGoal) = viewModelScope.launch {
        repository.deleteGoal(goal)
    }

    fun getGoalWithProgress(goal: SavingsGoal): SavingsGoalWithProgress {
        return repository.getGoalWithProgress(goal)
    }
}