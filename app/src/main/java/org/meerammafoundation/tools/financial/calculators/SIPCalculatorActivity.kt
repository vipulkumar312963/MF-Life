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

class SIPCalculatorActivity : AppCompatActivity() {

    private lateinit var etMonthlyInvestment: EditText
    private lateinit var etRate: EditText
    private lateinit var etYears: EditText
    private lateinit var btnCalculate: Button
    private lateinit var resultCard: CardView
    private lateinit var tvFutureValue: TextView
    private lateinit var tvTotalInvestment: TextView
    private lateinit var tvProfit: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.financial_activity_sip_calculator)

        // Back button
        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        // Initialize views
        etMonthlyInvestment = findViewById(R.id.etMonthlyInvestment)
        etRate = findViewById(R.id.etRate)
        etYears = findViewById(R.id.etYears)
        btnCalculate = findViewById(R.id.btnCalculate)
        resultCard = findViewById(R.id.resultCard)
        tvFutureValue = findViewById(R.id.tvFutureValue)
        tvTotalInvestment = findViewById(R.id.tvTotalInvestment)
        tvProfit = findViewById(R.id.tvProfit)

        btnCalculate.setOnClickListener { calculate() }
    }

    private fun calculate() {
        val investmentStr = etMonthlyInvestment.text.toString()
        val rateStr = etRate.text.toString()
        val yearsStr = etYears.text.toString()

        if (TextUtils.isEmpty(investmentStr) || TextUtils.isEmpty(rateStr) || TextUtils.isEmpty(yearsStr)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val monthlyInvestment = investmentStr.toDoubleOrNull()
        val annualRate = rateStr.toDoubleOrNull()
        val years = yearsStr.toIntOrNull()

        if (monthlyInvestment == null || annualRate == null || years == null) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            return
        }

        if (monthlyInvestment <= 0 || annualRate < 0 || years <= 0) {
            Toast.makeText(this, "Investment and years must be >0, rate >=0", Toast.LENGTH_SHORT).show()
            return
        }

        // SIP formula: FV = P * ((1 + r)^n - 1) / r * (1 + r)
        // where P = monthly investment, r = monthly rate (annual/12/100), n = number of months
        val months = years * 12
        val monthlyRate = annualRate / 12 / 100

        val futureValue = if (monthlyRate == 0.0) {
            monthlyInvestment * months
        } else {
            monthlyInvestment * ((1 + monthlyRate).pow(months) - 1) / monthlyRate * (1 + monthlyRate)
        }

        val totalInvested = monthlyInvestment * months
        val profit = futureValue - totalInvested

        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

        tvFutureValue.text = format.format(futureValue)
        tvTotalInvestment.text = "Total Invested: ${format.format(totalInvested)}"
        tvProfit.text = "Profit: ${format.format(profit)}"

        resultCard.visibility = CardView.VISIBLE
    }
}