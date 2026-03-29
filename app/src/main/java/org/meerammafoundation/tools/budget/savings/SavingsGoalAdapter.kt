package org.meerammafoundation.tools.budget.savings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.meerammafoundation.tools.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class SavingsGoalAdapter(
    private var goals: List<SavingsGoal>,
    private val onAddClick: (SavingsGoal) -> Unit,
    private val onWithdrawClick: (SavingsGoal) -> Unit,
    private val onEditClick: (SavingsGoal) -> Unit,
    private val onDeleteClick: (SavingsGoal) -> Unit
) : RecyclerView.Adapter<SavingsGoalAdapter.GoalViewHolder>() {

    private val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun updateData(newGoals: List<SavingsGoal>) {
        goals = newGoals
        notifyDataSetChanged()
    }

    class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvGoalName: TextView = itemView.findViewById(R.id.tvGoalName)
        private val tvCurrentAmount: TextView = itemView.findViewById(R.id.tvCurrentAmount)
        private val tvTargetAmount: TextView = itemView.findViewById(R.id.tvTargetAmount)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val tvProgress: TextView = itemView.findViewById(R.id.tvProgress)
        private val tvDeadline: TextView = itemView.findViewById(R.id.tvDeadline)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val btnAdd: Button = itemView.findViewById(R.id.btnAddMoney)
        private val btnWithdraw: Button = itemView.findViewById(R.id.btnWithdraw)
        private val ivEdit: ImageView = itemView.findViewById(R.id.ivEditGoal)
        private val ivDelete: ImageView = itemView.findViewById(R.id.ivDeleteGoal)

        fun bind(
            goal: SavingsGoal,
            format: NumberFormat,
            dateFormat: SimpleDateFormat,
            onAdd: (SavingsGoal) -> Unit,
            onWithdraw: (SavingsGoal) -> Unit,
            onEdit: (SavingsGoal) -> Unit,
            onDelete: (SavingsGoal) -> Unit
        ) {
            tvGoalName.text = goal.name
            tvCurrentAmount.text = format.format(goal.currentAmount)
            tvTargetAmount.text = format.format(goal.targetAmount)

            val progress = if (goal.targetAmount > 0) {
                ((goal.currentAmount / goal.targetAmount) * 100).toInt()
            } else 0
            progressBar.progress = progress
            tvProgress.text = "$progress% completed"

            if (goal.isCompleted) {
                tvStatus.text = "✓ COMPLETED"
                tvStatus.visibility = View.VISIBLE
                btnAdd.visibility = View.GONE
                btnWithdraw.visibility = View.GONE
                ivEdit.visibility = View.GONE  // ✅ Hide edit button
            } else {
                tvStatus.visibility = View.GONE
                btnAdd.visibility = View.VISIBLE
                btnWithdraw.visibility = View.VISIBLE
                ivEdit.visibility = View.VISIBLE
            }

            // Deadline info
            goal.targetDate?.let { targetDate ->
                val daysLeft = ((targetDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
                tvDeadline.text = when {
                    daysLeft < 0 -> "Deadline passed"
                    daysLeft == 0 -> "Deadline today"
                    else -> "$daysLeft days left"
                }
                tvDeadline.visibility = View.VISIBLE
            } ?: run {
                tvDeadline.visibility = View.GONE
            }

            btnAdd.setOnClickListener { onAdd(goal) }
            btnWithdraw.setOnClickListener { onWithdraw(goal) }
            ivEdit.setOnClickListener { onEdit(goal) }
            ivDelete.setOnClickListener { onDelete(goal) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.savings_item_savings_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        holder.bind(goals[position], format, dateFormat, onAddClick, onWithdrawClick, onEditClick, onDeleteClick)
    }

    override fun getItemCount() = goals.size
}