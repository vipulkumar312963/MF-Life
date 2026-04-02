package org.meerammafoundation.tools.ui.quickaction.goals

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.meerammafoundation.tools.R

class CompletedGoalsFragment : Fragment() {

    private lateinit var viewModel: GoalViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GoalAdapter

    companion object {
        fun newInstance(): CompletedGoalsFragment {
            return CompletedGoalsFragment()
        }
    }

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
            { _ -> },  // Add - hidden for completed goals
            { _ -> },  // Remove - hidden for completed goals
            { _ -> },  // Edit - hidden for completed goals
            { goal -> showDeleteDialog(goal) }
        )
        recyclerView.adapter = adapter

        viewModel = ViewModelProvider(requireActivity())[GoalViewModel::class.java]

        viewModel.completedGoals.observe(viewLifecycleOwner) { goals ->
            adapter.updateData(goals)
        }

        return view
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
}