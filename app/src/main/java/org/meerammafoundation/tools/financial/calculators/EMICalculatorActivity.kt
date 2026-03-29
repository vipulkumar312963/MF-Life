package org.meerammafoundation.tools.financial.calculators
import org.meerammafoundation.tools.R
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.text.NumberFormat
import java.util.Locale

class EMICalculatorActivity : AppCompatActivity() {

    private lateinit var etPrincipal: EditText
    private lateinit var etInterest: EditText
    private lateinit var etTenure: EditText
    private lateinit var btnCalculate: Button
    private lateinit var resultCard: CardView
    private lateinit var tvEMI: TextView
    private lateinit var tvTotalInterest: TextView
    private lateinit var tvTotalPayment: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.financial_activity_emi_calculator)

        // Back button
        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        // Initialize views
        etPrincipal = findViewById(R.id.etPrincipal)
        etInterest = findViewById(R.id.etInterest)
        etTenure = findViewById(R.id.etTenure)
        btnCalculate = findViewById(R.id.btnCalculate)
        resultCard = findViewById(R.id.resultCard)
        tvEMI = findViewById(R.id.tvEMI)
        tvTotalInterest = findViewById(R.id.tvTotalInterest)
        tvTotalPayment = findViewById(R.id.tvTotalPayment)

        btnCalculate.setOnClickListener { calculateEMI() }
    }

    private fun calculateEMI() {
        val principalStr = etPrincipal.text.toString()
        val interestStr = etInterest.text.toString()
        val tenureStr = etTenure.text.toString()

        if (TextUtils.isEmpty(principalStr) || TextUtils.isEmpty(interestStr) || TextUtils.isEmpty(tenureStr)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val principal = principalStr.toDoubleOrNull()
        val annualRate = interestStr.toDoubleOrNull()
        val tenureMonths = tenureStr.toIntOrNull()

        if (principal == null || annualRate == null || tenureMonths == null) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            return
        }

        if (principal <= 0 || annualRate <= 0 || tenureMonths <= 0) {
            Toast.makeText(this, "Values must be greater than zero", Toast.LENGTH_SHORT).show()
            return
        }

        // EMI calculation
        val monthlyRate = annualRate / 12 / 100
        val emi = if (monthlyRate == 0.0) {
            principal / tenureMonths
        } else {
            val factor = Math.pow(1 + monthlyRate, tenureMonths.toDouble())
            principal * monthlyRate * factor / (factor - 1)
        }

        val totalPayment = emi * tenureMonths
        val totalInterest = totalPayment - principal

        // Format currency (Indian Rupees)
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

        tvEMI.text = format.format(emi)
        tvTotalInterest.text = "Total Interest: ${format.format(totalInterest)}"
        tvTotalPayment.text = "Total Payment: ${format.format(totalPayment)}"

        // Show result card
        resultCard.visibility = CardView.VISIBLE
    }
}