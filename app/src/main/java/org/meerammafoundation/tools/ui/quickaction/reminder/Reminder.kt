package org.meerammafoundation.tools.ui.quickaction.reminder

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reminders",
    indices = [
        Index(value = ["due_date"], name = "idx_reminders_due_date"),
        Index(value = ["is_completed"], name = "idx_reminders_is_completed"),
        Index(value = ["snoozed_until"], name = "idx_reminders_snoozed_until"),
        Index(value = ["last_notified_at"], name = "idx_reminders_last_notified_at"),
        Index(value = ["reminder_type"], name = "idx_reminders_type"),
        Index(value = ["priority"], name = "idx_reminders_priority")
    ]
)
data class Reminder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "description")
    val description: String? = null,

    @ColumnInfo(name = "due_date")
    val dueDate: Long,

    @ColumnInfo(name = "reminder_type")
    val reminderType: ReminderType,

    @ColumnInfo(name = "priority")
    val priority: Priority,

    @ColumnInfo(name = "recurrence")
    val recurrence: RecurrenceType,  // ✅ Add this field

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "completed_date")
    val completedDate: Long? = null,

    @ColumnInfo(name = "snoozed_until")
    val snoozedUntil: Long? = null,

    @ColumnInfo(name = "last_notified_at")
    val lastNotifiedAt: Long? = null,

    @ColumnInfo(name = "metadata")
    val metadata: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)