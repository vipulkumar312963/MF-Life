package org.meerammafoundation.tools.financial.calculators

import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import org.meerammafoundation.tools.R
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.pow

class LoanEligibilityActivity : AppCompatActivity() {

    private lateinit var etMonthlyIncome: EditText
    private lateinit var etExistingEMI: EditText
    private lateinit var etInterestRate: EditText
    private lateinit var etTenure: EditText
    private lateinit var btnCalculate: Button
    private lateinit var resultCard: CardView
    private lateinit var tvLoanAmount: TextView
    private lateinit var tvMonthlyEMI: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.financial_activity_loan_eligibility)

        // Back button
        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        // Initialize views
        etMonthlyIncome = findViewById(R.id.etMonthlyIncome)
        etExistingEMI = findViewById(R.id.etExistingEMI)
        etInterestRate = findViewById(R.id.etInterestRate)
        etTenure = findViewById(R.id.etTenure)
        btnCalculate = findViewById(R.id.btnCalculate)
        resultCard = findViewById(R.id.resultCard)
        tvLoanAmount = findViewById(R.id.tvLoanAmount)
        tvMonthlyEMI = findViewById(R.id.tvMonthlyEMI)

        btnCalculate.setOnClickListener { calculateEligibility() }
    }

    private fun calculateEligibility() {
        val incomeStr = etMonthlyIncome.text.toString()
        var existingStr = etExistingEMI.text.toString()
        val rateStr = etInterestRate.text.toString()
        val tenureStr = etTenure.text.toString()

        if (TextUtils.isEmpty(incomeStr) || TextUtils.isEmpty(rateStr) || TextUtils.isEmpty(tenureStr)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val monthlyIncome = incomeStr.toDoubleOrNull()
        val annualRate = rateStr.toDoubleOrNull()
        val tenureMonths = tenureStr.toIntOrNull()
        val existingEMI = if (TextUtils.isEmpty(existingStr)) 0.0 else existingStr.toDoubleOrNull() ?: 0.0

        if (monthlyIncome == null || annualRate == null || tenureMonths == null) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            return
        }

        if (monthlyIncome <= 0 || annualRate <= 0 || tenureMonths <= 0) {
            Toast.makeText(this, "Values must be greater than zero", Toast.LENGTH_SHORT).show()
            return
        }

        // Typically banks allow up to 50-60% of income for EMI, but we'll use 50% for conservative estimate
        val maxEMI = monthlyIncome * 0.5 - existingEMI
        if (maxEMI <= 0) {
            Toast.makeText(this, "Existing obligations too high – not eligible", Toast.LENGTH_SHORT).show()
            resultCard.visibility = CardView.GONE
            return
        }

        // Calculate loan amount from maxEMI using reverse EMI formula
        val monthlyRate = annualRate / 12 / 100
        val factor = (1 + monthlyRate).pow(tenureMonths)
        val loanAmount = if (monthlyRate == 0.0) {
            maxEMI * tenureMonths
        } else {
            maxEMI * (factor - 1) / (monthlyRate * factor)
        }

        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

        tvLoanAmount.text = format.format(loanAmount)
        tvMonthlyEMI.text = "Estimated EMI: ${format.format(maxEMI)}"

        resultCard.visibility = CardView.VISIBLE
    }
}