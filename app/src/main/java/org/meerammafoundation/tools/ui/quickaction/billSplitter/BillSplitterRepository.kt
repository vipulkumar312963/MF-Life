package org.meerammafoundation.tools.ui.quickaction.billSplitter

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import android.util.Log

class BillSplitterRepository(private val db: BillSplitterDatabase) {

    // ========== GROUPS ==========
    suspend fun createGroup(name: String): Long {
        val group = Group(name = name)
        return db.groupDao().insertGroup(group)
    }

    suspend fun deleteGroup(group: Group) = db.groupDao().deleteGroup(group)

    fun getAllGroups(): Flow<List<Group>> = db.groupDao().getAllGroups()

    suspend fun updateGroup(group: Group) = db.groupDao().updateGroup(group)

    fun getGroupById(groupId: Long): Flow<Group?> = db.groupDao().getGroupById(groupId)

    // ========== MEMBERS ==========
    suspend fun addMember(groupId: Long, name: String): Long {
        val member = Member(groupId = groupId, name = name)
        return db.memberDao().insertMember(member)
    }

    suspend fun removeMember(member: Member) = db.memberDao().deleteMember(member)

    fun getMembersByGroup(groupId: Long): Flow<List<Member>> = db.memberDao().getMembersByGroup(groupId)

    // ========== BILLS ==========

    // ✅ FIX: Transaction for addBill using official withTransaction
    suspend fun addBill(
        groupId: Long,
        description: String,
        amount: Double,
        paidById: Long,
        splitType: SplitType,
        shares: List<Pair<Long, Double>>? = null
    ) {
        db.withTransaction {
            val bill = Bill(
                groupId = groupId,
                description = description,
                amount = amount,
                paidById = paidById,
                splitType = splitType
            )
            val billId = db.billDao().insertBill(bill)

            if (splitType == SplitType.EQUAL) {
                if (shares != null) {
                    // Use provided shares (selected members only)
                    val billShares = shares.map { (memberId, shareAmount) ->
                        BillShare(
                            billId = billId,
                            memberId = memberId,
                            shareAmount = shareAmount
                        )
                    }
                    db.billShareDao().insertAllBillShares(billShares)
                } else {
                    // Fallback: all members equally
                    val members = db.memberDao().getMembersByGroup(groupId).first()
                    val shareAmount = amount / members.size
                    val billShares = members.map { member ->
                        BillShare(
                            billId = billId,
                            memberId = member.id,
                            shareAmount = shareAmount
                        )
                    }
                    db.billShareDao().insertAllBillShares(billShares)
                }
            } else if (splitType == SplitType.CUSTOM && shares != null) {
                val billShares = shares.map { (memberId, shareAmount) ->
                    BillShare(
                        billId = billId,
                        memberId = memberId,
                        shareAmount = shareAmount
                    )
                }
                db.billShareDao().insertAllBillShares(billShares)
            }
        }
    }

    // ✅ FIX: Transaction for deleteBill using official withTransaction
    suspend fun deleteBill(bill: Bill) {
        try {
            db.withTransaction {
                // First delete all shares associated with this bill
                db.billShareDao().deleteSharesByBill(bill.id)
                // Then delete the bill itself
                db.billDao().deleteBill(bill)
            }
        } catch (e: Exception) {
            Log.e("BillSplitterRepo", "Error deleting bill: ${bill.id}", e)
            throw e
        }
    }

    fun getBillsByGroup(groupId: Long): Flow<List<Bill>> = db.billDao().getBillsByGroup(groupId)

    suspend fun updateBill(bill: Bill) = db.billDao().updateBill(bill)

    // ✅ FIX: Transaction for updateBillWithShares using official withTransaction
    suspend fun updateBillWithShares(
        bill: Bill,
        shares: List<Pair<Long, Double>>
    ) {
        try {
            db.withTransaction {
                // Delete old shares
                db.billShareDao().deleteSharesByBill(bill.id)

                // Update the bill
                db.billDao().updateBill(bill)

                // Add new shares
                val billShares = shares.map { (memberId, amount) ->
                    BillShare(
                        billId = bill.id,
                        memberId = memberId,
                        shareAmount = amount
                    )
                }
                db.billShareDao().insertAllBillShares(billShares)
            }
        } catch (e: Exception) {
            Log.e("BillSplitterRepo", "Error updating bill with shares: ${bill.id}", e)
            throw e
        }
    }

    // ========== BILL SHARES ==========
    suspend fun deleteSharesByBill(billId: Long) = db.billShareDao().deleteSharesByBill(billId)

    suspend fun addBillShares(billId: Long, shares: List<Pair<Long, Double>>) {
        val billShares = shares.map { (memberId, amount) ->
            BillShare(
                billId = billId,
                memberId = memberId,
                shareAmount = amount
            )
        }
        db.billShareDao().insertAllBillShares(billShares)
    }

    fun getBillWithShares(billId: Long): Flow<BillWithShares> = db.billDao().getBillWithShares(billId)

    // ========== BALANCES ==========
    suspend fun getBalances(groupId: Long): Map<Long, Double> {
        val members = db.memberDao().getMembersByGroup(groupId).first()
        val balances = members.associate { it.id to 0.0 }.toMutableMap()

        val bills = db.billDao().getBillsByGroup(groupId).first()
        for (bill in bills) {
            // Person who paid gets positive
            balances[bill.paidById] = balances[bill.paidById]!! + bill.amount

            // Shares: each member owes
            val shares = db.billShareDao().getSharesByBill(bill.id).first()
            for (share in shares) {
                balances[share.memberId] = balances[share.memberId]!! - share.shareAmount
            }
        }
        return balances
    }

    // Flow version of getBalances for ViewModel
    fun getBalancesFlow(groupId: Long): Flow<Map<Long, Double>> = flow {
        emit(getBalances(groupId))
    }.flowOn(Dispatchers.IO)
}