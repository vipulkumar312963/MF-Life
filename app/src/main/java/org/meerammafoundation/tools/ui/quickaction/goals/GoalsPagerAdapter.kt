package org.meerammafoundation.tools.ui.quickaction.goals

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class GoalsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ActiveGoalsFragment()
            1 -> CompletedGoalsFragment()
            else -> throw IllegalStateException("Invalid position")
        }
    }
}