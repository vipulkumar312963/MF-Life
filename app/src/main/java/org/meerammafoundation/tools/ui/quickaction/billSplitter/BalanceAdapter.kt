package org.meerammafoundation.tools.ui.quickaction.billSplitter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import org.meerammafoundation.tools.R
import java.text.NumberFormat
import java.util.Locale

class BalanceAdapter(
    private var balances: Map<Long, Double>,
    private val getMemberName: (Long) -> String
) : RecyclerView.Adapter<BalanceAdapter.BalanceViewHolder>() {

    private val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    fun updateData(newBalances: Map<Long, Double>) {
        balances = newBalances
        notifyDataSetChanged()
    }

    class BalanceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvMemberName: TextView = itemView.findViewById(R.id.tvMemberName)
        private val tvBalance: TextView = itemView.findViewById(R.id.tvBalance)

        fun bind(memberName: String, balance: Double, format: NumberFormat) {
            tvMemberName.text = memberName
            tvBalance.text = format.format(balance)

            val color = if (balance >= 0) {
                ContextCompat.getColor(itemView.context, R.color.primary)
            } else {
                ContextCompat.getColor(itemView.context, R.color.text_secondary)
            }
            tvBalance.setTextColor(color)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BalanceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.billsplitter_item_balance, parent, false)
        return BalanceViewHolder(view)
    }

    override fun onBindViewHolder(holder: BalanceViewHolder, position: Int) {
        // ✅ Always get fresh entry list from current balances
        val entry = balances.entries.toList()[position]
        holder.bind(getMemberName(entry.key), entry.value, format)
    }

    override fun getItemCount(): Int = balances.size
}