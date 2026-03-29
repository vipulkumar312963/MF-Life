package org.meerammafoundation.tools.budget.billSplitter

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.meerammafoundation.tools.R

class GroupDetailActivity : AppCompatActivity() {

    private lateinit var viewModel: BillSplitterViewModel
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var tvGroupName: TextView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var ivSettings: ImageView
    private lateinit var backButton: ImageView
    private lateinit var pageCallback: ViewPager2.OnPageChangeCallback
    private var groupId: Long = 0

    companion object {
        const val EXTRA_GROUP_ID = "extra_group_id"
        const val EXTRA_GROUP_NAME = "extra_group_name"
        private const val TAG = "GroupDetailActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.billsplitter_activity_group_detail)

        // Get group data from intent
        groupId = intent.getLongExtra(EXTRA_GROUP_ID, -1)
        val groupName = intent.getStringExtra(EXTRA_GROUP_NAME) ?: "Group"

        Log.d(TAG, "Opening group detail - ID: $groupId, Name: $groupName")

        if (groupId == -1L) {
            Log.e(TAG, "Invalid group ID received")
            Toast.makeText(this, "Error: Invalid group", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        try {
            // Initialize ViewModel
            viewModel = ViewModelProvider(this)[BillSplitterViewModel::class.java]

            // ✅ Initialize views FIRST (before observer)
            backButton = findViewById(R.id.backButton)
            tvGroupName = findViewById(R.id.tvGroupName)
            ivSettings = findViewById(R.id.ivSettings)
            tabLayout = findViewById(R.id.tabLayout)
            viewPager = findViewById(R.id.viewPager)
            fabAdd = findViewById(R.id.fabAdd)

            // Set group name
            tvGroupName.text = groupName

            // ✅ NOW safe to observe (views are initialized)
            viewModel.getGroupById(groupId).observe(this) { group ->
                if (group == null) {
                    // Group was deleted, close this activity
                    Log.d(TAG, "Group deleted, closing activity")
                    Toast.makeText(this, "Group deleted", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    // ✅ Fixed: compare strings properly
                    if (tvGroupName.text.toString() != group.name) {
                        tvGroupName.text = group.name
                    }
                }
            }

            // Back button click listener
            backButton.setOnClickListener { finish() }

            // Settings icon click listener
            ivSettings.setOnClickListener {
                val intent = Intent(this, GroupSettingsActivity::class.java).apply {
                    putExtra(GroupSettingsActivity.EXTRA_GROUP_ID, groupId)
                    putExtra(GroupSettingsActivity.EXTRA_GROUP_NAME, groupName)
                }
                startActivity(intent)
            }

            // Create adapter with fragments
            val adapter = GroupPagerAdapter(this, groupId)
            viewPager.adapter = adapter

            // Setup page callback with leak prevention
            pageCallback = object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    fabAdd.visibility = if (position <= 1) View.VISIBLE else View.GONE
                    Log.d(TAG, "Tab changed to position: $position, FAB visibility: ${fabAdd.visibility}")
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

            // Set initial FAB state based on current tab (0 = Members)
            showFab()

            Log.d(TAG, "onCreate completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error in onCreate: ${e.message}", e)
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    // Safer FAB listener with null safety
    fun setFabClickListener(listener: View.OnClickListener?) {
        fabAdd.setOnClickListener(null)  // Clear old listener first
        if (listener != null) {
            fabAdd.setOnClickListener(listener)
        }
    }

    // Show FAB based on current tab
    fun showFab() {
        val pos = viewPager.currentItem
        fabAdd.visibility = if (pos <= 1) View.VISIBLE else View.GONE
    }

    fun hideFab() {
        fabAdd.visibility = View.GONE
    }

    // Clean up callback to prevent memory leaks
    override fun onDestroy() {
        super.onDestroy()
        viewPager.unregisterOnPageChangeCallback(pageCallback)
        Log.d(TAG, "onDestroy - Page callback unregistered")
    }
}