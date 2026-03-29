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
import java.text.DecimalFormat
import kotlin.math.pow

class ROICalculatorActivity : AppCompatActivity() {

    private lateinit var etInitial: EditText
    private lateinit var etFinal: EditText
    private lateinit var etYears: EditText
    private lateinit var btnCalculate: Button
    private lateinit var resultCard: CardView
    private lateinit var tvSimpleROI: TextView
    private lateinit var tvCAGR: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.financial_activity_roi_calculator)

        // Back button
        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        // Initialize views
        etInitial = findViewById(R.id.etInitialInvestment)
        etFinal = findViewById(R.id.etFinalValue)
        etYears = findViewById(R.id.etYears)
        btnCalculate = findViewById(R.id.btnCalculate)
        resultCard = findViewById(R.id.resultCard)
        tvSimpleROI = findViewById(R.id.tvSimpleROI)
        tvCAGR = findViewById(R.id.tvCAGR)

        btnCalculate.setOnClickListener { calculateROI() }
    }

    private fun calculateROI() {
        val initialStr = etInitial.text.toString()
        val finalStr = etFinal.text.toString()
        val yearsStr = etYears.text.toString()

        if (TextUtils.isEmpty(initialStr) || TextUtils.isEmpty(finalStr) || TextUtils.isEmpty(yearsStr)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val initial = initialStr.toDoubleOrNull()
        val finalValue = finalStr.toDoubleOrNull()
        val years = yearsStr.toDoubleOrNull()

        if (initial == null || finalValue == null || years == null) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            return
        }

        if (initial <= 0 || finalValue <= 0 || years <= 0) {
            Toast.makeText(this, "Values must be greater than zero", Toast.LENGTH_SHORT).show()
            return
        }

        // Simple ROI
        val profit = finalValue - initial
        val simpleROI = (profit / initial) * 100

        // CAGR
        val cagr = ((finalValue / initial).pow(1.0 / years) - 1) * 100

        val df = DecimalFormat("#.##")
        tvSimpleROI.text = "${df.format(simpleROI)}%"
        tvCAGR.text = "${df.format(cagr)}%"

        resultCard.visibility = CardView.VISIBLE
    }
}