package org.meerammafoundation.tools.financial.calculators

import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import org.meerammafoundation.tools.R
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow

class PFPPFCalculatorActivity : AppCompatActivity() {

    private lateinit var radioGroup: RadioGroup
    private lateinit var ppfFields: LinearLayout
    private lateinit var pfFields: LinearLayout
    private lateinit var etPPFContribution: EditText
    private lateinit var etPFContribution: EditText
    private lateinit var etYears: EditText
    private lateinit var etInterestRate: EditText
    private lateinit var btnCalculate: Button
    private lateinit var resultCard: CardView
    private lateinit var tvMaturity: TextView
    private lateinit var tvTotalInvested: TextView
    private lateinit var tvInterest: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.financial_activity_pfppf_calculator)

        // Back button
        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        // Initialize views
        radioGroup = findViewById(R.id.radioGroupType)
        ppfFields = findViewById(R.id.ppfFields)
        pfFields = findViewById(R.id.pfFields)
        etPPFContribution = findViewById(R.id.etPPFContribution)
        etPFContribution = findViewById(R.id.etPFContribution)
        etYears = findViewById(R.id.etYears)
        etInterestRate = findViewById(R.id.etInterestRate)
        btnCalculate = findViewById(R.id.btnCalculate)
        resultCard = findViewById(R.id.resultCard)
        tvMaturity = findViewById(R.id.tvMaturity)
        tvTotalInvested = findViewById(R.id.tvTotalInvested)
        tvInterest = findViewById(R.id.tvInterest)

        // Radio group listener to switch between PPF and PF fields
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radioPPF) {
                ppfFields.visibility = LinearLayout.VISIBLE
                pfFields.visibility = LinearLayout.GONE
            } else {
                ppfFields.visibility = LinearLayout.GONE
                pfFields.visibility = LinearLayout.VISIBLE
            }
        }

        btnCalculate.setOnClickListener { calculate() }
    }

    private fun calculate() {
        val yearsStr = etYears.text.toString()
        val rateStr = etInterestRate.text.toString()

        if (TextUtils.isEmpty(yearsStr) || TextUtils.isEmpty(rateStr)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val years = yearsStr.toIntOrNull()
        val rate = rateStr.toDoubleOrNull()

        if (years == null || rate == null) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            return
        }

        if (years <= 0 || rate < 0) {
            Toast.makeText(this, "Years must be >0 and rate >=0", Toast.LENGTH_SHORT).show()
            return
        }

        val isPPF = radioGroup.checkedRadioButtonId == R.id.radioPPF
        val monthlyRate = rate / 12 / 100
        val annualRate = rate / 100

        val maturity: Double
        val totalInvested: Double

        if (isPPF) {
            // PPF: yearly contribution, annual compounding
            val contributionStr = etPPFContribution.text.toString()
            if (TextUtils.isEmpty(contributionStr)) {
                Toast.makeText(this, "Please enter annual contribution", Toast.LENGTH_SHORT).show()
                return
            }
            val annualContribution = contributionStr.toDoubleOrNull()
            if (annualContribution == null || annualContribution <= 0) {
                Toast.makeText(this, "Please enter a valid positive amount", Toast.LENGTH_SHORT).show()
                return
            }

            // Future value of a series: FV = P * ((1+r)^n - 1) / r * (1+r) if invested at beginning of year
            // We assume investment at the beginning of each year.
            if (annualRate == 0.0) {
                maturity = annualContribution * years
            } else {
                maturity = annualContribution * ((1 + annualRate).pow(years) - 1) / annualRate * (1 + annualRate)
            }
            totalInvested = annualContribution * years
        } else {
            // PF: monthly contribution, monthly compounding, yearly interest rate applied monthly
            val contributionStr = etPFContribution.text.toString()
            if (TextUtils.isEmpty(contributionStr)) {
                Toast.makeText(this, "Please enter monthly contribution", Toast.LENGTH_SHORT).show()
                return
            }
            val monthlyContribution = contributionStr.toDoubleOrNull()
            if (monthlyContribution == null || monthlyContribution <= 0) {
                Toast.makeText(this, "Please enter a valid positive amount", Toast.LENGTH_SHORT).show()
                return
            }

            val months = years * 12
            if (monthlyRate == 0.0) {
                maturity = monthlyContribution * months
            } else {
                // Future value of monthly investments with monthly compounding
                maturity = monthlyContribution * ((1 + monthlyRate).pow(months) - 1) / monthlyRate * (1 + monthlyRate)
            }
            totalInvested = monthlyContribution * months
        }

        val interest = maturity - totalInvested
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

        tvMaturity.text = format.format(maturity)
        tvTotalInvested.text = "Total Invested: ${format.format(totalInvested)}"
        tvInterest.text = "Interest Earned: ${format.format(interest)}"

        resultCard.visibility = CardView.VISIBLE
    }
}