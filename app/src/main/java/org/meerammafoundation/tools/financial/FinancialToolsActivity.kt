package org.meerammafoundation.tools.financial

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import org.meerammafoundation.tools.AboutActivity
import org.meerammafoundation.tools.R
import org.meerammafoundation.tools.financial.calculators.EMICalculatorActivity
import org.meerammafoundation.tools.financial.calculators.GratuityCalculatorActivity
import org.meerammafoundation.tools.financial.calculators.InflationCalculatorActivity
import org.meerammafoundation.tools.financial.calculators.LoanEligibilityActivity
import org.meerammafoundation.tools.financial.calculators.MutualFundCalculatorActivity
import org.meerammafoundation.tools.financial.calculators.NetWorthCalculatorActivity
import org.meerammafoundation.tools.financial.calculators.PFPPFCalculatorActivity
import org.meerammafoundation.tools.financial.calculators.RDFDCalculatorActivity
import org.meerammafoundation.tools.financial.calculators.ROICalculatorActivity
import org.meerammafoundation.tools.financial.calculators.RetirementPlannerActivity
import org.meerammafoundation.tools.financial.calculators.SIPCalculatorActivity
import org.meerammafoundation.tools.financial.calculators.TaxCalculatorActivity

class FinancialToolsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.financial_activity_financial_tools)

        // Find back button
        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener {
            finish() // Go back to main screen
        }

        // Find all tool cards
        val cardEMI = findViewById<CardView>(R.id.cardEMI)
        val cardLoanEligibility = findViewById<CardView>(R.id.cardLoanEligibility)
        val cardROI = findViewById<CardView>(R.id.cardROI)
        val cardInflation = findViewById<CardView>(R.id.cardInflation)
        val cardPF = findViewById<CardView>(R.id.cardPF)
        val cardRDFD = findViewById<CardView>(R.id.cardRDFD)
        val cardMutualFund = findViewById<CardView>(R.id.cardMutualFund)
        val cardSIP = findViewById<CardView>(R.id.cardSIP)
        val cardRetirement = findViewById<CardView>(R.id.cardRetirement)
        val cardTax = findViewById<CardView>(R.id.cardTax)
        val cardGratuity = findViewById<CardView>(R.id.cardGratuity)
        val cardNetWorth = findViewById<CardView>(R.id.cardNetWorth)
        // Find bottom navigation items
        val bottomHome = findViewById<TextView>(R.id.bottomHome)
        val bottomAbout = findViewById<TextView>(R.id.bottomAbout)
        val bottomUpdate = findViewById<TextView>(R.id.bottomUpdate)

        // Set click listeners (all showing Coming Soon for now)
        cardEMI.setOnClickListener {
            val intent = Intent(this, EMICalculatorActivity::class.java)
            startActivity(intent)
        }

        cardLoanEligibility.setOnClickListener {
            val intent = Intent(this, LoanEligibilityActivity::class.java)
            startActivity(intent)
        }

        cardROI.setOnClickListener {
            val intent = Intent(this, ROICalculatorActivity::class.java)
            startActivity(intent)
        }

        cardInflation.setOnClickListener {
            val intent = Intent(this, InflationCalculatorActivity::class.java)
            startActivity(intent)
        }

        cardPF.setOnClickListener {
            val intent = Intent(this, PFPPFCalculatorActivity::class.java)
            startActivity(intent)
        }

        cardRDFD.setOnClickListener {
            val intent = Intent(this, RDFDCalculatorActivity::class.java)
            startActivity(intent)
        }

        cardMutualFund.setOnClickListener {
            val intent = Intent(this, MutualFundCalculatorActivity::class.java)
            startActivity(intent)
        }

        cardSIP.setOnClickListener {
            val intent = Intent(this, SIPCalculatorActivity::class.java)
            startActivity(intent)
        }

        cardRetirement.setOnClickListener {
            val intent = Intent(this, RetirementPlannerActivity::class.java)
            startActivity(intent)
        }

        cardTax.setOnClickListener {
            val intent = Intent(this, TaxCalculatorActivity::class.java)
            startActivity(intent)
        }

        cardGratuity.setOnClickListener {
            val intent = Intent(this, GratuityCalculatorActivity::class.java)
            startActivity(intent)
        }

        cardNetWorth.setOnClickListener {
            val intent = Intent(this, NetWorthCalculatorActivity::class.java)
            startActivity(intent)
        }
        // Set click listeners
        bottomHome.setOnClickListener {
            finish() // Go back to main screen (Home)
        }

        bottomAbout.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        bottomUpdate.setOnClickListener {
            Toast.makeText(this, "Update screen - Coming Soon!", Toast.LENGTH_SHORT).show()
        }
    }
}