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

class GratuityCalculatorActivity : AppCompatActivity() {

    private lateinit var etSalary: EditText
    private lateinit var etYears: EditText
    private lateinit var btnCalculate: Button
    private lateinit var resultCard: CardView
    private lateinit var tvGratuity: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.financial_activity_gratuity_calculator)

        // Back button
        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        // Initialize views
        etSalary = findViewById(R.id.etSalary)
        etYears = findViewById(R.id.etYears)
        btnCalculate = findViewById(R.id.btnCalculate)
        resultCard = findViewById(R.id.resultCard)
        tvGratuity = findViewById(R.id.tvGratuity)

        btnCalculate.setOnClickListener { calculateGratuity() }
    }

    private fun calculateGratuity() {
        val salaryStr = etSalary.text.toString()
        val yearsStr = etYears.text.toString()

        if (TextUtils.isEmpty(salaryStr) || TextUtils.isEmpty(yearsStr)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val salary = salaryStr.toDoubleOrNull()
        val years = yearsStr.toDoubleOrNull()

        if (salary == null || years == null) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            return
        }

        if (salary <= 0 || years <= 0) {
            Toast.makeText(this, "Values must be greater than zero", Toast.LENGTH_SHORT).show()
            return
        }

        // Round years to nearest integer: if fractional part >= 0.5, treat as full year
        val yearsRounded = if (years - years.toInt() >= 0.5) years.toInt() + 1 else years.toInt()

        // Gratuity = (last drawn salary × years of service × 15) / 26
        val gratuity = (salary * yearsRounded * 15) / 26

        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        tvGratuity.text = format.format(gratuity)

        resultCard.visibility = CardView.VISIBLE
    }
}