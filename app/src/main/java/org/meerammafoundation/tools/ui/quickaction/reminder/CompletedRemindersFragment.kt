package org.meerammafoundation.tools.ui.quickaction.reminder

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.meerammafoundation.tools.R

class CompletedRemindersFragment : Fragment() {

    private lateinit var viewModel: ReminderViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReminderAdapter

    companion object {
        fun newInstance(): CompletedRemindersFragment {
            return CompletedRemindersFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.reminder_fragment_reminder, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewReminders)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = ReminderAdapter(
            emptyList(),
            { reminder ->
                // Mark as incomplete (will move to upcoming)
                viewModel.markAsUncompleted(reminder.id)
                Toast.makeText(requireContext(), "Reminder marked as incomplete", Toast.LENGTH_SHORT).show()
            },
            { _, _ -> },  // Snooze not needed for completed reminders
            { reminder ->
                // Edit - This won't be called because edit is not shown in options for completed reminders
                // Keeping for completeness but won't be used
                showEditReminderDialog(reminder)
            },
            { reminder ->
                // Delete reminder
                showDeleteReminderDialog(reminder)
            }
        )
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(requireActivity())[ReminderViewModel::class.java]

        viewModel.completedReminders.observe(viewLifecycleOwner) { reminders ->
            val completedWithStatus = reminders.map { reminder ->
                ReminderWithStatus(
                    reminder = reminder,
                    daysUntilDue = 0,
                    status = ReminderStatus.COMPLETED
                )
            }
            adapter.updateData(completedWithStatus)
        }

        return view
    }

    private fun showEditReminderDialog(reminder: Reminder) {
        // This is kept for reference but won't be called
        // Completed reminders don't show Edit option in the dialog
        Toast.makeText(requireContext(), "Edit not available for completed reminders", Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteReminderDialog(reminder: Reminder) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Reminder")
            .setMessage("Are you sure you want to delete '${reminder.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteReminder(reminder)
                Toast.makeText(requireContext(), "Reminder deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}