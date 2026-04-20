package org.meerammafoundation.tools.ui.quickaction.goals

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import java.util.*
import kotlin.math.abs

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
            { goal -> showGoalOptionsDialog(goal) },
            { goal -> showGoalOptionsDialog(goal) }
        )
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(requireActivity())[GoalViewModel::class.java]

        return view
    }

    override fun onResume() {
        super.onResume()
        loadGoals()
        viewModel.regenerateRecurringGoalsIfNeeded()
    }

    private fun loadGoals() {
        viewModel.activeGoals.observe(viewLifecycleOwner) { goals ->
            val activeGoals = goals.filter { !it.isCompleted }
            val sortedGoals = activeGoals.sortedWith(
                compareBy<Goal> { goal ->
                    when (goal.recurrence) {
                        GoalRecurrence.DAILY -> 0
                        GoalRecurrence.WEEKLY -> 1
                        GoalRecurrence.MONTHLY -> 2
                        GoalRecurrence.YEARLY -> 3
                        GoalRecurrence.ONE_TIME -> 4
                        GoalRecurrence.CUSTOM -> 5
                    }
                }.thenBy { it.targetDate ?: Long.MAX_VALUE }
            )
            adapter.updateData(sortedGoals)
        }
    }

    fun refresh() {
        loadGoals()
    }

    private fun showGoalOptionsDialog(goal: Goal) {
        val options = mutableListOf<String>()
        val actions = mutableListOf<() -> Unit>()

        options.add("➕ Add Progress")
        actions.add { showAddProgressDialog(goal) }

        options.add("✏️ Edit Goal")
        actions.add { showEditDialog(goal) }

        options.add("🗑️ Delete Goal")
        actions.add { showDeleteDialog(goal) }

        if (!goal.isCompleted) {
            options.add("✅ Mark as Completed")
            actions.add { showMarkCompleteDialog(goal) }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(goal.title)
            .setItems(options.toTypedArray()) { _, which ->
                actions[which].invoke()
            }
            .setNegativeButton("Cancel", null)
            .show()
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
                        Handler(Looper.getMainLooper()).postDelayed({
                            refresh()
                            (parentFragment as? GoalsFragment)?.refreshCompletedGoals()
                        }, 300)
                    } else {
                        Toast.makeText(requireContext(), "Enter valid amount", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showMarkCompleteDialog(goal: Goal) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Complete Goal")
            .setMessage("Mark '${goal.title}' as completed?")
            .setPositiveButton("Yes") { _, _ ->
                val remaining = goal.targetValue - goal.currentValue
                if (remaining > 0) {
                    viewModel.addProgress(goal.id, remaining)
                } else {
                    viewModel.markGoalAsCompleted(goal.id)
                }
                Toast.makeText(requireContext(), "Goal completed! 🎉", Toast.LENGTH_SHORT).show()

                Handler(Looper.getMainLooper()).postDelayed({
                    refresh()
                    (parentFragment as? GoalsFragment)?.refreshCompletedGoals()
                    viewModel.regenerateRecurringGoalsIfNeeded()
                }, 500)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun showEditDialog(goal: Goal) {
        // Use the same layout as add goal
        val dialogView = layoutInflater.inflate(R.layout.goal_dialog_add_goal, null)

        // Now all the views exist with correct IDs
        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etGoalTitle)
        val etTarget = dialogView.findViewById<TextInputEditText>(R.id.etTargetValue)
        val spinnerUnit = dialogView.findViewById<Spinner>(R.id.spinnerUnit)
        val spinnerRecurrence = dialogView.findViewById<Spinner>(R.id.spinnerRecurrence)
        val layoutCustomDuration = dialogView.findViewById<LinearLayout>(R.id.layoutCustomDuration)
        val etCustomDuration = dialogView.findViewById<TextInputEditText>(R.id.etCustomDuration)
        val tvDate = dialogView.findViewById<TextView>(R.id.tvTargetDate)

        // Pre-fill with existing goal data
        val units = arrayOf("₹ (Rupees)", "kg (Kilograms)", "books", "hours", "km (Kilometers)", "days", "% (Percentage)")
        val unitValues = arrayOf("₹", "kg", "books", "hours", "km", "days", "%")
        val unitAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, units)
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUnit.adapter = unitAdapter

        val currentUnitIndex = unitValues.indexOf(goal.unit)
        if (currentUnitIndex >= 0) {
            spinnerUnit.setSelection(currentUnitIndex)
        }

        etTitle.setText(goal.title)
        etTarget.setText(goal.targetValue.toString())

        val recurrences = arrayOf("One time", "Daily", "Weekly", "Monthly", "Yearly", "Custom (days)")
        val recurrenceValues = arrayOf(
            GoalRecurrence.ONE_TIME, GoalRecurrence.DAILY, GoalRecurrence.WEEKLY,
            GoalRecurrence.MONTHLY, GoalRecurrence.YEARLY, GoalRecurrence.CUSTOM
        )
        val recurrenceAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, recurrences)
        recurrenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRecurrence.adapter = recurrenceAdapter
        spinnerRecurrence.setSelection(recurrenceValues.indexOf(goal.recurrence))

        if (goal.recurrence == GoalRecurrence.CUSTOM) {
            layoutCustomDuration.visibility = View.VISIBLE
            etCustomDuration.setText(goal.customDurationDays?.toString())
        }

        spinnerRecurrence.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                layoutCustomDuration.visibility = if (recurrenceValues[position] == GoalRecurrence.CUSTOM) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        var selectedDate = goal.targetDate
        if (selectedDate != null) {
            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            tvDate.text = dateFormat.format(selectedDate)
            tvDate.setTextColor(resources.getColor(R.color.text_primary, null))
        } else {
            tvDate.text = "Select target date"
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

        val editDialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Goal")
            .setView(dialogView)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()

        editDialog.setOnShowListener {
            val positiveButton = editDialog.getButton(Dialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                // Same validation as in add goal
                val title = etTitle.text.toString().trim()
                val targetStr = etTarget.text.toString().trim()
                val unitIndex = spinnerUnit.selectedItemPosition
                val unit = unitValues[unitIndex]
                val recurrenceIndex = spinnerRecurrence.selectedItemPosition
                val recurrence = recurrenceValues[recurrenceIndex]
                val customDurationStr = etCustomDuration.text.toString().trim()

                var hasError = false

                if (title.isEmpty()) {
                    etTitle.error = "Enter goal title"
                    hasError = true
                }

                if (targetStr.isEmpty()) {
                    etTarget.error = "Enter target value"
                    hasError = true
                } else {
                    val target = targetStr.toDoubleOrNull()
                    if (target == null || target <= 0) {
                        etTarget.error = "Enter valid value"
                        hasError = true
                    }
                }

                var customDurationDays: Int? = null
                if (recurrence == GoalRecurrence.CUSTOM) {
                    if (customDurationStr.isEmpty()) {
                        etCustomDuration.error = "Enter duration in days"
                        hasError = true
                    } else {
                        val days = customDurationStr.toIntOrNull()
                        if (days == null || days <= 0) {
                            etCustomDuration.error = "Enter valid number of days"
                            hasError = true
                        } else {
                            customDurationDays = days
                        }
                    }
                }

                if (selectedDate == null) {
                    Toast.makeText(requireContext(), "Please select a target date", Toast.LENGTH_SHORT).show()
                    hasError = true
                }

                if (hasError) return@setOnClickListener

                val updatedGoal = goal.copy(
                    title = title,
                    targetValue = targetStr.toDouble(),
                    unit = unit,
                    recurrence = recurrence,
                    customDurationDays = customDurationDays,
                    targetDate = selectedDate,
                    updatedAt = System.currentTimeMillis()
                )
                viewModel.updateGoal(updatedGoal)
                Toast.makeText(requireContext(), "Goal updated", Toast.LENGTH_SHORT).show()
                editDialog.dismiss()
                refresh()
                (parentFragment as? GoalsFragment)?.refreshCompletedGoals()
            }
        }

        editDialog.show()
    }

    private fun showDeleteDialog(goal: Goal) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Goal")
            .setMessage("Are you sure you want to delete '${goal.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteGoal(goal)
                Toast.makeText(requireContext(), "Goal deleted", Toast.LENGTH_SHORT).show()
                refresh()
                (parentFragment as? GoalsFragment)?.refreshCompletedGoals()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatValue(value: Double, unit: String): String {
        val numberFormat = NumberFormat.getInstance(Locale.getDefault())
        numberFormat.maximumFractionDigits = 2
        numberFormat.minimumFractionDigits = 0

        return when (unit) {
            "₹" -> NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(value)
            "%" -> "${numberFormat.format(value)}%"
            else -> {
                if (unit == "books" || unit == "days") {
                    numberFormat.format(value.toInt())
                } else {
                    "${numberFormat.format(value)} $unit"
                }
            }
        }
    }
}