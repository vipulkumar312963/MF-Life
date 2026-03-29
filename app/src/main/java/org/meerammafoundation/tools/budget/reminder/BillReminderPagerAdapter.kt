package org.meerammafoundation.tools.budget.reminder

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class BillReminderPagerAdapter(
    fragmentActivity: FragmentActivity
) : FragmentStateAdapter(fragmentActivity) {

    companion object {
        private const val PAGE_COUNT = 2
        private const val PAGE_UPCOMING = 0
        private const val PAGE_PAID = 1
    }

    override fun getItemCount(): Int = PAGE_COUNT

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            PAGE_UPCOMING -> UpcomingBillsFragment()
            PAGE_PAID -> PaidBillsFragment()
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
}