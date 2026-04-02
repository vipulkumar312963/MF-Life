package org.meerammafoundation.tools.ui.quickaction.billSplitter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.meerammafoundation.tools.R
import java.text.NumberFormat
import java.util.Locale

class BillAdapter(
    private var bills: List<Bill>,
    private val getMemberName: (Long) -> String,
    private val getSelectedMemberCount: (Bill) -> Int,
    private val onEditClick: (Bill) -> Unit,
    private val onDeleteClick: (Bill) -> Unit
) : RecyclerView.Adapter<BillAdapter.BillViewHolder>() {

    private val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    // ✅ FIX 2: Safer updateList with list copy
    fun updateList(newList: List<Bill>) {
        bills = newList.toList() // Copy to avoid mutation issues
        notifyDataSetChanged()
    }

    class BillViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDescription: TextView = itemView.findViewById(R.id.tvBillDescription)
        private val tvAmount: TextView = itemView.findViewById(R.id.tvBillAmount)
        private val tvPaidBy: TextView = itemView.findViewById(R.id.tvPaidBy)
        private val tvSplitDetails: TextView = itemView.findViewById(R.id.tvSplitDetails)
        private val tvBillIcon: TextView = itemView.findViewById(R.id.tvBillIcon)
        private val ivEdit: ImageView = itemView.findViewById(R.id.ivEditBill)
        private val ivDelete: ImageView = itemView.findViewById(R.id.ivDeleteBill)

        // ✅ FIX 3: Safe bind with local variable
        fun bind(
            bill: Bill,
            getMemberName: (Long) -> String,
            getSelectedMemberCount: (Bill) -> Int,
            format: NumberFormat,
            onEdit: (Bill) -> Unit,
            onDelete: (Bill) -> Unit
        ) {
            // Capture the current bill to avoid reference issues
            val currentBill = bill

            tvDescription.text = currentBill.description
            tvAmount.text = format.format(currentBill.amount)
            tvPaidBy.text = "Paid by: ${getMemberName(currentBill.paidById)}"

            val selectedCount = getSelectedMemberCount(currentBill)

            tvSplitDetails.text = when (currentBill.splitType) {
                SplitType.EQUAL -> "Split equally between $selectedCount people"
                SplitType.CUSTOM -> "Split custom amounts"
            }

            tvBillIcon.text = when (currentBill.splitType) {
                SplitType.EQUAL -> "🤝"
                SplitType.CUSTOM -> "✂️"
            }

            // Use the captured variable for click listeners
            ivEdit.setOnClickListener { onEdit(currentBill) }
            ivDelete.setOnClickListener { onDelete(currentBill) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BillViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.billsplitter_item_bill, parent, false)
        return BillViewHolder(view)
    }

    // ✅ FIX 1: Safe position check
    override fun onBindViewHolder(holder: BillViewHolder, position: Int) {
        // Guard against invalid positions
        if (position < 0 || position >= bills.size) {
            return
        }

        val bill = bills[position]

        holder.bind(
            bill,
            getMemberName,
            getSelectedMemberCount,
            format,
            onEditClick,
            onDeleteClick
        )
    }

    override fun getItemCount() = bills.size
}