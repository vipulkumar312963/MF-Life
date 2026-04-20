package org.meerammafoundation.tools.ui.quickaction.goals

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class GoalsPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private var activeFragment: ActiveGoalsFragment? = null
    private var completedFragment: CompletedGoalsFragment? = null

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ActiveGoalsFragment().also { activeFragment = it }
            1 -> CompletedGoalsFragment().also { completedFragment = it }
            else -> throw IllegalStateException("Invalid position")
        }
    }

    fun getActiveFragment(): ActiveGoalsFragment? = activeFragment
    fun getCompletedFragment(): CompletedGoalsFragment? = completedFragment
}