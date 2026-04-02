package org.meerammafoundation.tools.ui.quickaction.billSplitter

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BillShareDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillShare(billShare: BillShare)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBillShares(billShares: List<BillShare>)

    @Query("SELECT * FROM bill_shares WHERE billId = :billId")
    fun getSharesByBill(billId: Long): Flow<List<BillShare>>

    @Query("DELETE FROM bill_shares WHERE billId = :billId")
    suspend fun deleteSharesByBill(billId: Long)
}