package org.meerammafoundation.tools.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import org.meerammafoundation.tools.MainActivity
import org.meerammafoundation.tools.R
import org.meerammafoundation.tools.budget.reminder.BillReminderActivity
import org.meerammafoundation.tools.budget.savings.SavingsGoalsActivity
import org.meerammafoundation.tools.ui.budget.billSplitter.BillSplitterFragment
import java.util.Calendar

class MainMenuFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        updateGreeting(view)
        setupClickListeners(view)
    }

    private fun updateGreeting(view: View) {
        val greetingText = view.findViewById<TextView>(R.id.greetingText)
        val greetingEmoji = view.findViewById<TextView>(R.id.greetingEmoji)

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)

        // TODO: Replace with actual user progress data from database
        val completed = 2
        val total = 4

        val (emoji, greeting) = when (hour) {
            in 0..11 -> "🌅" to "Good morning!"
            in 12..16 -> "☀️" to "Good afternoon!"
            in 17..20 -> "🌤️" to "Good evening!"
            else -> "🌙" to "Good night!"
        }

        greetingEmoji.text = emoji
        greetingText.text = "$greeting You've completed $completed of $total daily goals. Keep going!"
    }

    private fun setupClickListeners(view: View) {
        // ========== QUICK ACTIONS ==========

        // Add Bill - Load Bill Splitter as Fragment
        view.findViewById<TextView>(R.id.quickAddBill)?.setOnClickListener {
            val billSplitterFragment = BillSplitterFragment()
            (activity as? MainActivity)?.loadFragment(billSplitterFragment, "Bill Splitter")
        }

        // Add Reminder - Opens Bill Reminder
        view.findViewById<TextView>(R.id.quickAddReminder)?.setOnClickListener {
            val intent = Intent(requireContext(), BillReminderActivity::class.java)
            startActivity(intent)
        }

        // Add Goal - Opens Savings Goals
        view.findViewById<TextView>(R.id.quickAddGoal)?.setOnClickListener {
            val intent = Intent(requireContext(), SavingsGoalsActivity::class.java)
            startActivity(intent)
        }

        // ========== MY ACTION ZONE ==========
        view.findViewById<CardView>(R.id.cardMyPower)?.setOnClickListener {
            (activity as? MainActivity)?.showMessage("My Power - Coming Soon!")
        }

        view.findViewById<CardView>(R.id.cardMyPeople)?.setOnClickListener {
            (activity as? MainActivity)?.showMessage("My People - Coming Soon!")
        }

        view.findViewById<CardView>(R.id.cardMyPride)?.setOnClickListener {
            (activity as? MainActivity)?.showMessage("My Pride - Coming Soon!")
        }

        view.findViewById<CardView>(R.id.cardMyFuture)?.setOnClickListener {
            (activity as? MainActivity)?.showMessage("My Future - Coming Soon!")
        }

        view.findViewById<CardView>(R.id.cardMyThoughts)?.setOnClickListener {
            (activity as? MainActivity)?.showMessage("My Thoughts - Coming Soon!")
        }

        view.findViewById<CardView>(R.id.cardMyVault)?.setOnClickListener {
            (activity as? MainActivity)?.showMessage("My Vault - Coming Soon!")
        }

        // ========== NEED HELP WITH ==========
        view.findViewById<CardView>(R.id.cardCollegeAdmission)?.setOnClickListener {
            (activity as? MainActivity)?.showMessage("College Admission Resources - Coming Soon!")
        }

        view.findViewById<CardView>(R.id.cardGettingJob)?.setOnClickListener {
            (activity as? MainActivity)?.showMessage("Job Assistance - Coming Soon!")
        }

        view.findViewById<CardView>(R.id.cardSocialNeeds)?.setOnClickListener {
            (activity as? MainActivity)?.showMessage("Social Needs Support - Coming Soon!")
        }

        // ========== SPECIAL ATTENTION CENTER ==========
        view.findViewById<CardView>(R.id.cardDopamineDetox)?.setOnClickListener {
            (activity as? MainActivity)?.showMessage("Dopamine Detox - Coming Soon!")
        }

        view.findViewById<CardView>(R.id.cardDeAddiction)?.setOnClickListener {
            (activity as? MainActivity)?.showMessage("De-Addiction Support - Coming Soon!")
        }

        view.findViewById<CardView>(R.id.cardChildCare)?.setOnClickListener {
            (activity as? MainActivity)?.showMessage("Child Care Resources - Coming Soon!")
        }
    }
}