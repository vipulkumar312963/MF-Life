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
import java.util.Locale

class IncomeVsExpensesActivity : AppCompatActivity() {

    private lateinit var etIncome: EditText
    private lateinit var etExpenses: EditText
    private lateinit var btnCalculate: Button
    private lateinit var btnSave: Button
    private lateinit var btnLoad: Button
    private lateinit var btnClear: Button
    private lateinit var resultCard: CardView
    private lateinit var tvDifference: TextView
    private lateinit var tvPercentage: TextView
    private lateinit var tvAdvice: TextView
    private lateinit var scrollView: ScrollView

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.budget_activity_income_vs_expenses)

        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        etIncome = findViewById(R.id.etIncome)
        etExpenses = findViewById(R.id.etExpenses)
        btnCalculate = findViewById(R.id.btnCalculate)
        btnSave = findViewById(R.id.btnSave)
        btnLoad = findViewById(R.id.btnLoad)
        btnClear = findViewById(R.id.btnClear)
        resultCard = findViewById(R.id.resultCard)
        tvDifference = findViewById(R.id.tvDifference)
        tvPercentage = findViewById(R.id.tvPercentage)
        tvAdvice = findViewById(R.id.tvAdvice)
        scrollView = findViewById(R.id.scrollView)

        sharedPref = getSharedPreferences("IncomeVsExpensesPrefs", MODE_PRIVATE)

        btnCalculate.setOnClickListener { calculate() }
        btnSave.setOnClickListener { saveData() }
        btnLoad.setOnClickListener { loadData() }
        btnClear.setOnClickListener { clearFields() }
    }

    private fun calculate() {
        val incomeStr = etIncome.text.toString()
        val expensesStr = etExpenses.text.toString()

        if (TextUtils.isEmpty(incomeStr) || TextUtils.isEmpty(expensesStr)) {
            Toast.makeText(this, "Please fill both fields", Toast.LENGTH_SHORT).show()
            return
        }

        val income = incomeStr.toDoubleOrNull()
        val expenses = expensesStr.toDoubleOrNull()

        if (income == null || expenses == null) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
            return
        }

        if (income < 0 || expenses < 0) {
            Toast.makeText(this, "Values cannot be negative", Toast.LENGTH_SHORT).show()
            return
        }

        val difference = income - expenses
        val percentage = if (income > 0) (expenses / income) * 100 else 0.0

        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        val percentFormat = NumberFormat.getInstance().apply { maximumFractionDigits = 1 }

        tvDifference.text = format.format(difference)
        tvPercentage.text = "Spent: ${percentFormat.format(percentage)}% of income"

        tvAdvice.text = when {
            difference < 0 -> "⚠️ You are spending more than you earn. Deficit of ${format.format(-difference)}."
            difference == 0.0 -> "⚖️ You are breaking even."
            percentage < 50 -> "🎉 Excellent! You save more than 50% of your income."
            percentage < 70 -> "✅ Good. You save ${percentFormat.format(100 - percentage)}%."
            else -> "⚠️ You are spending ${percentFormat.format(percentage)}% of your income. Try to save more."
        }

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
        editor.putString("income", etIncome.text.toString())
        editor.putString("expenses", etExpenses.text.toString())
        editor.apply()
        Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show()
    }

    private fun loadData() {
        etIncome.setText(sharedPref.getString("income", ""))
        etExpenses.setText(sharedPref.getString("expenses", ""))
        Toast.makeText(this, "Data loaded", Toast.LENGTH_SHORT).show()
    }

    private fun clearFields() {
        etIncome.text.clear()
        etExpenses.text.clear()
        resultCard.visibility = CardView.GONE
        Toast.makeText(this, "Fields cleared", Toast.LENGTH_SHORT).show()
    }
}