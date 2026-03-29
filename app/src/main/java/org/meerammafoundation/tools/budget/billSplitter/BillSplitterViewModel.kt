package org.meerammafoundation.tools.budget.billSplitter

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class BillSplitterViewModel(application: Application) : AndroidViewModel(application) {
    private val database = BillSplitterDatabase.getDatabase(application)
    private val repository = BillSplitterRepository(database)

    private val _selectedGroup = MutableLiveData<Group?>()
    val selectedGroup: LiveData<Group?> = _selectedGroup

    // ✅ Flow → LiveData conversion (Room Flow auto-updates)
    val allGroups: LiveData<List<Group>> = repository.getAllGroups().asLiveData()

    fun selectGroup(group: Group) {
        _selectedGroup.value = group
    }

    fun clearSelectedGroup() {
        _selectedGroup.value = null
    }

    fun createGroup(name: String) = viewModelScope.launch {
        repository.createGroup(name)
    }

    fun deleteGroup(group: Group) = viewModelScope.launch {
        repository.deleteGroup(group)
    }

    fun addMember(groupId: Long, name: String) = viewModelScope.launch {
        repository.addMember(groupId, name)
    }

    fun removeMember(member: Member) = viewModelScope.launch {
        repository.removeMember(member)
    }

    fun getMembers(groupId: Long): LiveData<List<Member>> =
        repository.getMembersByGroup(groupId).asLiveData()

    fun getBills(groupId: Long): LiveData<List<Bill>> =
        repository.getBillsByGroup(groupId).asLiveData()

    fun addBill(
        groupId: Long,
        description: String,
        amount: Double,
        paidById: Long,
        splitType: SplitType,
        shares: List<Pair<Long, Double>>? = null
    ) = viewModelScope.launch {
        repository.addBill(groupId, description, amount, paidById, splitType, shares)
        // No manual refresh needed - Room Flow updates automatically
    }

    // ✅ FIX 2 & 3: Simplified delete and update without manual refresh
    fun deleteBill(bill: Bill) = viewModelScope.launch {
        try {
            repository.deleteBill(bill)
            // Room Flow auto-updates - no refresh needed
        } catch (e: Exception) {
            Log.e("BillSplitterVM", "Error deleting bill: ${bill.id}", e)
        }
    }

    fun updateBill(
        bill: Bill,
        shares: List<Pair<Long, Double>>
    ) = viewModelScope.launch {
        try {
            repository.updateBillWithShares(bill, shares)
            // Room Flow auto-updates - no refresh needed
        } catch (e: Exception) {
            Log.e("BillSplitterVM", "Error updating bill: ${bill.id}", e)
        }
    }

    fun getBalances(groupId: Long): LiveData<Map<Long, Double>> =
        repository.getBalancesFlow(groupId).asLiveData()

    fun getGroupById(groupId: Long): LiveData<Group?> =
        repository.getGroupById(groupId).asLiveData()

    fun updateGroup(group: Group) = viewModelScope.launch {
        repository.updateGroup(group)
    }

    fun getBillWithShares(billId: Long): LiveData<BillWithShares> =
        repository.getBillWithShares(billId).asLiveData()

    // ✅ FIX 1: REMOVED refreshBills completely
    // ❌ No manual refresh needed - Room Flow handles it automatically
}