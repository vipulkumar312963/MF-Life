package org.meerammafoundation.tools.budget

import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import org.meerammafoundation.tools.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class DebtPayoffActivity : AppCompatActivity() {

    private lateinit var etDebt: EditText
    private lateinit var etInterest: EditText
    private lateinit var etPayment: EditText
    private lateinit var btnCalculate: Button
    private lateinit var btnSave: Button
    private lateinit var btnLoad: Button
    private lateinit var btnClear: Button
    private lateinit var resultCard: CardView
    private lateinit var tvTime: TextView
    private lateinit var tvTotalInterest: TextView
    private lateinit var tvPayoffDate: TextView
    private lateinit var scrollView: ScrollView

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.budget_activity_debt_payoff)

        // Back button
        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        // Initialize views
        etDebt = findViewById(R.id.etDebt)
        etInterest = findViewById(R.id.etInterest)
        etPayment = findViewById(R.id.etPayment)
        btnCalculate = findViewById(R.id.btnCalculate)
        btnSave = findViewById(R.id.btnSave)
        btnLoad = findViewById(R.id.btnLoad)
        btnClear = findViewById(R.id.btnClear)
        resultCard = findViewById(R.id.resultCard)
        tvTime = findViewById(R.id.tvTime)
        tvTotalInterest = findViewById(R.id.tvTotalInterest)
        tvPayoffDate = findViewById(R.id.tvPayoffDate)
        scrollView = findViewById(R.id.scrollView)

        sharedPref = getSharedPreferences("DebtPrefs", MODE_PRIVATE)

        btnCalculate.setOnClickListener { calculate() }
        btnSave.setOnClickListener { saveData() }
        btnLoad.setOnClickListener { loadData() }
        btnClear.setOnClickListener { clearFields() }
    }

    private fun calculate() {
        val debtStr = etDebt.text.toString()
        val interestStr = etInterest.text.toString()
        val paymentStr = etPayment.text.toString()

        if (TextUtils.isEmpty(debtStr) || TextUtils.isEmpty(interestStr) || TextUtils.isEmpty(paymentStr)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val debt = debtStr.toDoubleOrNull()
        val annualRate = interestStr.toDoubleOrNull()
        val monthlyPayment = paymentStr.toDoubleOrNull()

        if (debt == null || annualRate == null || monthlyPayment == null) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            return
        }

        if (debt <= 0 || annualRate < 0 || monthlyPayment <= 0) {
            Toast.makeText(this, "Values must be positive (rate can be zero)", Toast.LENGTH_SHORT).show()
            return
        }

        if (monthlyPayment <= debt * annualRate / 12 / 100) {
            Toast.makeText(this, "Monthly payment must be > interest accrued per month", Toast.LENGTH_SHORT).show()
            return
        }

        val monthlyRate = annualRate / 12 / 100

        var months = 0
        var balance = debt
        var totalInterest = 0.0

        while (balance > 0 && months < 1200) { // 100 years safety cap
            val interestThisMonth = balance * monthlyRate
            totalInterest += interestThisMonth
            val principalPaid = monthlyPayment - interestThisMonth
            if (principalPaid <= 0) break // shouldn't happen because of earlier check
            balance -= principalPaid
            months++
        }

        if (balance <= 0) {
            // Calculate exact final payment adjustment
            // Already accounted in loop
        }

        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        val monthsStr = if (months == 1) "1 month" else "$months months"

        tvTime.text = monthsStr
        tvTotalInterest.text = format.format(totalInterest)

        // Calculate payoff date
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.MONTH, months)
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        tvPayoffDate.text = "Payoff by: ${dateFormat.format(calendar.time)}"

        resultCard.visibility = CardView.VISIBLE

        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun parseDouble(editText: EditText): Double {
        val text = editText.text.toString()
        return if (TextUtils.isEmpty(text)) 0.0 else text.toDoubleOrNull() ?: 0.0
    }

    private fun saveData() {
        val editor = sharedPref.edit()
        editor.putString("debt", etDebt.text.toString())
        editor.putString("interest", etInterest.text.toString())
        editor.putString("payment", etPayment.text.toString())
        editor.apply()
        Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show()
    }

    private fun loadData() {
        etDebt.setText(sharedPref.getString("debt", ""))
        etInterest.setText(sharedPref.getString("interest", ""))
        etPayment.setText(sharedPref.getString("payment", ""))
        Toast.makeText(this, "Data loaded", Toast.LENGTH_SHORT).show()
    }

    private fun clearFields() {
        etDebt.text.clear()
        etInterest.text.clear()
        etPayment.text.clear()
        resultCard.visibility = CardView.GONE
        Toast.makeText(this, "Fields cleared", Toast.LENGTH_SHORT).show()
    }
}