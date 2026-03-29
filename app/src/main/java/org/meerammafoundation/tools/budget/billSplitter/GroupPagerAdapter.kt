package org.meerammafoundation.tools.budget.billSplitter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class GroupPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val groupId: Long
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> MembersFragment.newInstance(groupId)
            1 -> BillsFragment.newInstance(groupId)
            2 -> BalancesFragment.newInstance(groupId)
            else -> throw IllegalStateException("Invalid position $position")
        }
    }

    // ✅ Prevent fragment reuse bugs after rotation / process death
    override fun getItemId(position: Int): Long {
        return position.toLong()  // Stable ID based on position
    }

    override fun containsItem(itemId: Long): Boolean {
        return itemId in 0..2  // Check if ID is valid
    }
}