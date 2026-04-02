package org.meerammafoundation.tools.ui.quickaction.billSplitter

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.meerammafoundation.tools.MainActivity
import org.meerammafoundation.tools.R

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

        // ✅ Updated with long-click listener
        adapter = GroupAdapter(
            groups = emptyList(),
            onGroupClick = { group ->
                // Click: Open group detail
                val groupDetailFragment = GroupDetailFragment.newInstance(group.id, group.name)
                (activity as? MainActivity)?.loadFragment(groupDetailFragment, group.name)
            },
            onGroupLongClick = { group ->
                // Long press: Show rename/delete options
                showGroupOptionsDialog(group)
            }
        )
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

    // ✅ New: Show options dialog on long press
    private fun showGroupOptionsDialog(group: Group) {
        val options = arrayOf("Rename", "Delete")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(group.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showRenameGroupDialog(group)
                    1 -> showDeleteGroupDialog(group)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ✅ New: Rename group dialog
    private fun showRenameGroupDialog(group: Group) {
        val dialogView = layoutInflater.inflate(R.layout.billsplitter_dialog_add_group, null)
        val etGroupName = dialogView.findViewById<EditText>(R.id.etGroupName)
        etGroupName.setText(group.name)
        etGroupName.selectAll()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Rename Group")
            .setView(dialogView)
            .setPositiveButton("Rename") { _, _ ->
                val newName = etGroupName.text.toString().trim()
                if (newName.isNotEmpty() && newName != group.name) {
                    val updatedGroup = group.copy(name = newName)
                    viewModel.updateGroup(updatedGroup)
                    Toast.makeText(requireContext(), "Group renamed to $newName", Toast.LENGTH_SHORT).show()
                } else if (newName.isEmpty()) {
                    Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ✅ New: Delete group dialog with confirmation
    private fun showDeleteGroupDialog(group: Group) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Group")
            .setMessage("Are you sure you want to delete \"${group.name}\"? This will also delete all bills and members in this group.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteGroup(group)
                Toast.makeText(requireContext(), "Group deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showAddGroupDialog() {
        val dialogView = layoutInflater.inflate(R.layout.billsplitter_dialog_add_group, null)
        val etGroupName = dialogView.findViewById<EditText>(R.id.etGroupName)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create New Group")
            .setView(dialogView)
            .setPositiveButton("Create", null)
            .setNegativeButton("Cancel", null)
            .create()
            .apply {
                setOnShowListener {
                    val input = dialogView.findViewById<EditText>(R.id.etGroupName)
                    val positiveButton = getButton(Dialog.BUTTON_POSITIVE)

                    positiveButton.setOnClickListener {
                        val groupName = input.text.toString().trim()
                        if (groupName.isNotEmpty()) {
                            viewModel.createGroup(groupName)
                            dismiss()
                        } else {
                            input.error = "Group name cannot be empty"
                        }
                    }
                }
                show()
            }
    }
}