package org.meerammafoundation.tools.financial.calculators

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

class NetWorthCalculatorActivity : AppCompatActivity() {

    // Asset fields
    private lateinit var etAgriLand: EditText
    private lateinit var etNonAgriLand: EditText
    private lateinit var etGold: EditText
    private lateinit var etSilver: EditText
    private lateinit var etOtherMetals: EditText
    private lateinit var etCash: EditText
    private lateinit var etShares: EditText
    private lateinit var etMutualFunds: EditText
    private lateinit var etOtherAssets: EditText

    // Liability fields
    private lateinit var etLoans: EditText
    private lateinit var etOtherLiabilities: EditText

    // Monthly expenses
    private lateinit var etMonthlyExpenses: EditText

    // Family support
    private lateinit var etInsideFamily: EditText
    private lateinit var etOutsideFamily: EditText

    // Buttons
    private lateinit var btnCalculate: Button
    private lateinit var btnSave: Button
    private lateinit var btnLoad: Button
    private lateinit var btnClear: Button

    // Result views
    private lateinit var resultCard: CardView
    private lateinit var tvTotalAssets: TextView
    private lateinit var tvTotalLiabilities: TextView
    private lateinit var tvNetWorth: TextView
    private lateinit var tvMaterialStars: TextView
    private lateinit var tvNonMaterialStars: TextView
    private lateinit var tvAdvice: TextView

    // ScrollView
    private lateinit var scrollView: ScrollView

