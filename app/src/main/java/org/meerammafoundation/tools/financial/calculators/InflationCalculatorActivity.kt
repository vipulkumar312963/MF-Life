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

class InflationCalculatorActivity : AppCompatActivity() {

    private lateinit var etCurrentValue: EditText
    private lateinit var etInflationRate: EditText
    private lateinit var etYears: EditText
    private lateinit var btnCalculate: Button
    private lateinit var resultCard: CardView
    private lateinit var tvFutureValue: TextView
    private lateinit var tvIncrease: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.financial_activity_inflation_calculator)

        // Back button
        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        // Initialize views
        etCurrentValue = findViewById(R.id.etCurrentValue)
        etInflationRate = findViewById(R.id.etInflationRate)
        etYears = findViewById(R.id.etYears)
        btnCalculate = findViewById(R.id.btnCalculate)
        resultCard = findViewById(R.id.resultCard)
        tvFutureValue = findViewById(R.id.tvFutureValue)
        tvIncrease = findViewById(R.id.tvIncrease)

        btnCalculate.setOnClickListener { calculateInflation() }
    }

    private fun calculateInflation() {
        val currentStr = etCurrentValue.text.toString()
        val rateStr = etInflationRate.text.toString()
        val yearsStr = etYears.text.toString()

        if (TextUtils.isEmpty(currentStr) || TextUtils.isEmpty(rateStr) || TextUtils.isEmpty(yearsStr)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val current = currentStr.toDoubleOrNull()
        val rate = rateStr.toDoubleOrNull()
        val years = yearsStr.toIntOrNull()

        if (current == null || rate == null || years == null) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            return
        }

        if (current <= 0 || rate < 0 || years <= 0) {
            Toast.makeText(this, "Please enter positive values (rate can be zero)", Toast.LENGTH_SHORT).show()
            return
        }

        // Future value formula: FV = PV * (1 + r)^n
        val futureValue = current * (1 + rate / 100).pow(years)
        val increase = futureValue - current

        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

        tvFutureValue.text = format.format(futureValue)
        tvIncrease.text = "Increase: ${format.format(increase)}"

        resultCard.visibility = CardView.VISIBLE
    }
}