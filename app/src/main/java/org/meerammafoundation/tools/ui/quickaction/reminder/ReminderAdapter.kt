package org.meerammafoundation.tools.ui.quickaction.reminder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.meerammafoundation.tools.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReminderAdapter(
    private var reminders: List<ReminderWithStatus>,
    private val onMarkCompleted: (Reminder) -> Unit,
    private val onSnooze: (Reminder, Int) -> Unit,
    private val onEdit: (Reminder) -> Unit,
    private val onDelete: (Reminder) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

    class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvIcon: TextView = itemView.findViewById(R.id.tvReminderIcon)
        private val tvTitle: TextView = itemView.findViewById(R.id.tvReminderTitle)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tvDueDate)
        private val tvPriority: TextView = itemView.findViewById(R.id.tvPriority)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)

        fun bind(
            reminderWithStatus: ReminderWithStatus,
            onMarkCompleted: (Reminder) -> Unit,
            onSnooze: (Reminder, Int) -> Unit,
            onEdit: (Reminder) -> Unit,
            onDelete: (Reminder) -> Unit,
            dateFormat: SimpleDateFormat
        ) {
            val reminder = reminderWithStatus.reminder

            // Set icon based on type
            tvIcon.text = when (reminder.reminderType) {
                ReminderType.BILL -> "💰"
                ReminderType.TASK -> "✓"
                ReminderType.EVENT -> "📅"
                ReminderType.HABIT -> "🔄"
                ReminderType.CUSTOM -> "📌"
            }

            tvTitle.text = reminder.title

            val date = Date(reminder.dueDate)
            tvDueDate.text = dateFormat.format(date)

            // Set priority color and text
            when (reminder.priority) {
                Priority.HIGH -> {
                    tvPriority.setTextColor(0xFFE53935.toInt())
                    tvPriority.text = "High"
                }
                Priority.MEDIUM -> {
                    tvPriority.setTextColor(0xFFFFA000.toInt())
                    tvPriority.text = "Medium"
                }
                Priority.LOW -> {
                    tvPriority.setTextColor(0xFF43A047.toInt())
                    tvPriority.text = "Low"
                }
            }

            // Set status text
            tvStatus.text = when (reminderWithStatus.status) {
                ReminderStatus.OVERDUE -> "Overdue"
                ReminderStatus.DUE_TODAY -> "Due Today"
                ReminderStatus.UPCOMING -> "Upcoming"
                ReminderStatus.COMPLETED -> "Completed"
            }

            // Set background color based on status
            when (reminderWithStatus.status) {
                ReminderStatus.OVERDUE -> itemView.setBackgroundColor(0x1AE53935.toInt())
                ReminderStatus.DUE_TODAY -> itemView.setBackgroundColor(0x1AFFA000.toInt())
                else -> itemView.setBackgroundColor(0)
            }

            // Single click - Edit (only for non-completed reminders)
            if (!reminder.isCompleted) {
                itemView.setOnClickListener { onEdit(reminder) }
            } else {
                itemView.setOnClickListener(null)  // No edit for completed reminders
            }

            // Long press - Show options dialog (different options based on status)
            itemView.setOnLongClickListener {
                showOptionsDialog(reminder, onMarkCompleted, onSnooze, onEdit, onDelete)
                true
            }
        }

        private fun showOptionsDialog(
            reminder: Reminder,
            onMarkCompleted: (Reminder) -> Unit,
            onSnooze: (Reminder, Int) -> Unit,
            onEdit: (Reminder) -> Unit,
            onDelete: (Reminder) -> Unit
        ) {
            val options = mutableListOf<String>()
            val actions = mutableListOf<() -> Unit>()

            if (reminder.isCompleted) {
                // ✅ For COMPLETED reminders: Only "Mark as Incomplete" and "Delete"
                options.add("Mark as Incomplete")
                actions.add { onMarkCompleted(reminder) }

                options.add("Delete")
                actions.add { onDelete(reminder) }

            } else {
                // ✅ For UPCOMING reminders: Full options
                // Mark as Completed
                options.add("Mark as Completed")
                actions.add { onMarkCompleted(reminder) }

                // Snooze options
                options.add("Snooze for 1 day")
                actions.add { onSnooze(reminder, 1) }

                options.add("Snooze for 3 days")
                actions.add { onSnooze(reminder, 3) }

                options.add("Snooze for 7 days")
                actions.add { onSnooze(reminder, 7) }

                // Edit option
                options.add("Edit")
                actions.add { onEdit(reminder) }

                // Delete option
                options.add("Delete")
                actions.add { onDelete(reminder) }
            }

            MaterialAlertDialogBuilder(itemView.context)
                .setTitle(reminder.title)
                .setItems(options.toTypedArray()) { _, which ->
                    actions[which].invoke()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.reminder_item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        holder.bind(
            reminders[position],
            onMarkCompleted,
            onSnooze,
            onEdit,
            onDelete,
            dateFormat
        )
    }

    override fun getItemCount() = reminders.size

    fun updateData(newReminders: List<ReminderWithStatus>) {
        reminders = newReminders
        notifyDataSetChanged()
    }
}