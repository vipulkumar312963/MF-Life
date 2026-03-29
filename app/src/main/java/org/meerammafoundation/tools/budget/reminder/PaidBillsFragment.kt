package org.meerammafoundation.tools.budget.reminder

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

class PaidBillsFragment : Fragment() {

    private lateinit var viewModel: BillReminderViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BillReminderAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.reminder_fragment_bill_reminder, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewBills)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // ✅ Fix: Add snooze callback (empty lambda for paid bills)
        adapter = BillReminderAdapter(
            emptyList(),
            { bill -> showMarkUnpaidDialog(bill) },  // Mark as Unpaid for paid bills
            { _, _ -> },  // ✅ Snooze - no action for paid bills
            { bill -> showEditBillDialog(bill) },
            { bill -> showDeleteBillDialog(bill) }
        )
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(requireActivity())[BillReminderViewModel::class.java]

        viewModel.paidBills.observe(viewLifecycleOwner) { bills ->
            val paidWithStatus = bills.map { bill ->
                BillReminderWithStatus(
                    bill = bill,
                    daysUntilDue = 0,
                    status = BillStatus.PAID
                )
            }
            adapter.updateData(paidWithStatus)
        }

        return view
    }

    private fun showMarkUnpaidDialog(bill: BillReminder) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Mark as Unpaid")
            .setMessage("Mark '${bill.name}' as unpaid?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.markAsUnpaid(bill.id)
                Toast.makeText(requireContext(), "Bill marked as unpaid", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showEditBillDialog(bill: BillReminder) {
        Toast.makeText(requireContext(), "Edit coming soon", Toast.LENGTH_SHORT).show()
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