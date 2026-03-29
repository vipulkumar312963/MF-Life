package org.meerammafoundation.tools.budget

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.meerammafoundation.tools.R
import java.text.NumberFormat
import java.util.Locale

class ExpenseAdapter(private val expenses: MutableList<Expense>) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvDescription: TextView = itemView.findViewById(R.id.tvDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.expensetracker_item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.tvDate.text = expense.date
        holder.tvCategory.text = expense.category
        holder.tvDescription.text = expense.description
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        holder.tvAmount.text = format.format(expense.amount)
    }

    override fun getItemCount() = expenses.size
}