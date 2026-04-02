package org.meerammafoundation.tools.ui.quickaction.billSplitter

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.meerammafoundation.tools.MainActivity
import org.meerammafoundation.tools.R

class GroupDetailFragment : Fragment() {

    private lateinit var viewModel: BillSplitterViewModel
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var pageCallback: ViewPager2.OnPageChangeCallback
    private var groupId: Long = 0
    private var groupName: String = ""

    companion object {
        private const val ARG_GROUP_ID = "group_id"
        private const val ARG_GROUP_NAME = "group_name"
        private const val TAG = "GroupDetailFragment"

        fun newInstance(groupId: Long, groupName: String): GroupDetailFragment {
            return GroupDetailFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_GROUP_ID, groupId)
                    putString(ARG_GROUP_NAME, groupName)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            groupId = it.getLong(ARG_GROUP_ID, -1)
            groupName = it.getString(ARG_GROUP_NAME, "Group") ?: "Group"
        }

        Log.d(TAG, "Opening group detail - ID: $groupId, Name: $groupName")

        if (groupId == -1L) {
            Log.e(TAG, "Invalid group ID received")
            Toast.makeText(requireContext(), "Error: Invalid group", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.billsplitter_fragment_group_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            // Initialize ViewModel (shared with BillSplitterFragment)
            viewModel = ViewModelProvider(requireActivity())[BillSplitterViewModel::class.java]

            // ✅ Update header in MainActivity (this is the only title)
            (activity as? MainActivity)?.setHeaderTitle(groupName)

            // Initialize views
            tabLayout = view.findViewById(R.id.tabLayout)
            viewPager = view.findViewById(R.id.viewPager)
            fabAdd = view.findViewById(R.id.fabAdd)

            // Observe group data for updates (if group name changes from rename)
            viewModel.getGroupById(groupId).observe(viewLifecycleOwner) { group ->
                if (group == null) {
                    Log.d(TAG, "Group deleted, closing fragment")
                    Toast.makeText(requireContext(), "Group deleted", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    // Update header when group name changes
                    if (groupName != group.name) {
                        groupName = group.name
                        (activity as? MainActivity)?.setHeaderTitle(group.name)
                    }
                }
            }

            // Create adapter with the fragment itself
            val adapter = GroupPagerAdapter(this, groupId)
            viewPager.adapter = adapter

            // Setup page callback
            pageCallback = object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    // Show FAB only for Members and Bills tabs (not for Balances)
                    fabAdd.visibility = if (position <= 1) View.VISIBLE else View.GONE
                    Log.d(TAG, "Tab changed to position: $position")
                }
            }
            viewPager.registerOnPageChangeCallback(pageCallback)

            // Connect TabLayout with ViewPager
            TabLayoutMediator(tabLayout, viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> "Members"
                    1 -> "Bills"
                    2 -> "Balances"
                    else -> ""
                }
            }.attach()

            // Set initial FAB state
            fabAdd.visibility = View.VISIBLE

            Log.d(TAG, "onViewCreated completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated: ${e.message}", e)
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // Safer FAB listener with null safety (can be called by child fragments)
    fun setFabClickListener(listener: View.OnClickListener?) {
        fabAdd.setOnClickListener(null)
        if (listener != null) {
            fabAdd.setOnClickListener(listener)
        }
    }

    // Show FAB based on current tab
    fun showFab() {
        try {
            val pos = viewPager.currentItem
            fabAdd.visibility = if (pos <= 1) View.VISIBLE else View.GONE
        } catch (e: Exception) {
            fabAdd.visibility = View.VISIBLE
        }
    }

    fun hideFab() {
        fabAdd.visibility = View.GONE
    }

    // Clean up callback to prevent memory leaks
    override fun onDestroyView() {
        super.onDestroyView()
        try {
            viewPager.unregisterOnPageChangeCallback(pageCallback)
        } catch (e: Exception) {
            Log.e(TAG, "Error unregistering callback", e)
        }
        Log.d(TAG, "onDestroyView - Page callback unregistered")
    }
}