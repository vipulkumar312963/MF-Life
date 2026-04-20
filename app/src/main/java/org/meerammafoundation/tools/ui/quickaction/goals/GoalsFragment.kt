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
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import org.meerammafoundation.tools.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GoalsFragment : Fragment() {

    private lateinit var viewModel: GoalViewModel
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var goalsPagerAdapter: GoalsPagerAdapter

    companion object {
        fun newInstance(): GoalsFragment {
            return GoalsFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.goal_fragment_goals_container, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[GoalViewModel::class.java]

        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)
        fabAdd = view.findViewById(R.id.fabAddGoal)

        goalsPagerAdapter = GoalsPagerAdapter(this)
        viewPager.adapter = goalsPagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Active"
                1 -> "Completed"
                else -> ""
            }
        }.attach()

        fabAdd.setOnClickListener {
            showAddGoalDialog()
        }
    }

    fun refreshCompletedGoals() {
        goalsPagerAdapter.getCompletedFragment()?.refresh()
    }

    fun refreshActiveGoals() {
        goalsPagerAdapter.getActiveFragment()?.refresh()
    }

    private fun showAddGoalDialog() {
        val dialogView = layoutInflater.inflate(R.layout.goal_dialog_add_goal, null)
        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etGoalTitle)
        val etTarget = dialogView.findViewById<TextInputEditText>(R.id.etTargetValue)
        val spinnerUnit = dialogView.findViewById<Spinner>(R.id.spinnerUnit)
        val spinnerRecurrence = dialogView.findViewById<Spinner>(R.id.spinnerRecurrence)
        val layoutCustomDuration = dialogView.findViewById<LinearLayout>(R.id.layoutCustomDuration)
        val etCustomDuration = dialogView.findViewById<TextInputEditText>(R.id.etCustomDuration)
        val tvDate = dialogView.findViewById<TextView>(R.id.tvTargetDate)

        var selectedDate: Long? = null

        // Setup Unit Spinner with predefined options
        val units = arrayOf("₹ (Rupees)", "kg (Kilograms)", "books", "hours", "km (Kilometers)", "days", "% (Percentage)")
        val unitValues = arrayOf("₹", "kg", "books", "hours", "km", "days", "%")
        val unitAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, units)
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerUnit.adapter = unitAdapter

        // Setup Recurrence Spinner
        val recurrences = arrayOf(
            "One time",
            "Daily",
            "Weekly",
            "Monthly",
            "Yearly",
            "Custom (days)"
        )
        val recurrenceValues = arrayOf(
            GoalRecurrence.ONE_TIME,
            GoalRecurrence.DAILY,
            GoalRecurrence.WEEKLY,
            GoalRecurrence.MONTHLY,
            GoalRecurrence.YEARLY,
            GoalRecurrence.CUSTOM
        )
        val recurrenceAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, recurrences)
        recurrenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRecurrence.adapter = recurrenceAdapter

        // Show/hide custom duration field based on recurrence selection
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

        // Target Date click listener
        tvDate.setOnClickListener {
            val calendar = Calendar.getInstance()
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
            .setTitle("Create New Goal")
            .setView(dialogView)
            .setPositiveButton("Create", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
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
                        etTarget.error = "Enter valid value greater than 0"
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

                val target = targetStr.toDouble()

                viewModel.createGoal(
                    title = title,
                    goalType = GoalType.CUSTOM,
                    targetValue = target,
                    unit = unit,
                    currentValue = 0.0,
                    targetDate = selectedDate,
                    description = null,
                    recurrence = recurrence,
                    customDurationDays = customDurationDays
                )
                Toast.makeText(requireContext(), "Goal created", Toast.LENGTH_SHORT).show()
                dialog.dismiss()

                // Refresh both tabs
                refreshActiveGoals()
                refreshCompletedGoals()
            }
        }

        dialog.show()
    }
}