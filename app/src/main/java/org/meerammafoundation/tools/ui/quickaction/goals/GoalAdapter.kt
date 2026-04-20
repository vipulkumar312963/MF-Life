package org.meerammafoundation.tools.ui.quickaction.goals

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.meerammafoundation.tools.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class GoalAdapter(
    private var goals: List<Goal>,
    private val onGoalClick: (Goal) -> Unit,
    private val onGoalLongClick: (Goal) -> Unit
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

    private fun getRecurrenceText(recurrence: GoalRecurrence): String {
        return when (recurrence) {
            GoalRecurrence.DAILY -> "Daily"
            GoalRecurrence.WEEKLY -> "Weekly"
            GoalRecurrence.MONTHLY -> "Monthly"
            GoalRecurrence.YEARLY -> "Yearly"
            GoalRecurrence.ONE_TIME -> "One time"
            GoalRecurrence.CUSTOM -> "Custom"
        }
    }

    class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvGoalIcon: TextView = itemView.findViewById(R.id.tvGoalIcon)
        private val tvGoalTitle: TextView = itemView.findViewById(R.id.tvGoalTitle)
        private val tvProgress: TextView = itemView.findViewById(R.id.tvProgress)
        private val tvRecurrence: TextView = itemView.findViewById(R.id.tvRecurrence)

        fun bind(
            goal: Goal,
            formatValue: (Double, String) -> String,
            getRecurrenceText: (GoalRecurrence) -> String,
            onGoalClick: (Goal) -> Unit,
            onGoalLongClick: (Goal) -> Unit
        ) {
            tvGoalIcon.text = goal.icon
            tvGoalTitle.text = goal.title

            val progress = if (goal.targetValue > 0) {
                ((goal.currentValue / goal.targetValue) * 100).toInt()
            } else 0
            tvProgress.text = "${formatValue(goal.currentValue, goal.unit)} / ${formatValue(goal.targetValue, goal.unit)} ($progress%)"

            tvRecurrence.text = getRecurrenceText(goal.recurrence)

            // Color code recurrence
            when (goal.recurrence) {
                GoalRecurrence.DAILY -> tvRecurrence.setTextColor(0xFF2196F3.toInt())
                GoalRecurrence.WEEKLY -> tvRecurrence.setTextColor(0xFF4CAF50.toInt())
                GoalRecurrence.MONTHLY -> tvRecurrence.setTextColor(0xFFFF9800.toInt())
                GoalRecurrence.YEARLY -> tvRecurrence.setTextColor(0xFF9C27B0.toInt())
                else -> tvRecurrence.setTextColor(0xFF757575.toInt())
            }

            itemView.setOnClickListener { onGoalClick(goal) }
            itemView.setOnLongClickListener {
                onGoalLongClick(goal)
                true
            }
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
            ::getRecurrenceText,
            onGoalClick,
            onGoalLongClick
        )
    }

    override fun getItemCount() = goals.size
}