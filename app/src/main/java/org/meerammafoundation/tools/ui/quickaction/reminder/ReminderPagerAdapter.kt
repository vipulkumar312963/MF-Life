package org.meerammafoundation.tools.ui.quickaction.reminder

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class ReminderPagerAdapter(
    fragment: Fragment
) : FragmentStateAdapter(fragment) {

    companion object {
        private const val PAGE_COUNT = 2
        private const val PAGE_UPCOMING = 0
        private const val PAGE_PAID = 1
    }

    override fun getItemCount(): Int = PAGE_COUNT

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            PAGE_UPCOMING -> UpcomingRemindersFragment.newInstance()
            PAGE_PAID -> CompletedRemindersFragment.newInstance()
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
}