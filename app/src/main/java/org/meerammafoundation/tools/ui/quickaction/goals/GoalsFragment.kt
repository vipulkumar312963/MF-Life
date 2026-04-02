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

        val adapter = GoalsPagerAdapter(this)
        viewPager.adapter = adapter

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

    private fun showAddGoalDialog() {
        val dialogView = layoutInflater.inflate(R.layout.goal_dialog_add_goal, null)
        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etGoalTitle)
        val etTarget = dialogView.findViewById<TextInputEditText>(R.id.etTargetValue)
        val etCurrent = dialogView.findViewById<TextInputEditText>(R.id.etCurrentValue)
        val spinnerType = dialogView.findViewById<Spinner>(R.id.spinnerGoalType)
        val etUnit = dialogView.findViewById<TextInputEditText>(R.id.etUnit)
        val tvDate = dialogView.findViewById<TextView>(R.id.tvTargetDate)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etDescription)

        var selectedDate: Long? = null

        // Setup goal type spinner
        val types = GoalType.values().map { it.name }
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = typeAdapter

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
                val currentStr = etCurrent.text.toString().trim()
                val typeIndex = spinnerType.selectedItemPosition
                val unit = etUnit.text.toString().trim()
                val description = etDescription.text.toString().trim()

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

                if (unit.isEmpty()) {
                    etUnit.error = "Enter unit (e.g., ₹, kg, books)"
                    hasError = true
                }

                if (currentStr.isNotEmpty()) {
                    val current = currentStr.toDoubleOrNull()
                    if (current == null || current < 0) {
                        etCurrent.error = "Enter valid amount"
                        hasError = true
                    }
                }

                if (hasError) return@setOnClickListener

                val target = targetStr.toDouble()
                val current = if (currentStr.isNotEmpty()) currentStr.toDouble() else 0.0
                val goalType = GoalType.values()[typeIndex]

                viewModel.createGoal(
                    title = title,
                    goalType = goalType,
                    targetValue = target,
                    unit = unit,
                    currentValue = current,
                    targetDate = selectedDate,
                    description = description
                )
                Toast.makeText(requireContext(), "Goal created", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}