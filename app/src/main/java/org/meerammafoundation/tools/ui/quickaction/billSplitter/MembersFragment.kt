package org.meerammafoundation.tools.ui.quickaction.billSplitter

import android.os.Bundle
import android.util.Log
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
import org.meerammafoundation.tools.R

class MembersFragment : Fragment() {

    private lateinit var viewModel: BillSplitterViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MemberAdapter
    private var groupId: Long = 0
    private val TAG = "MembersFragment"

    companion object {
        fun newInstance(groupId: Long): MembersFragment {
            val fragment = MembersFragment()
            val args = Bundle()
            args.putLong("groupId", groupId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = it.getLong("groupId", -1)
            Log.d(TAG, "onCreate - Group ID: $groupId")
        }
        viewModel = ViewModelProvider(requireActivity())[BillSplitterViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView - Creating view for groupId: $groupId")

        val view = inflater.inflate(R.layout.billsplitter_fragment_members, container, false)

        recyclerView = view.findViewById(R.id.recyclerViewMembers)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Observe members list
        viewModel.getMembers(groupId).observe(viewLifecycleOwner) { members ->
            Log.d(TAG, "Received ${members.size} members")
            adapter = MemberAdapter(members) { member ->
                showDeleteMemberDialog(member)
            }
            recyclerView.adapter = adapter
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        // Set FAB click listener when fragment becomes visible
        setupFabClickListener()
    }

    private fun setupFabClickListener() {
        try {
            // ✅ FIXED: Get parent fragment (GroupDetailFragment) instead of activity
            val parentFragment = parentFragment as? GroupDetailFragment

            if (parentFragment != null) {
                parentFragment.setFabClickListener {
                    Log.d(TAG, "FAB clicked in Members tab")
                    showAddMemberDialog()
                }
                parentFragment.showFab()
                Log.d(TAG, "FAB listener set in Members tab")
            } else {
                Log.e(TAG, "Parent fragment is not GroupDetailFragment")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting FAB listener", e)
        }
    }

    private fun showAddMemberDialog() {
        Log.d(TAG, "Showing add member dialog")

        val dialogView = layoutInflater.inflate(R.layout.billsplitter_dialog_add_member, null)
        val etName = dialogView.findViewById<EditText>(R.id.etMemberName)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Member")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isNotEmpty()) {
                    Log.d(TAG, "Adding member: $name to group: $groupId")
                    viewModel.addMember(groupId, name)
                    Toast.makeText(requireContext(), "Member added", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteMemberDialog(member: Member) {
        Log.d(TAG, "Showing delete dialog for member: ${member.name}")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Remove Member")
            .setMessage("Are you sure you want to remove ${member.name}?")
            .setPositiveButton("Remove") { _, _ ->
                Log.d(TAG, "Removing member: ${member.name}")
                viewModel.removeMember(member)
                Toast.makeText(requireContext(), "Member removed", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}