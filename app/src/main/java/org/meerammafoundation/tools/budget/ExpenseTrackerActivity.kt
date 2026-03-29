package org.meerammafoundation.tools.budget

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.meerammafoundation.tools.R
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import android.view.ViewGroup

class ExpenseTrackerActivity : AppCompatActivity() {

    private lateinit var tvDate: TextView
    private lateinit var spinnerCategory: Spinner
    private lateinit var etAmount: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnAdd: Button
    private lateinit var btnClearAll: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvTotal: TextView

    private val expenses = mutableListOf<Expense>()
    private lateinit var adapter: ExpenseAdapter
    private val calendar = Calendar.getInstance()
    private var selectedDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.expensetracker_activity_expense_tracker)

        // Back button
        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        // Initialize views
        tvDate = findViewById(R.id.tvDate)
        spinnerCategory = findViewById(R.id.spinnerCategory)
        etAmount = findViewById(R.id.etAmount)
        etDescription = findViewById(R.id.etDescription)
        btnAdd = findViewById(R.id.btnAddExpense)
        btnClearAll = findViewById(R.id.btnClearAll)
        recyclerView = findViewById(R.id.recyclerView)
        tvTotal = findViewById(R.id.tvTotal)

        // Set up category spinner
        val categories = arrayOf("Food", "Transport", "Shopping", "Entertainment", "Bills", "Healthcare", "Education", "Other")
        val adapterSpinner = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent) as TextView
                view.setTextColor(resources.getColor(R.color.text_primary, null))
                return view
            }
        }
        adapterSpinner.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapterSpinner

        // Set up date picker
        tvDate.setOnClickListener {
            DatePickerDialog(this, { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                selectedDate = dateFormat.format(calendar.time)
                tvDate.text = selectedDate
                tvDate.setTextColor(resources.getColor(R.color.text_primary, null))
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ExpenseAdapter(expenses)
        recyclerView.adapter = adapter

        // Add expense button
        btnAdd.setOnClickListener {
            addExpense()
        }

        // Clear all button
        btnClearAll.setOnClickListener {
            expenses.clear()
            adapter.notifyDataSetChanged()
            updateTotal()
        }
    }

    private fun addExpense() {
        val date = selectedDate
        if (date.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show()
            return
        }

        val category = spinnerCategory.selectedItem.toString()
        val amountStr = etAmount.text.toString()
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val description = etDescription.text.toString().ifEmpty { "No description" }

        val expense = Expense(date, category, amount, description)
        expenses.add(expense)
        adapter.notifyItemInserted(expenses.size - 1)
        updateTotal()

        // Clear input fields
        etAmount.text.clear()
        etDescription.text.clear()
        tvDate.text = "Select Date"
        tvDate.setTextColor(resources.getColor(R.color.text_secondary, null))
        selectedDate = ""
        spinnerCategory.setSelection(0)
    }

    private fun updateTotal() {
        val total = expenses.sumOf { it.amount }
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        tvTotal.text = "Total: ${format.format(total)}"
    }
}