    // SharedPreferences
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.financial_activity_net_worth_calculator)

        // Back button
        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        // Initialize all EditTexts
        etAgriLand = findViewById(R.id.etAgriLand)
        etNonAgriLand = findViewById(R.id.etNonAgriLand)
        etGold = findViewById(R.id.etGold)
        etSilver = findViewById(R.id.etSilver)
        etOtherMetals = findViewById(R.id.etOtherMetals)
        etCash = findViewById(R.id.etCash)
        etShares = findViewById(R.id.etShares)
        etMutualFunds = findViewById(R.id.etMutualFunds)
        etOtherAssets = findViewById(R.id.etOtherAssets)

        etLoans = findViewById(R.id.etLoans)
        etOtherLiabilities = findViewById(R.id.etOtherLiabilities)

        etMonthlyExpenses = findViewById(R.id.etMonthlyExpenses)

        etInsideFamily = findViewById(R.id.etInsideFamily)
        etOutsideFamily = findViewById(R.id.etOutsideFamily)

        // Buttons
        btnCalculate = findViewById(R.id.btnCalculate)
        btnSave = findViewById(R.id.btnSave)
        btnLoad = findViewById(R.id.btnLoad)
        btnClear = findViewById(R.id.btnClear)

        // Result views
        resultCard = findViewById(R.id.resultCard)
        tvTotalAssets = findViewById(R.id.tvTotalAssets)
        tvTotalLiabilities = findViewById(R.id.tvTotalLiabilities)
        tvNetWorth = findViewById(R.id.tvNetWorth)
        tvMaterialStars = findViewById(R.id.tvMaterialStars)
        tvNonMaterialStars = findViewById(R.id.tvNonMaterialStars)
        tvAdvice = findViewById(R.id.tvAdvice)

        // ScrollView
        scrollView = findViewById(R.id.scrollView)

        // SharedPreferences
        sharedPref = getSharedPreferences("WealthPrefs", MODE_PRIVATE)

        // Set click listeners
        btnCalculate.setOnClickListener { calculateWealth() }
        btnSave.setOnClickListener { saveData() }
        btnLoad.setOnClickListener { loadData() }
        btnClear.setOnClickListener { clearAllFields() }
    }

    private fun calculateWealth() {
        // Parse all asset fields (default 0 if empty)
        val agriLand = parseDouble(etAgriLand)
        val nonAgriLand = parseDouble(etNonAgriLand)
        val gold = parseDouble(etGold)
        val silver = parseDouble(etSilver)
        val otherMetals = parseDouble(etOtherMetals)
        val cash = parseDouble(etCash)
        val shares = parseDouble(etShares)
        val mutualFunds = parseDouble(etMutualFunds)
        val otherAssets = parseDouble(etOtherAssets)

        val totalAssets = agriLand + nonAgriLand + gold + silver + otherMetals + cash + shares + mutualFunds + otherAssets

        // Liabilities
        val loans = parseDouble(etLoans)
        val otherLiabilities = parseDouble(etOtherLiabilities)
        val totalLiabilities = loans + otherLiabilities

        // Monthly expenses
        val monthlyExpenses = parseDouble(etMonthlyExpenses)

        // Family support
        val insideFamily = parseInt(etInsideFamily)
        val outsideFamily = parseInt(etOutsideFamily)
        val totalSupport = insideFamily + outsideFamily

        val netWorth = totalAssets - totalLiabilities

        // Material stars based on net worth / monthly expenses ratio
        val materialStars = if (monthlyExpenses > 0) {
            val ratio = netWorth / monthlyExpenses
            when {
                ratio >= 20 -> 5
                ratio >= 10 -> 4
                ratio >= 5 -> 3
                ratio >= 1 -> 2
                ratio > 0 -> 1
                else -> 0
            }
        } else {
            0
        }

        // Non-material stars based on total support people
        val nonMaterialStars = when {
            totalSupport >= 7 -> 5
            totalSupport >= 5 -> 4
            totalSupport >= 3 -> 3
            totalSupport >= 1 -> 2
            else -> 0
        }

        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

        tvTotalAssets.text = format.format(totalAssets)
        tvTotalLiabilities.text = format.format(totalLiabilities)
        tvNetWorth.text = format.format(netWorth)

        tvMaterialStars.text = getStars(materialStars)
        tvNonMaterialStars.text = getStars(nonMaterialStars)

        // Advice
        tvAdvice.text = buildString {
            if (netWorth < 0) append("⚠️ Your net worth is negative. ")
            else if (netWorth == 0.0) append("⚖️ Your net worth is zero. ")
            else append("✅ Positive net worth. ")

            if (totalSupport == 0) append("You have no family support. Consider building your network.")
            else append("You have $totalSupport people who can support you.")
        }

        resultCard.visibility = CardView.VISIBLE

        // Auto-scroll to show the result
        scrollView.post {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }

    private fun getStars(count: Int): String {
        return when (count) {
            5 -> "★★★★★"
            4 -> "★★★★☆"
            3 -> "★★★☆☆"
            2 -> "★★☆☆☆"
            1 -> "★☆☆☆☆"
            else -> "☆☆☆☆☆"
        }
    }

    private fun parseDouble(editText: EditText): Double {
        val text = editText.text.toString()
        return if (TextUtils.isEmpty(text)) 0.0 else text.toDoubleOrNull() ?: 0.0
    }

    private fun parseInt(editText: EditText): Int {
        val text = editText.text.toString()
        return if (TextUtils.isEmpty(text)) 0 else text.toIntOrNull() ?: 0
    }

    private fun saveData() {
        val editor = sharedPref.edit()
        editor.putString("agriLand", etAgriLand.text.toString())
        editor.putString("nonAgriLand", etNonAgriLand.text.toString())
        editor.putString("gold", etGold.text.toString())
        editor.putString("silver", etSilver.text.toString())
        editor.putString("otherMetals", etOtherMetals.text.toString())
        editor.putString("cash", etCash.text.toString())
        editor.putString("shares", etShares.text.toString())
        editor.putString("mutualFunds", etMutualFunds.text.toString())
        editor.putString("otherAssets", etOtherAssets.text.toString())
        editor.putString("loans", etLoans.text.toString())
        editor.putString("otherLiabilities", etOtherLiabilities.text.toString())
        editor.putString("monthlyExpenses", etMonthlyExpenses.text.toString())
        editor.putString("insideFamily", etInsideFamily.text.toString())
        editor.putString("outsideFamily", etOutsideFamily.text.toString())
        editor.apply()
        Toast.makeText(this, "Data saved", Toast.LENGTH_SHORT).show()
    }

    private fun loadData() {
        etAgriLand.setText(sharedPref.getString("agriLand", ""))
        etNonAgriLand.setText(sharedPref.getString("nonAgriLand", ""))
        etGold.setText(sharedPref.getString("gold", ""))
        etSilver.setText(sharedPref.getString("silver", ""))
        etOtherMetals.setText(sharedPref.getString("otherMetals", ""))
        etCash.setText(sharedPref.getString("cash", ""))
        etShares.setText(sharedPref.getString("shares", ""))
        etMutualFunds.setText(sharedPref.getString("mutualFunds", ""))
        etOtherAssets.setText(sharedPref.getString("otherAssets", ""))
        etLoans.setText(sharedPref.getString("loans", ""))
        etOtherLiabilities.setText(sharedPref.getString("otherLiabilities", ""))
        etMonthlyExpenses.setText(sharedPref.getString("monthlyExpenses", ""))
        etInsideFamily.setText(sharedPref.getString("insideFamily", ""))
        etOutsideFamily.setText(sharedPref.getString("outsideFamily", ""))
        Toast.makeText(this, "Data loaded", Toast.LENGTH_SHORT).show()
    }

    private fun clearAllFields() {
        etAgriLand.text.clear()
        etNonAgriLand.text.clear()
        etGold.text.clear()
        etSilver.text.clear()
        etOtherMetals.text.clear()
        etCash.text.clear()
        etShares.text.clear()
        etMutualFunds.text.clear()
        etOtherAssets.text.clear()
        etLoans.text.clear()
        etOtherLiabilities.text.clear()
        etMonthlyExpenses.text.clear()
        etInsideFamily.text.clear()
        etOutsideFamily.text.clear()
        resultCard.visibility = CardView.GONE
        Toast.makeText(this, "All fields cleared", Toast.LENGTH_SHORT).show()
    }
}