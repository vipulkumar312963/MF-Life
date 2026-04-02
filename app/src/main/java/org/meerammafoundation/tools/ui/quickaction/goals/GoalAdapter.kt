package org.meerammafoundation.tools.ui.quickaction.goals

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

class GoalAdapter(
    private var goals: List<Goal>,
    private val onAddClick: (Goal) -> Unit,
    private val onRemoveClick: (Goal) -> Unit,
    private val onEditClick: (Goal) -> Unit,
    private val onDeleteClick: (Goal) -> Unit
) : RecyclerView.Adapter<GoalAdapter.GoalViewHolder>() {

    private val numberFormat = NumberFormat.getInstance(Locale.getDefault())
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    fun updateData(newGoals: List<Goal>) {
        goals = newGoals
        notifyDataSetChanged()
    }

    private fun formatValue(value: Double, unit: String): String {
        return when (unit) {
            "₹" -> NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(value)
            "%" -> "${numberFormat.format(value)}%"
            "kg" -> "${numberFormat.format(value)} kg"
            "km" -> "${numberFormat.format(value)} km"
            "hours" -> "${numberFormat.format(value)} hours"
            "books" -> numberFormat.format(value).toInt().toString()
            else -> "${numberFormat.format(value)} $unit"
        }
    }

    class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvIcon: TextView = itemView.findViewById(R.id.tvGoalIcon)
        private val tvGoalName: TextView = itemView.findViewById(R.id.tvGoalName)
        private val tvCurrentValue: TextView = itemView.findViewById(R.id.tvCurrentValue)
        private val tvTargetValue: TextView = itemView.findViewById(R.id.tvTargetValue)
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val tvProgress: TextView = itemView.findViewById(R.id.tvProgress)
        private val tvDeadline: TextView = itemView.findViewById(R.id.tvDeadline)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val btnAdd: Button = itemView.findViewById(R.id.btnAddProgress)
        private val btnRemove: Button = itemView.findViewById(R.id.btnRemoveProgress)
        private val ivEdit: ImageView = itemView.findViewById(R.id.ivEditGoal)
        private val ivDelete: ImageView = itemView.findViewById(R.id.ivDeleteGoal)

        fun bind(
            goal: Goal,
            formatValue: (Double, String) -> String,
            dateFormat: SimpleDateFormat,
            onAdd: (Goal) -> Unit,
            onRemove: (Goal) -> Unit,
            onEdit: (Goal) -> Unit,
            onDelete: (Goal) -> Unit
        ) {
            tvIcon.text = goal.icon
            tvGoalName.text = goal.title
            tvCurrentValue.text = formatValue(goal.currentValue, goal.unit)
            tvTargetValue.text = formatValue(goal.targetValue, goal.unit)

            val progress = if (goal.targetValue > 0) {
                ((goal.currentValue / goal.targetValue) * 100).toInt()
            } else 0
            progressBar.progress = progress
            tvProgress.text = "$progress% completed"

            if (goal.isCompleted) {
                tvStatus.text = "✓ COMPLETED"
                tvStatus.visibility = View.VISIBLE
                btnAdd.visibility = View.GONE
                btnRemove.visibility = View.GONE
                ivEdit.visibility = View.GONE
            } else {
                tvStatus.visibility = View.GONE
                btnAdd.visibility = View.VISIBLE
                btnRemove.visibility = if (goal.currentValue > 0) View.VISIBLE else View.GONE
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
            btnRemove.setOnClickListener { onRemove(goal) }
            ivEdit.setOnClickListener { onEdit(goal) }
            ivDelete.setOnClickListener { onDelete(goal) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.goal_item_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        holder.bind(
            goals[position],
            ::formatValue,
            dateFormat,
            onAddClick,
            onRemoveClick,
            onEditClick,
            onDeleteClick
        )
    }

    override fun getItemCount() = goals.size
}