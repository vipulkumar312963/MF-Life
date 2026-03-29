package org.meerammafoundation.tools.budget.savings

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SavingsGoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: SavingsGoal): Long

    @Update
    suspend fun updateGoal(goal: SavingsGoal)

    @Delete
    suspend fun deleteGoal(goal: SavingsGoal)

    @Query("SELECT * FROM savings_goals ORDER BY is_completed ASC, target_date ASC, created_at DESC")
    fun getAllGoals(): Flow<List<SavingsGoal>>

    @Query("SELECT * FROM savings_goals WHERE is_completed = 0 ORDER BY target_date ASC, created_at DESC")
    fun getActiveGoals(): Flow<List<SavingsGoal>>

    @Query("SELECT * FROM savings_goals WHERE is_completed = 1 ORDER BY updated_at DESC")
    fun getCompletedGoals(): Flow<List<SavingsGoal>>

    @Query("SELECT * FROM savings_goals WHERE id = :goalId")
    fun getGoalById(goalId: Long): Flow<SavingsGoal?>

    @Query("UPDATE savings_goals SET current_amount = current_amount + :amount, updated_at = :updatedAt WHERE id = :goalId")
    suspend fun addToGoal(goalId: Long, amount: Double, updatedAt: Long)

    @Query("UPDATE savings_goals SET current_amount = current_amount - :amount, updated_at = :updatedAt WHERE id = :goalId AND current_amount >= :amount")
    suspend fun withdrawFromGoal(goalId: Long, amount: Double, updatedAt: Long)

    @Query("UPDATE savings_goals SET is_completed = 1, updated_at = :updatedAt WHERE id = :goalId AND current_amount >= target_amount")
    suspend fun markGoalCompleted(goalId: Long, updatedAt: Long)

    @Query("DELETE FROM savings_goals WHERE id = :goalId")
    suspend fun deleteGoalById(goalId: Long)
}