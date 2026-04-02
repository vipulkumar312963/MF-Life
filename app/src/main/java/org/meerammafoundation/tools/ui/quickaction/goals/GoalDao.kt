package org.meerammafoundation.tools.ui.quickaction.goals

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: Goal): Long

    @Update
    suspend fun updateGoal(goal: Goal)

    @Delete
    suspend fun deleteGoal(goal: Goal)

    @Query("SELECT * FROM goals ORDER BY is_completed ASC, target_date ASC, created_at DESC")
    fun getAllGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE is_completed = 0 ORDER BY target_date ASC, created_at DESC")
    fun getActiveGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE is_completed = 1 ORDER BY updated_at DESC")
    fun getCompletedGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals WHERE id = :goalId")
    fun getGoalById(goalId: Long): Flow<Goal?>

    @Query("UPDATE goals SET current_value = current_value + :amount, updated_at = :updatedAt WHERE id = :goalId")
    suspend fun addProgress(goalId: Long, amount: Double, updatedAt: Long)

    @Query("UPDATE goals SET current_value = current_value - :amount, updated_at = :updatedAt WHERE id = :goalId AND current_value >= :amount")
    suspend fun removeProgress(goalId: Long, amount: Double, updatedAt: Long)

    @Query("UPDATE goals SET is_completed = 1, completed_date = :completedDate, updated_at = :updatedAt WHERE id = :goalId AND current_value >= target_value")
    suspend fun markGoalCompleted(goalId: Long, completedDate: Long, updatedAt: Long)

    @Query("DELETE FROM goals WHERE id = :goalId")
    suspend fun deleteGoalById(goalId: Long)
}