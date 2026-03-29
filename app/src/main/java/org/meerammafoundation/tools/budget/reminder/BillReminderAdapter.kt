package org.meerammafoundation.tools.budget.reminder

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.meerammafoundation.tools.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BillReminderAdapter(
    private var bills: List<BillReminderWithStatus>,
    private val onPaidClick: (BillReminder) -> Unit,
    private val onSnoozeClick: (BillReminder, Int) -> Unit,
    private val onEditClick: (BillReminder) -> Unit,
    private val onDeleteClick: (BillReminder) -> Unit
) : RecyclerView.Adapter<BillReminderAdapter.BillViewHolder>() {

    private val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)

    fun updateData(newBills: List<BillReminderWithStatus>) {
        bills = newBills
        notifyDataSetChanged()
    }

    class BillViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvBillName)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvBillAmount)
        private val tvDueDate: TextView = itemView.findViewById(R.id.tvDueDate)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        private val btnMarkPaid: Button = itemView.findViewById(R.id.btnMarkPaid)
        private val btnSnooze: Button = itemView.findViewById(R.id.btnSnooze)
        private val ivEdit: ImageView = itemView.findViewById(R.id.ivEditBill)
        private val ivDelete: ImageView = itemView.findViewById(R.id.ivDeleteBill)

        fun bind(
            billWithStatus: BillReminderWithStatus,
            format: NumberFormat,
            dateFormat: SimpleDateFormat,
            onPaid: (BillReminder) -> Unit,
            onSnooze: (BillReminder, Int) -> Unit,
            onEdit: (BillReminder) -> Unit,
            onDelete: (BillReminder) -> Unit
        ) {
            val bill = billWithStatus.bill
            tvName.text = bill.name
            tvAmount.text = format.format(bill.amount)
            tvDueDate.text = "Due: ${dateFormat.format(Date(bill.dueDate))}"

            tvCategory.text = bill.category.name
                .replace("_", " ")
                .lowercase()
                .replaceFirstChar { it.uppercase() }

            val statusText = if (bill.isPaid) {
                "Paid ✓"
            } else {
                when (billWithStatus.status) {
                    BillStatus.DUE_TODAY -> "Due Today!"
                    BillStatus.OVERDUE -> "Overdue!"
                    BillStatus.UPCOMING -> "${billWithStatus.daysUntilDue} days left"
                    else -> ""
                }
            }
            tvStatus.text = statusText

            val statusColor = if (bill.isPaid) {
                R.color.primary
            } else {
                when (billWithStatus.status) {
                    BillStatus.DUE_TODAY -> R.color.primary
                    BillStatus.OVERDUE -> R.color.text_secondary
                    BillStatus.UPCOMING -> R.color.text_secondary
                    else -> R.color.text_secondary
                }
            }
            tvStatus.setTextColor(ContextCompat.getColor(itemView.context, statusColor))

            if (bill.isPaid) {
                btnMarkPaid.visibility = View.GONE
                btnSnooze.visibility = View.GONE
                ivEdit.visibility = View.GONE
            } else {
                btnMarkPaid.visibility = View.VISIBLE
                btnSnooze.visibility = View.VISIBLE
                ivEdit.visibility = View.VISIBLE
            }

            btnMarkPaid.setOnClickListener { onPaid(bill) }
            btnSnooze.setOnClickListener { showSnoozeDialog(bill, onSnooze) }
            ivEdit.setOnClickListener { onEdit(bill) }
            ivDelete.setOnClickListener { onDelete(bill) }
        }

        // ✅ Single dialog with confirmation built-in - no double confirmation
        private fun showSnoozeDialog(bill: BillReminder, onSnooze: (BillReminder, Int) -> Unit) {
            val options = arrayOf("1 day", "3 days", "7 days", "1 month")

            MaterialAlertDialogBuilder(itemView.context)
                .setTitle("Snooze ${bill.name}")
                .setItems(options) { _, which ->
                    val days = when (which) {
                        0 -> 1
                        1 -> 3
                        2 -> 7
                        3 -> 30
                        else -> 1
                    }
                    // ✅ Call snooze directly - no second confirmation
                    onSnooze(bill, days)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.reminder_item_bill_reminder, parent, false)
        return BillViewHolder(view)
    }

    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        holder.bind(
            bills[position],
            format,
            dateFormat,
            onPaidClick,
            onSnoozeClick,
            onEditClick,
            onDeleteClick
        )
    }

    override fun getItemCount() = bills.size
}