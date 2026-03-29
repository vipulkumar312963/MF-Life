package org.meerammafoundation.tools.budget.savings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.meerammafoundation.tools.R

class CompletedGoalsFragment : Fragment() {

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

        // ✅ Clean and simple - adapter handles button visibility
        adapter = SavingsGoalAdapter(
            emptyList(),
            { _ -> },  // Add - won't be shown for completed goals (adapter hides it)
            { _ -> },  // Withdraw - won't be shown for completed goals (adapter hides it)
            { _ -> },  // Edit - won't be shown for completed goals (adapter hides it)
            { goal -> showDeleteDialog(goal) }
        )
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(requireActivity())[SavingsGoalViewModel::class.java]

        viewModel.completedGoals.observe(viewLifecycleOwner) { goals ->
            adapter.updateData(goals)
        }

        return view
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