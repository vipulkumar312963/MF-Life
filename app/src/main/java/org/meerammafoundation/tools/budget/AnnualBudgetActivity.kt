package org.meerammafoundation.tools.budget

import android.content.Context
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

class AnnualBudgetActivity : AppCompatActivity() {

    private lateinit var etIncome: EditText
    private lateinit var etRent: EditText
    private lateinit var etGroceries: EditText
    private lateinit var etUtilities: EditText
    private lateinit var etTransport: EditText
    private lateinit var etHealthcare: EditText
    private lateinit var etEntertainment: EditText
    private lateinit var etSavings: EditText
    private lateinit var etMisc: EditText

    private lateinit var btnCalculate: Button
    private lateinit var btnSave: Button
    private lateinit var btnLoad: Button
    private lateinit var btnClear: Button
    private lateinit var resultCard: CardView
    private lateinit var tvTotalIncome: TextView
    private lateinit var tvTotalExpenses: TextView
    private lateinit var tvRemaining: TextView
    private lateinit var tvAdvice: TextView
    private lateinit var scrollView: ScrollView

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.budget_activity_annual_budget)

        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        // Initialize EditTexts
        etIncome = findViewById(R.id.etIncome)
        etRent = findViewById(R.id.etRent)
        etGroceries = findViewById(R.id.etGroceries)
        etUtilities = findViewById(R.id.etUtilities)
        etTransport = findViewById(R.id.etTransport)
        etHealthcare = findViewById(R.id.etHealthcare)
        etEntertainment = findViewById(R.id.etEntertainment)
        etSavings = findViewById(R.id.etSavings)
        etMisc = findViewById(R.id.etMisc)

        // Buttons
        btnCalculate = findViewById(R.id.btnCalculate)
        btnSave = findViewById(R.id.btnSave)
        btnLoad = findViewById(R.id.btnLoad)
        btnClear = findViewById(R.id.btnClear)

        // Result views
        resultCard = findViewById(R.id.resultCard)
        tvTotalIncome = findViewById(R.id.tvTotalIncome)
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses)
        tvRemaining = findViewById(R.id.tvRemaining)
        tvAdvice = findViewById(R.id.tvAdvice)

        scrollView = findViewById(R.id.scrollView)

        sharedPref = getSharedPreferences("AnnualBudgetPrefs", Context.MODE_PRIVATE)

        btnCalculate.setOnClickListener { calculateBudget() }
        btnSave.setOnClickListener { saveData() }
        btnLoad.setOnClickListener { loadData() }
        btnClear.setOnClickListener { clearAllFields() }
    }

    private fun calculateBudget() {
        val income = parseDouble(etIncome)

        val rent = parseDouble(etRent)
        val groceries = parseDouble(etGroceries)
        val utilities = parseDouble(etUtilities)
        val transport = parseDouble(etTransport)
        val healthcare = parseDouble(etHealthcare)
        val entertainment = parseDouble(etEntertainment)
        val savings = parseDouble(etSavings)
        val misc = parseDouble(etMisc)

        val totalExpenses = rent + groceries + utilities + transport + healthcare + entertainment + savings + misc
        val remaining = income - totalExpenses

        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

        tvTotalIncome.text = format.format(income)
        tvTotalExpenses.text = format.format(totalExpenses)
        tvRemaining.text = format.format(remaining)

        tvAdvice.text = when {
            remaining < 0 -> "⚠️ You are overspending by ${format.format(-remaining)}. Reduce expenses or increase income."
            remaining == 0.0 -> "⚖️ You are breaking even. Try to save more."
            remaining < income * 0.2 -> "✅ You are saving, but less than 20%. Aim for 20% savings."
            else -> "🎉 Great job! You are saving ${format.format(remaining)} (${(remaining/income*100).toInt()}% of income)."
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
        editor.putString("rent", etRent.text.toString())
        editor.putString("groceries", etGroceries.text.toString())
        editor.putString("utilities", etUtilities.text.toString())
        editor.putString("transport", etTransport.text.toString())
        editor.putString("healthcare", etHealthcare.text.toString())
        editor.putString("entertainment", etEntertainment.text.toString())
        editor.putString("savings", etSavings.text.toString())
        editor.putString("misc", etMisc.text.toString())
        editor.apply()
        Toast.makeText(this, "Annual budget data saved", Toast.LENGTH_SHORT).show()
    }

    private fun loadData() {
        etIncome.setText(sharedPref.getString("income", ""))
        etRent.setText(sharedPref.getString("rent", ""))
        etGroceries.setText(sharedPref.getString("groceries", ""))
        etUtilities.setText(sharedPref.getString("utilities", ""))
        etTransport.setText(sharedPref.getString("transport", ""))
        etHealthcare.setText(sharedPref.getString("healthcare", ""))
        etEntertainment.setText(sharedPref.getString("entertainment", ""))
        etSavings.setText(sharedPref.getString("savings", ""))
        etMisc.setText(sharedPref.getString("misc", ""))
        Toast.makeText(this, "Annual budget data loaded", Toast.LENGTH_SHORT).show()
    }

    private fun clearAllFields() {
        etIncome.text.clear()
        etRent.text.clear()
        etGroceries.text.clear()
        etUtilities.text.clear()
        etTransport.text.clear()
        etHealthcare.text.clear()
        etEntertainment.text.clear()
        etSavings.text.clear()
        etMisc.text.clear()
        resultCard.visibility = CardView.GONE
        Toast.makeText(this, "All fields cleared", Toast.LENGTH_SHORT).show()
    }
}