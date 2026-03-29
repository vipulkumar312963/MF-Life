package org.meerammafoundation.tools.budget.reminder

import android.app.DatePickerDialog
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
import java.util.Locale

class UpcomingBillsFragment : Fragment() {

    private lateinit var viewModel: BillReminderViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BillReminderAdapter

    companion object {
        private const val DAY_MS = 24L * 60L * 60L * 1000L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.reminder_fragment_bill_reminder, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewBills)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = BillReminderAdapter(
            emptyList(),
            { bill -> showMarkPaidDialog(bill) },
            { bill, days -> showSnoozeDialog(bill, days) },
            { bill -> showEditBillDialog(bill) },  // ✅ Edit callback
            { bill -> showDeleteBillDialog(bill) }
        )
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(requireActivity())[BillReminderViewModel::class.java]

        viewModel.unpaidBills.observe(viewLifecycleOwner) { bills ->
            val unpaidWithStatus = bills.map { bill ->
                val daysUntilDue = ((bill.dueDate - System.currentTimeMillis()) / DAY_MS).toInt()
                val status = when {
                    daysUntilDue < 0 -> BillStatus.OVERDUE
                    daysUntilDue == 0 -> BillStatus.DUE_TODAY
                    else -> BillStatus.UPCOMING
                }
                BillReminderWithStatus(
                    bill = bill,
                    daysUntilDue = daysUntilDue,
                    status = status
                )
            }
            adapter.updateData(unpaidWithStatus)
        }

        return view
    }

    private fun showMarkPaidDialog(bill: BillReminder) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Mark as Paid")
            .setMessage("Mark '${bill.name}' as paid?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.markAsPaid(bill.id)
                Toast.makeText(requireContext(), "Bill marked as paid", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showSnoozeDialog(bill: BillReminder, days: Int) {
        val dayText = when (days) {
            1 -> "1 day"
            3 -> "3 days"
            7 -> "7 days"
            30 -> "1 month"
            else -> "$days days"
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Confirm Snooze")
            .setMessage("Snooze '${bill.name}' for $dayText?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.snoozeBill(bill.id, days)
                Toast.makeText(requireContext(), "Bill snoozed for $dayText", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    // ✅ Add Edit Bill Dialog
    private fun showEditBillDialog(bill: BillReminder) {
        val dialogView = layoutInflater.inflate(R.layout.reminder_dialog_add_bill_reminder, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etBillName)
        val etAmount = dialogView.findViewById<TextInputEditText>(R.id.etBillAmount)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val spinnerRecurrence = dialogView.findViewById<Spinner>(R.id.spinnerRecurrence)
        val tvDueDate = dialogView.findViewById<TextView>(R.id.tvDueDate)
        val etNotes = dialogView.findViewById<TextInputEditText>(R.id.etNotes)

        // Pre-fill with existing data
        etName.setText(bill.name)
        etAmount.setText(bill.amount.toString())
        etNotes.setText(bill.notes)

        // Set category spinner selection
        val categories = BillCategory.values().map { category ->
            category.name.replace("_", " ")
                .lowercase()
                .replaceFirstChar { it.uppercase() }
        }
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter
        spinnerCategory.setSelection(bill.category.ordinal)

        // Set recurrence spinner selection
        val recurrences = RecurrenceType.values().map { recurrence ->
            recurrence.name.replace("_", " ")
                .lowercase()
                .replaceFirstChar { it.uppercase() }
        }
        val recurrenceAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, recurrences)
        recurrenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRecurrence.adapter = recurrenceAdapter
        spinnerRecurrence.setSelection(bill.recurrence.ordinal)

        // Date picker
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        var selectedDate = bill.dueDate
        tvDueDate.text = dateFormat.format(java.util.Date(selectedDate))

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
            .setTitle("Edit Bill Reminder")
            .setView(dialogView)
            .setPositiveButton("Update", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(android.app.Dialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val name = etName.text.toString().trim()
                val amountStr = etAmount.text.toString().trim()
                val categoryIndex = spinnerCategory.selectedItemPosition
                val recurrenceIndex = spinnerRecurrence.selectedItemPosition

                if (name.isEmpty()) {
                    etName.error = "Enter bill name"
                    return@setOnClickListener
                }

                if (amountStr.isEmpty()) {
                    etAmount.error = "Enter amount"
                    return@setOnClickListener
                }

                val amount = amountStr.toDoubleOrNull()
                if (amount == null || amount <= 0 || amount > ReminderConstants.MAX_AMOUNT) {
                    etAmount.error = "Enter valid amount"
                    return@setOnClickListener
                }

                val category = BillCategory.values().getOrNull(categoryIndex) ?: BillCategory.OTHER
                val recurrence = RecurrenceType.values().getOrNull(recurrenceIndex) ?: RecurrenceType.ONE_TIME
                val notes = etNotes.text.toString().trim()

                // Update the bill
                val updatedBill = bill.copy(
                    name = name,
                    amount = amount,
                    dueDate = selectedDate,
                    category = category,
                    recurrence = recurrence,
                    notes = notes,
                    updatedAt = System.currentTimeMillis()
                )
                viewModel.updateBill(updatedBill)
                Toast.makeText(requireContext(), "Bill updated", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun showDeleteBillDialog(bill: BillReminder) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Bill")
            .setMessage("Are you sure you want to delete '${bill.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteBill(bill)
                Toast.makeText(requireContext(), "Bill deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}