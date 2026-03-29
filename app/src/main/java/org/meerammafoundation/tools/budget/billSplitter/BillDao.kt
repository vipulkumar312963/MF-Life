package org.meerammafoundation.tools.budget.billSplitter

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBill(bill: Bill): Long

    @Update
    suspend fun updateBill(bill: Bill)

    @Delete
    suspend fun deleteBill(bill: Bill)

    @Query("SELECT * FROM bills WHERE groupId = :groupId ORDER BY created_at DESC")
    fun getBillsByGroup(groupId: Long): Flow<List<Bill>>

    @Transaction
    @Query("SELECT * FROM bills WHERE id = :billId")
    fun getBillWithShares(billId: Long): Flow<BillWithShares>
}