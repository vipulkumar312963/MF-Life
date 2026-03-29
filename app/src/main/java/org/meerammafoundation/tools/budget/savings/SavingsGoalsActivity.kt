package org.meerammafoundation.tools.budget.savings

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.meerammafoundation.tools.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SavingsGoalsActivity : AppCompatActivity() {

    private lateinit var viewModel: SavingsGoalViewModel
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var backButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.savings_activity_savings_goals)

        viewModel = ViewModelProvider(this)[SavingsGoalViewModel::class.java]

        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener { finish() }

        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        fabAdd = findViewById(R.id.fabAddGoal)

        val adapter = SavingsGoalsPagerAdapter(this)
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
        val dialogView = layoutInflater.inflate(R.layout.savings_dialog_add_savings_goal, null)
        val etName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etGoalName)
        val etTarget = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etTargetAmount)
        val etCurrent = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etCurrentAmount)
        val tvDate = dialogView.findViewById<TextView>(R.id.tvTargetDate)

        var selectedDate: Long? = null

        tvDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
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

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Create Savings Goal")
            .setView(dialogView)
            .setPositiveButton("Create", null)  // Set to null to handle manually
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(android.app.Dialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val name = etName.text.toString().trim()
                val targetStr = etTarget.text.toString().trim()
                val currentStr = etCurrent.text.toString().trim()

                // Validation
                var hasError = false

                if (name.isEmpty()) {
                    etName.error = "Enter goal name"
                    hasError = true
                } else {
                    etName.error = null
                }

                if (targetStr.isEmpty()) {
                    etTarget.error = "Enter target amount"
                    hasError = true
                } else {
                    val target = targetStr.toDoubleOrNull()
                    if (target == null || target <= 0) {
                        etTarget.error = "Enter valid amount greater than 0"
                        hasError = true
                    } else {
                        etTarget.error = null
                    }
                }

                if (currentStr.isNotEmpty()) {
                    val current = currentStr.toDoubleOrNull()
                    if (current == null || current < 0) {
                        etCurrent.error = "Enter valid amount"
                        hasError = true
                    } else {
                        etCurrent.error = null
                    }
                }

                if (hasError) {
                    // Dialog stays open - don't dismiss
                    return@setOnClickListener
                }

                // All valid, create the goal
                val target = targetStr.toDouble()
                val current = if (currentStr.isNotEmpty()) currentStr.toDouble() else 0.0

                viewModel.createGoal(name, target, current, selectedDate)
                Toast.makeText(this, "Goal created", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}