package org.meerammafoundation.tools.budget.savings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import org.meerammafoundation.tools.R
import java.text.NumberFormat
import java.util.Locale

class ActiveGoalsFragment : Fragment() {

    private lateinit var viewModel: SavingsGoalViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SavingsGoalAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.savings_fragment_savings_goals, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewGoals)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = SavingsGoalAdapter(
            emptyList(),
            { goal -> showAddMoneyDialog(goal) },
            { goal -> showWithdrawDialog(goal) },
            { goal -> showEditDialog(goal) },
            { goal -> showDeleteDialog(goal) }
        )
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(requireActivity())[SavingsGoalViewModel::class.java]

        viewModel.activeGoals.observe(viewLifecycleOwner) { goals ->
            adapter.updateData(goals)
        }

        return view
    }

    private fun showAddMoneyDialog(goal: SavingsGoal) {
        val dialogView = layoutInflater.inflate(R.layout.savings_dialog_add_money, null)
        val etAmount = dialogView.findViewById<TextInputEditText>(R.id.etAmount)
        val tvCurrent = dialogView.findViewById<TextView>(R.id.tvCurrentAmount)
        val tvTarget = dialogView.findViewById<TextView>(R.id.tvTargetAmount)

        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        tvCurrent.text = format.format(goal.currentAmount)
        tvTarget.text = format.format(goal.targetAmount)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add to ${goal.name}")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val amountStr = etAmount.text.toString().trim()
                if (amountStr.isNotEmpty()) {
                    val amount = amountStr.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        viewModel.addToGoal(goal.id, amount)
                    } else {
                        android.widget.Toast.makeText(requireContext(), "Enter valid amount", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showWithdrawDialog(goal: SavingsGoal) {
        val dialogView = layoutInflater.inflate(R.layout.savings_dialog_withdraw_money, null)
        val etAmount = dialogView.findViewById<TextInputEditText>(R.id.etAmount)
        val tvCurrent = dialogView.findViewById<TextView>(R.id.tvCurrentAmount)
        val tvTarget = dialogView.findViewById<TextView>(R.id.tvTargetAmount)

        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        tvCurrent.text = format.format(goal.currentAmount)
        tvTarget.text = format.format(goal.targetAmount)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Withdraw from ${goal.name}")
            .setView(dialogView)
            .setPositiveButton("Withdraw") { _, _ ->
                val amountStr = etAmount.text.toString().trim()
                if (amountStr.isNotEmpty()) {
                    val amount = amountStr.toDoubleOrNull()
                    if (amount != null && amount > 0) {
                        if (amount <= goal.currentAmount) {
                            viewModel.withdrawFromGoal(goal.id, amount)
                        } else {
                            android.widget.Toast.makeText(requireContext(), "Insufficient funds", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        android.widget.Toast.makeText(requireContext(), "Enter valid amount", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDialog(goal: SavingsGoal) {
        val dialogView = layoutInflater.inflate(R.layout.savings_dialog_edit_savings_goal, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etGoalName)
        val etTarget = dialogView.findViewById<TextInputEditText>(R.id.etTargetAmount)

        etName.setText(goal.name)
        etTarget.setText(goal.targetAmount.toString())

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Goal")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                val targetStr = etTarget.text.toString().trim()
                if (name.isNotEmpty() && targetStr.isNotEmpty()) {
                    val target = targetStr.toDoubleOrNull()
                    if (target != null && target > 0) {
                        val updatedGoal = goal.copy(
                            name = name,
                            targetAmount = target,
                            updatedAt = System.currentTimeMillis()
                        )
                        viewModel.updateGoal(updatedGoal)
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog(goal: SavingsGoal) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Goal")
            .setMessage("Are you sure you want to delete '${goal.name}'?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteGoal(goal)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}