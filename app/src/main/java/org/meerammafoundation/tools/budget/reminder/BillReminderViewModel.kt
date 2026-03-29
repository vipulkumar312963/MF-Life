package org.meerammafoundation.tools.budget.reminder

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.work.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import org.meerammafoundation.tools.BuildConfig

class BillReminderViewModel(application: Application) : AndroidViewModel(application) {

    // ✅ Fix: Pass the database, not DAO
    private val database = BillReminderDatabase.getDatabase(application)
    private val repository = BillReminderRepository(database)

    val allBills: LiveData<List<BillReminder>> = repository.getAllBills().asLiveData()
    val unpaidBills: LiveData<List<BillReminder>> = repository.getUnpaidBills().asLiveData()
    val paidBills: LiveData<List<BillReminder>> = repository.getPaidBills().asLiveData()

    private val _selectedBill = MutableLiveData<BillReminder?>()
    val selectedBill: LiveData<BillReminder?> = _selectedBill

    init {
        startPeriodicBillCheck()
    }

    fun selectBill(bill: BillReminder) {
        _selectedBill.value = bill
    }

    fun createBill(
        name: String,
        amount: Double,
        dueDate: Long,
        category: BillCategory,
        recurrence: RecurrenceType,
        notes: String = ""
    ) = viewModelScope.launch {
        if (name.isBlank()) return@launch
        if (amount <= 0 || amount > ReminderConstants.MAX_AMOUNT) return@launch
        if (dueDate <= 0) return@launch

        repository.createBill(name, amount, dueDate, category, recurrence, notes)
        triggerImmediateCheck()
    }

    fun updateBill(bill: BillReminder) = viewModelScope.launch {
        if (bill.name.isBlank()) return@launch
        if (bill.amount <= 0 || bill.amount > ReminderConstants.MAX_AMOUNT) return@launch
        if (bill.dueDate <= 0) return@launch

        repository.updateBill(bill)
        triggerImmediateCheck()
    }

    fun deleteBill(bill: BillReminder) = viewModelScope.launch {
        repository.deleteBill(bill)
        NotificationHelper.cancelNotification(getApplication(), bill.id)
        triggerImmediateCheck()
    }

    fun markAsPaid(billId: Long) = viewModelScope.launch {
        repository.markAsPaid(billId)
        NotificationHelper.cancelNotification(getApplication(), billId)
        triggerImmediateCheck()
    }

    fun markAsUnpaid(billId: Long) = viewModelScope.launch {
        repository.markAsUnpaid(billId)
        triggerImmediateCheck()
    }

    fun snoozeBill(billId: Long, days: Int) = viewModelScope.launch {
        repository.snoozeBill(billId, days)
        triggerImmediateCheck()
    }

    private fun startPeriodicBillCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val checkInterval = if (BuildConfig.DEBUG) 15L else 24L
        val checkIntervalUnit = if (BuildConfig.DEBUG) TimeUnit.MINUTES else TimeUnit.HOURS

        val workRequest = PeriodicWorkRequestBuilder<BillReminderWorker>(
            checkInterval, checkIntervalUnit
        )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(getApplication())
            .enqueueUniquePeriodicWork(
                "bill_reminder_check",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )

        triggerImmediateCheck()
    }

    private fun triggerImmediateCheck() {
        val workRequest = OneTimeWorkRequestBuilder<BillReminderWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(getApplication())
            .enqueueUniqueWork(
                "bill_check_now",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }

    override fun onCleared() {
        super.onCleared()
        // Do NOT cancel work here
    }
}
