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

class RDFDCalculatorActivity : AppCompatActivity() {

    private lateinit var radioGroup: RadioGroup
    private lateinit var fdFields: LinearLayout
    private lateinit var rdFields: LinearLayout
    private lateinit var etPrincipal: EditText
    private lateinit var etMonthlyInstallment: EditText
    private lateinit var etInterestRate: EditText
    private lateinit var etYears: EditText
    private lateinit var btnCalculate: Button
    private lateinit var resultCard: CardView
    private lateinit var tvMaturity: TextView
    private lateinit var tvTotalInvested: TextView
    private lateinit var tvInterest: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.financial_activity_rdfd_calculator)

        // Back button
        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        // Initialize views
        radioGroup = findViewById(R.id.radioGroupType)
        fdFields = findViewById(R.id.fdFields)
        rdFields = findViewById(R.id.rdFields)
        etPrincipal = findViewById(R.id.etPrincipal)
        etMonthlyInstallment = findViewById(R.id.etMonthlyInstallment)
        etInterestRate = findViewById(R.id.etInterestRate)
        etYears = findViewById(R.id.etYears)
        btnCalculate = findViewById(R.id.btnCalculate)
        resultCard = findViewById(R.id.resultCard)
        tvMaturity = findViewById(R.id.tvMaturity)
        tvTotalInvested = findViewById(R.id.tvTotalInvested)
        tvInterest = findViewById(R.id.tvInterest)

        // Radio group listener to switch between FD and RD fields
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id.radioFD) {
                fdFields.visibility = LinearLayout.VISIBLE
                rdFields.visibility = LinearLayout.GONE
            } else {
                fdFields.visibility = LinearLayout.GONE
                rdFields.visibility = LinearLayout.VISIBLE
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

        val isFD = radioGroup.checkedRadioButtonId == R.id.radioFD

        val maturity: Double
        val totalInvested: Double

        if (isFD) {
            // FD: lump sum, compounded annually (or quarterly? For simplicity, annual)
            val principalStr = etPrincipal.text.toString()
            if (TextUtils.isEmpty(principalStr)) {
                Toast.makeText(this, "Please enter principal amount", Toast.LENGTH_SHORT).show()
                return
            }
            val principal = principalStr.toDoubleOrNull()
            if (principal == null || principal <= 0) {
                Toast.makeText(this, "Please enter a valid positive amount", Toast.LENGTH_SHORT).show()
                return
            }

            // A = P * (1 + r)^n
            maturity = principal * (1 + rate / 100).pow(years)
            totalInvested = principal
        } else {
            // RD: monthly installments, compounded monthly, invested at the beginning of each month
            val installmentStr = etMonthlyInstallment.text.toString()
            if (TextUtils.isEmpty(installmentStr)) {
                Toast.makeText(this, "Please enter monthly installment", Toast.LENGTH_SHORT).show()
                return
            }
            val installment = installmentStr.toDoubleOrNull()
            if (installment == null || installment <= 0) {
                Toast.makeText(this, "Please enter a valid positive amount", Toast.LENGTH_SHORT).show()
                return
            }

            val months = years * 12
            val monthlyRate = rate / 12 / 100

            if (monthlyRate == 0.0) {
                maturity = installment * months
            } else {
                // FV = P * ((1+r)^n - 1) / r * (1+r)
                maturity = installment * ((1 + monthlyRate).pow(months) - 1) / monthlyRate * (1 + monthlyRate)
            }
            totalInvested = installment * months
        }

        val interest = maturity - totalInvested
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

        tvMaturity.text = format.format(maturity)
        tvTotalInvested.text = "Total Invested: ${format.format(totalInvested)}"
        tvInterest.text = "Interest Earned: ${format.format(interest)}"

        resultCard.visibility = CardView.VISIBLE
    }
}