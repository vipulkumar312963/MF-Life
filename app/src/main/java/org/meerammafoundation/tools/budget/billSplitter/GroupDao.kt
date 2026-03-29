package org.meerammafoundation.tools.budget.billSplitter

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: Group): Long

    @Update
    suspend fun updateGroup(group: Group)

    @Delete
    suspend fun deleteGroup(group: Group)

    @Query("SELECT * FROM groups ORDER BY created_at DESC")
    fun getAllGroups(): Flow<List<Group>>

    @Query("SELECT * FROM groups WHERE id = :groupId")
    fun getGroupById(groupId: Long): Flow<Group?>
}