package org.meerammafoundation.tools.ui.quickaction.goals

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
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ActiveGoalsFragment : Fragment() {

    private lateinit var viewModel: GoalViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GoalAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.goal_fragment_goals, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewGoals)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = GoalAdapter(
            emptyList(),
            { goal -> showAddProgressDialog(goal) },
            { goal -> showRemoveProgressDialog(goal) },
            { goal -> showEditDialog(goal) },
            { goal -> showDeleteDialog(goal) }
        )
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(requireActivity())[GoalViewModel::class.java]

        viewModel.activeGoals.observe(viewLifecycleOwner) { goals ->
            adapter.updateData(goals)
        }

        return view
    }

    private fun showAddProgressDialog(goal: Goal) {
        val dialogView = layoutInflater.inflate(R.layout.goal_dialog_add_progress, null)
        val etAmount = dialogView.findViewById<TextInputEditText>(R.id.etAmount)
        val tvCurrent = dialogView.findViewById<TextView>(R.id.tvCurrentValue)
        val tvTarget = dialogView.findViewById<TextView>(R.id.tvTargetValue)

        tvCurrent.text = formatValue(goal.currentValue, goal.unit)
        tvTarget.text = formatValue(goal.targetValue, goal.unit)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add to ${goal.title}")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val amountStr = etAmount.text.toString().trim()
                if (amountStr.isNotEmpty()) {
                    val amount = amountStr.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        viewModel.addProgress(goal.id, amount)
                        Toast.makeText(requireContext(), "Progress added", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Enter valid amount", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showRemoveProgressDialog(goal: Goal) {
        val dialogView = layoutInflater.inflate(R.layout.goal_dialog_remove_progress, null)
        val etAmount = dialogView.findViewById<TextInputEditText>(R.id.etAmount)
        val tvCurrent = dialogView.findViewById<TextView>(R.id.tvCurrentValue)
        val tvTarget = dialogView.findViewById<TextView>(R.id.tvTargetValue)

        tvCurrent.text = formatValue(goal.currentValue, goal.unit)
        tvTarget.text = formatValue(goal.targetValue, goal.unit)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Remove from ${goal.title}")
            .setView(dialogView)
            .setPositiveButton("Remove") { _, _ ->
                val amountStr = etAmount.text.toString().trim()
                if (amountStr.isNotEmpty()) {
                    val amount = amountStr.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        if (amount <= goal.currentValue) {
                            viewModel.removeProgress(goal.id, amount)
                            Toast.makeText(requireContext(), "Progress removed", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(requireContext(), "Cannot remove more than current progress", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Enter valid amount", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDialog(goal: Goal) {
        val dialogView = layoutInflater.inflate(R.layout.goal_dialog_edit_goal, null)
        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etGoalTitle)
        val etTarget = dialogView.findViewById<TextInputEditText>(R.id.etTargetValue)
        val spinnerType = dialogView.findViewById<Spinner>(R.id.spinnerGoalType)
        val etUnit = dialogView.findViewById<TextInputEditText>(R.id.etUnit)
        val tvDate = dialogView.findViewById<TextView>(R.id.tvTargetDate)

        etTitle.setText(goal.title)
        etTarget.setText(goal.targetValue.toString())
        etUnit.setText(goal.unit)

        // Setup goal type spinner
        val types = GoalType.values().map { it.name }
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = typeAdapter
        spinnerType.setSelection(goal.goalType.ordinal)

        var selectedDate = goal.targetDate
        if (selectedDate != null) {
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            tvDate.text = dateFormat.format(selectedDate)
            tvDate.setTextColor(resources.getColor(R.color.text_primary, null))
        }

        tvDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            selectedDate?.let { calendar.timeInMillis = it }
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val date = Calendar.getInstance()
                    date.set(year, month, dayOfMonth)
                    selectedDate = date.timeInMillis
                    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                    tvDate.text = dateFormat.format(date.time)
                    tvDate.setTextColor(resources.getColor(R.color.text_primary, null))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Goal")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = etTitle.text.toString().trim()
                val targetStr = etTarget.text.toString().trim()
                val unit = etUnit.text.toString().trim()
                val typeIndex = spinnerType.selectedItemPosition

                if (title.isNotEmpty() && targetStr.isNotEmpty() && unit.isNotEmpty()) {
                    val target = targetStr.toDoubleOrNull()
                    if (target != null && target > 0) {
                        val updatedGoal = goal.copy(
                            title = title,
                            targetValue = target,
                            unit = unit,
                            goalType = GoalType.values()[typeIndex],
                            targetDate = selectedDate,
                            updatedAt = System.currentTimeMillis()
                        )
                        viewModel.updateGoal(updatedGoal)
                        Toast.makeText(requireContext(), "Goal updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), "Enter valid target", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog(goal: Goal) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Goal")
            .setMessage("Are you sure you want to delete '${goal.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteGoal(goal)
                Toast.makeText(requireContext(), "Goal deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatValue(value: Double, unit: String): String {
        val numberFormat = NumberFormat.getInstance(Locale.getDefault())
        return when (unit) {
            "₹" -> NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(value)
            "%" -> "${numberFormat.format(value)}%"
            else -> "${numberFormat.format(value)} $unit"
        }
    }
}