package org.meerammafoundation.tools.ui.quickaction.reminder

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
import org.meerammafoundation.tools.notifications.NotificationHelper

class ReminderViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ReminderDatabase.getDatabase(application)
    private val repository = ReminderRepository(database)

    val allReminders: LiveData<List<Reminder>> = repository.getAllReminders().asLiveData()
    val uncompletedReminders: LiveData<List<Reminder>> = repository.getUncompletedReminders().asLiveData()
    val completedReminders: LiveData<List<Reminder>> = repository.getCompletedReminders().asLiveData()

    private val _selectedReminder = MutableLiveData<Reminder?>()
    val selectedReminder: LiveData<Reminder?> = _selectedReminder

    init {
        startPeriodicReminderCheck()
    }

    fun selectReminder(reminder: Reminder) {
        _selectedReminder.value = reminder
    }

    fun createReminder(
        title: String,
        description: String? = null,
        dueDate: Long,
        reminderType: ReminderType,
        priority: Priority,
        recurrence: RecurrenceType,
        metadata: String? = null
    ) = viewModelScope.launch {
        if (title.isBlank()) return@launch
        if (dueDate <= 0) return@launch

        repository.createReminder(title, description, dueDate, reminderType, priority, recurrence, metadata)
        triggerImmediateCheck()
    }

    fun updateReminder(reminder: Reminder) = viewModelScope.launch {
        if (reminder.title.isBlank()) return@launch
        if (reminder.dueDate <= 0) return@launch

        repository.updateReminder(reminder)
        triggerImmediateCheck()
    }

    fun deleteReminder(reminder: Reminder) = viewModelScope.launch {
        repository.deleteReminder(reminder)
        NotificationHelper.cancelNotification(getApplication(), reminder.id)
        triggerImmediateCheck()
    }

    fun markAsCompleted(reminderId: Long) = viewModelScope.launch {
        repository.markAsCompleted(reminderId)
        NotificationHelper.cancelNotification(getApplication(), reminderId)
        triggerImmediateCheck()
    }

    fun markAsUncompleted(reminderId: Long) = viewModelScope.launch {
        repository.markAsUncompleted(reminderId)
        triggerImmediateCheck()
    }

    fun snoozeReminder(reminderId: Long, days: Int) = viewModelScope.launch {
        repository.snoozeReminder(reminderId, days)
        triggerImmediateCheck()
    }

    private fun startPeriodicReminderCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val checkInterval = if (BuildConfig.DEBUG) 15L else 24L
        val checkIntervalUnit = if (BuildConfig.DEBUG) TimeUnit.MINUTES else TimeUnit.HOURS

        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            checkInterval, checkIntervalUnit
        )
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(getApplication())
            .enqueueUniquePeriodicWork(
                "reminder_check",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )

        triggerImmediateCheck()
    }

    private fun triggerImmediateCheck() {
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(5, TimeUnit.SECONDS)
            .build()

        WorkManager.getInstance(getApplication())
            .enqueueUniqueWork(
                "reminder_check_now",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
    }

    override fun onCleared() {
        super.onCleared()
    }
}