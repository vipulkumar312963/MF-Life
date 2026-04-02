package org.meerammafoundation.tools.budget

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import org.meerammafoundation.tools.AboutActivity
import org.meerammafoundation.tools.MainActivity
import org.meerammafoundation.tools.R

class BudgetToolsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.budget_activity_budget_tools)

        // Find back button
        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }

        // Find budget tool cards (only keep budget-related cards)
        val cardMonthlyBudget = findViewById<CardView>(R.id.cardBudgetMonthly)
        val cardAnnualBudget = findViewById<CardView>(R.id.cardBudgetAnnual)
        val cardExpenseTracker = findViewById<CardView>(R.id.cardBudgetExpense)
        val cardIncomeExpense = findViewById<CardView>(R.id.cardBudgetIncomeExpense)
        val cardDebtPayoff = findViewById<CardView>(R.id.cardBudgetDebtPayoff)

        // REMOVED: cardBillSplitter, cardSavingsGoals, cardBillReminder
        // These are now only in Quick Actions on Main Menu

        // Set click listeners for budget tools only
        cardMonthlyBudget.setOnClickListener {
            val intent = Intent(this, MonthlyBudgetActivity::class.java)
            startActivity(intent)
        }

        cardAnnualBudget.setOnClickListener {
            val intent = Intent(this, AnnualBudgetActivity::class.java)
            startActivity(intent)
        }

        cardExpenseTracker.setOnClickListener {
            val intent = Intent(this, ExpenseTrackerActivity::class.java)
            startActivity(intent)
        }

        cardIncomeExpense.setOnClickListener {
            val intent = Intent(this, IncomeVsExpensesActivity::class.java)
            startActivity(intent)
        }

        cardDebtPayoff.setOnClickListener {
            val intent = Intent(this, DebtPayoffActivity::class.java)
            startActivity(intent)
        }

        // Bottom navigation
        val bottomHome = findViewById<TextView>(R.id.bottomHome)
        val bottomAbout = findViewById<TextView>(R.id.bottomAbout)
        val bottomUpdate = findViewById<TextView>(R.id.bottomUpdate)

        bottomHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        bottomAbout.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        bottomUpdate.setOnClickListener {
            Toast.makeText(this, "Update screen - Coming Soon!", Toast.LENGTH_LONG).show()
        }
    }
}