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

class RetirementPlannerActivity : AppCompatActivity() {

    private lateinit var etCurrentAge: EditText
    private lateinit var etRetirementAge: EditText
    private lateinit var etLifeExpectancy: EditText
    private lateinit var etCurrentExpenses: EditText
    private lateinit var etInflation: EditText
    private lateinit var etReturnRate: EditText
    private lateinit var btnCalculate: Button
    private lateinit var resultCard: CardView
    private lateinit var tvMonthlyIncome: TextView
    private lateinit var tvCorpus: TextView
    private lateinit var tvMonthlySavings: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.financial_activity_retirement_planner)

        // Back button
        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        // Initialize views
        etCurrentAge = findViewById(R.id.etCurrentAge)
        etRetirementAge = findViewById(R.id.etRetirementAge)
        etLifeExpectancy = findViewById(R.id.etLifeExpectancy)
        etCurrentExpenses = findViewById(R.id.etCurrentExpenses)
        etInflation = findViewById(R.id.etInflation)
        etReturnRate = findViewById(R.id.etReturnRate)
        btnCalculate = findViewById(R.id.btnCalculate)
        resultCard = findViewById(R.id.resultCard)
        tvMonthlyIncome = findViewById(R.id.tvMonthlyIncome)
        tvCorpus = findViewById(R.id.tvCorpus)
        tvMonthlySavings = findViewById(R.id.tvMonthlySavings)

        btnCalculate.setOnClickListener { calculate() }
    }

    private fun calculate() {
        val currentAgeStr = etCurrentAge.text.toString()
        val retirementAgeStr = etRetirementAge.text.toString()
        val lifeExpStr = etLifeExpectancy.text.toString()
        val expensesStr = etCurrentExpenses.text.toString()
        val inflationStr = etInflation.text.toString()
        val returnStr = etReturnRate.text.toString()

        if (TextUtils.isEmpty(currentAgeStr) || TextUtils.isEmpty(retirementAgeStr) ||
            TextUtils.isEmpty(lifeExpStr) || TextUtils.isEmpty(expensesStr) ||
            TextUtils.isEmpty(inflationStr) || TextUtils.isEmpty(returnStr)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val currentAge = currentAgeStr.toIntOrNull()
        val retirementAge = retirementAgeStr.toIntOrNull()
        val lifeExpectancy = lifeExpStr.toIntOrNull()
        val currentExpenses = expensesStr.toDoubleOrNull()
        val inflation = inflationStr.toDoubleOrNull()
        val returnRate = returnStr.toDoubleOrNull()

        if (currentAge == null || retirementAge == null || lifeExpectancy == null ||
            currentExpenses == null || inflation == null || returnRate == null) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentAge < 0 || retirementAge <= currentAge || lifeExpectancy <= retirementAge ||
            currentExpenses <= 0 || inflation < 0 || returnRate < 0) {
            Toast.makeText(this, "Please check your inputs: ages must be reasonable, expenses >0, rates >=0", Toast.LENGTH_SHORT).show()
            return
        }

        val yearsToRetirement = retirementAge - currentAge
        val yearsInRetirement = lifeExpectancy - retirementAge

        // Step 1: Future monthly expenses at retirement (inflated)
        val monthlyAtRetirement = currentExpenses * (1 + inflation / 100).pow(yearsToRetirement)

        // Step 2: Total corpus needed at retirement using the 4% rule (or a simple annuity factor)
        // Annual expenses at retirement
        val annualAtRetirement = monthlyAtRetirement * 12
        // Using 4% rule -> corpus = annual expenses * 25
        val corpusNeeded = annualAtRetirement * 25

        // Step 3: Monthly savings required to accumulate corpus
        // Use SIP formula: FV = P * [((1+r)^n - 1)/r] * (1+r)
        // where P = monthly savings, r = monthly return, n = months to retirement
        val months = yearsToRetirement * 12
        val monthlyRate = returnRate / 12 / 100

        val monthlySavings = if (monthlyRate == 0.0) {
            corpusNeeded / months
        } else {
            val factor = ((1 + monthlyRate).pow(months) - 1) / monthlyRate * (1 + monthlyRate)
            corpusNeeded / factor
        }

        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

        tvMonthlyIncome.text = format.format(monthlyAtRetirement)
        tvCorpus.text = format.format(corpusNeeded)
        tvMonthlySavings.text = format.format(monthlySavings)

        resultCard.visibility = CardView.VISIBLE
    }
}