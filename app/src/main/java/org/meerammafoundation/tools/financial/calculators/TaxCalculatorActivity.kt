package org.meerammafoundation.tools.financial.calculators

import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import org.meerammafoundation.tools.R
import java.text.NumberFormat
import java.util.Locale

class TaxCalculatorActivity : AppCompatActivity() {

    private lateinit var radioGroup: RadioGroup
    private lateinit var deductionsSection: LinearLayout
    private lateinit var etIncome: EditText
    private lateinit var et80C: EditText
    private lateinit var et80D: EditText
    private lateinit var etHRA: EditText
    private lateinit var btnCalculate: Button
    private lateinit var resultCard: CardView
    private lateinit var tvTaxableIncome: TextView
    private lateinit var tvTax: TextView
    private lateinit var tvCess: TextView
    private lateinit var tvTotalTax: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.financial_activity_tax_calculator)

        // Back button
        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        // Initialize views
        radioGroup = findViewById(R.id.radioGroupRegime)
        deductionsSection = findViewById(R.id.deductionsSection)
        etIncome = findViewById(R.id.etIncome)
        et80C = findViewById(R.id.et80C)
        et80D = findViewById(R.id.et80D)
        etHRA = findViewById(R.id.etHRA)
        btnCalculate = findViewById(R.id.btnCalculate)
        resultCard = findViewById(R.id.resultCard)
        tvTaxableIncome = findViewById(R.id.tvTaxableIncome)
        tvTax = findViewById(R.id.tvTax)
        tvCess = findViewById(R.id.tvCess)
        tvTotalTax = findViewById(R.id.tvTotalTax)

        // Show/hide deductions based on regime selection
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radioOld) {
                deductionsSection.visibility = LinearLayout.VISIBLE
            } else {
                deductionsSection.visibility = LinearLayout.GONE
            }
        }

        btnCalculate.setOnClickListener { calculateTax() }
    }

    private fun calculateTax() {
        val incomeStr = etIncome.text.toString()
        if (TextUtils.isEmpty(incomeStr)) {
            Toast.makeText(this, "Please enter annual income", Toast.LENGTH_SHORT).show()
            return
        }

        val income = incomeStr.toDoubleOrNull()
        if (income == null || income < 0) {
            Toast.makeText(this, "Please enter a valid income", Toast.LENGTH_SHORT).show()
            return
        }

        val isOldRegime = radioGroup.checkedRadioButtonId == R.id.radioOld

        var taxableIncome = income

        if (isOldRegime) {
            // Parse deductions (default 0 if empty)
            val deduction80C = et80C.text.toString().toDoubleOrNull() ?: 0.0
            val deduction80D = et80D.text.toString().toDoubleOrNull() ?: 0.0
            val hra = etHRA.text.toString().toDoubleOrNull() ?: 0.0

            // Simple cap for 80C (max 1.5L) – you can add more rules
            val capped80C = if (deduction80C > 150000) 150000.0 else deduction80C
            val capped80D = if (deduction80D > 25000) 25000.0 else deduction80D // for self, <60 yrs

            taxableIncome = income - capped80C - capped80D - hra
            if (taxableIncome < 0) taxableIncome = 0.0
        }

        // Calculate tax based on slabs (example: Indian old regime for individual <60)
        val tax = when {
            isOldRegime -> calculateOldRegimeTax(taxableIncome)
            else -> calculateNewRegimeTax(taxableIncome)
        }

        val cess = tax * 0.04
        val totalTax = tax + cess

        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

        tvTaxableIncome.text = format.format(taxableIncome)
        tvTax.text = format.format(tax)
        tvCess.text = "Health & Education Cess: ${format.format(cess)}"
        tvTotalTax.text = "Total Tax: ${format.format(totalTax)}"

        resultCard.visibility = CardView.VISIBLE
    }

    private fun calculateOldRegimeTax(income: Double): Double {
        return when {
            income <= 250000 -> 0.0
            income <= 500000 -> (income - 250000) * 0.05
            income <= 1000000 -> 250000 * 0.05 + (income - 500000) * 0.2
            else -> 250000 * 0.05 + 500000 * 0.2 + (income - 1000000) * 0.3
        }
    }

    private fun calculateNewRegimeTax(income: Double): Double {
        // New regime slabs (FY 2023-24)
        return when {
            income <= 300000 -> 0.0
            income <= 600000 -> (income - 300000) * 0.05
            income <= 900000 -> 300000 * 0.05 + (income - 600000) * 0.1
            income <= 1200000 -> 300000 * 0.05 + 300000 * 0.1 + (income - 900000) * 0.15
            income <= 1500000 -> 300000 * 0.05 + 300000 * 0.1 + 300000 * 0.15 + (income - 1200000) * 0.2
            else -> 300000 * 0.05 + 300000 * 0.1 + 300000 * 0.15 + 300000 * 0.2 + (income - 1500000) * 0.3
        }
    }
}