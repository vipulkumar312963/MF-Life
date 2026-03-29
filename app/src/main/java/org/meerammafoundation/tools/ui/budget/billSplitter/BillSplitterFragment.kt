package org.meerammafoundation.tools.ui.budget.billSplitter

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.meerammafoundation.tools.R
import org.meerammafoundation.tools.budget.billSplitter.BillSplitterViewModel
import org.meerammafoundation.tools.budget.billSplitter.GroupAdapter
import org.meerammafoundation.tools.budget.billSplitter.GroupDetailActivity

class BillSplitterFragment : Fragment() {

    private lateinit var viewModel: BillSplitterViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddGroup: FloatingActionButton
    private lateinit var adapter: GroupAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bill_splitter, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[BillSplitterViewModel::class.java]

        setupRecyclerView(view)
        setupFAB(view)
        observeGroups()
    }

    private fun setupRecyclerView(view: View) {
        recyclerView = view.findViewById(R.id.recyclerViewGroups)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = GroupAdapter(emptyList()) { group ->
            val intent = Intent(requireContext(), GroupDetailActivity::class.java).apply {
                putExtra(GroupDetailActivity.Companion.EXTRA_GROUP_ID, group.id)
                putExtra(GroupDetailActivity.Companion.EXTRA_GROUP_NAME, group.name)
            }
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    private fun setupFAB(view: View) {
        fabAddGroup = view.findViewById(R.id.fabAddGroup)
        fabAddGroup.setOnClickListener { showAddGroupDialog() }
    }

    private fun observeGroups() {
        viewModel.allGroups.observe(viewLifecycleOwner) { groups ->
            adapter.updateGroups(groups)
        }
    }

    private fun showAddGroupDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create New Group")
            .setView(R.layout.billsplitter_dialog_add_group)
            .setPositiveButton("Create", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val input = dialog.findViewById<EditText>(R.id.etGroupName) ?: return@setOnShowListener
            val positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE)

            positiveButton.setOnClickListener {
                val groupName = input.text.toString().trim()
                if (groupName.isNotEmpty()) {
                    viewModel.createGroup(groupName)
                    dialog.dismiss()
                } else {
                    input.error = "Group name cannot be empty"
                }
            }
        }

        dialog.show()
    }
}