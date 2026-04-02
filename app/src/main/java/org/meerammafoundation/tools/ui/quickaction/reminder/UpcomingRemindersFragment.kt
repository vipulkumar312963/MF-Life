package org.meerammafoundation.tools.ui.quickaction.reminder

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import org.meerammafoundation.tools.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class UpcomingRemindersFragment : Fragment() {

    private lateinit var viewModel: ReminderViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ReminderAdapter

    companion object {
        private const val DAY_MS = 24L * 60L * 60L * 1000L

        fun newInstance(): UpcomingRemindersFragment {
            return UpcomingRemindersFragment()
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
                // Mark as completed
                viewModel.markAsCompleted(reminder.id)
                Toast.makeText(requireContext(), "Reminder marked as completed", Toast.LENGTH_SHORT).show()
            },
            { reminder, days ->
                viewModel.snoozeReminder(reminder.id, days)
                Toast.makeText(requireContext(), "Reminder snoozed for $days days", Toast.LENGTH_SHORT).show()
            },
            { reminder -> showEditReminderDialog(reminder) },
            { reminder -> showDeleteReminderDialog(reminder) }
        )
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(requireActivity())[ReminderViewModel::class.java]

        viewModel.uncompletedReminders.observe(viewLifecycleOwner) { reminders ->
            val uncompletedWithStatus = reminders.map { reminder ->
                val daysUntilDue = ((reminder.dueDate - System.currentTimeMillis()) / DAY_MS).toInt()
                val status = when {
                    daysUntilDue < 0 -> ReminderStatus.OVERDUE
                    daysUntilDue == 0 -> ReminderStatus.DUE_TODAY
                    else -> ReminderStatus.UPCOMING
                }
                ReminderWithStatus(
                    reminder = reminder,
                    daysUntilDue = daysUntilDue,
                    status = status
                )
            }
            adapter.updateData(uncompletedWithStatus)
        }

        return view
    }

    private fun showEditReminderDialog(reminder: Reminder) {
        val dialogView = layoutInflater.inflate(R.layout.reminder_dialog_add_reminder, null)
        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etReminderTitle)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etReminderDescription)
        val spinnerType = dialogView.findViewById<Spinner>(R.id.spinnerReminderType)
        val spinnerPriority = dialogView.findViewById<Spinner>(R.id.spinnerPriority)
        val spinnerRecurrence = dialogView.findViewById<Spinner>(R.id.spinnerRecurrence)
        val tvDueDate = dialogView.findViewById<TextView>(R.id.tvDueDate)

        val layoutBillFields = dialogView.findViewById<LinearLayout>(R.id.layoutBillFields)
        val etAmount = dialogView.findViewById<TextInputEditText>(R.id.etAmount)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)

        etTitle.setText(reminder.title)
        etDescription.setText(reminder.description)

        val types = ReminderType.values().map { type ->
            type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
        }
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = typeAdapter
        spinnerType.setSelection(reminder.reminderType.ordinal)

        val priorities = Priority.values().map { priority ->
            priority.name.replaceFirstChar { it.uppercase() }
        }
        val priorityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, priorities)
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = priorityAdapter
        spinnerPriority.setSelection(reminder.priority.ordinal)

        val recurrences = RecurrenceType.values().map { recurrence ->
            recurrence.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
        }
        val recurrenceAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, recurrences)
        recurrenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRecurrence.adapter = recurrenceAdapter
        spinnerRecurrence.setSelection(reminder.recurrence.ordinal)

        if (reminder.reminderType == ReminderType.BILL && reminder.metadata != null) {
            try {
                val metadata = Converters().jsonToBillMetadata(reminder.metadata)
                etAmount.setText(metadata.amount.toString())
                val categoryIndex = BillCategory.values().indexOfFirst { it.name == metadata.category }
                if (categoryIndex >= 0) {
                    spinnerCategory.setSelection(categoryIndex)
                }
            } catch (e: Exception) { }
        }

        fun updateBillFieldsVisibility(type: ReminderType) {
            layoutBillFields.visibility = if (type == ReminderType.BILL) View.VISIBLE else View.GONE
        }
        updateBillFieldsVisibility(reminder.reminderType)

        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                updateBillFieldsVisibility(ReminderType.values()[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        var selectedDate = reminder.dueDate
        tvDueDate.text = dateFormat.format(Date(selectedDate))

        tvDueDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = selectedDate
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val date = Calendar.getInstance()
                    date.set(year, month, dayOfMonth, 9, 0, 0)
                    selectedDate = date.timeInMillis
                    tvDueDate.text = dateFormat.format(date.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Reminder")
            .setView(dialogView)
            .setPositiveButton("Update", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val typeIndex = spinnerType.selectedItemPosition
                val priorityIndex = spinnerPriority.selectedItemPosition
                val recurrenceIndex = spinnerRecurrence.selectedItemPosition

                if (title.isEmpty()) {
                    etTitle.error = "Enter title"
                    return@setOnClickListener
                }

                val reminderType = ReminderType.values().getOrNull(typeIndex) ?: ReminderType.TASK
                val priority = Priority.values().getOrNull(priorityIndex) ?: Priority.MEDIUM
                val recurrence = RecurrenceType.values().getOrNull(recurrenceIndex) ?: RecurrenceType.ONE_TIME

                var metadata: String? = null

                if (reminderType == ReminderType.BILL) {
                    val amountStr = etAmount.text.toString().trim()
                    if (amountStr.isEmpty()) {
                        etAmount.error = "Enter amount"
                        return@setOnClickListener
                    }
                    val amount = amountStr.toDoubleOrNull()
                    if (amount == null || amount <= 0) {
                        etAmount.error = "Enter valid amount"
                        return@setOnClickListener
                    }
                    val categoryIndex = spinnerCategory.selectedItemPosition
                    val category = BillCategory.values().getOrNull(categoryIndex) ?: BillCategory.OTHER
                    metadata = Converters().billMetadataToJson(BillMetadata(amount = amount, category = category.name))
                }

                val updatedReminder = reminder.copy(
                    title = title,
                    description = description,
                    dueDate = selectedDate,
                    reminderType = reminderType,
                    priority = priority,
                    recurrence = recurrence,
                    metadata = metadata,
                    updatedAt = System.currentTimeMillis()
                )
                viewModel.updateReminder(updatedReminder)
                Toast.makeText(requireContext(), "Reminder updated", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialog.show()
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