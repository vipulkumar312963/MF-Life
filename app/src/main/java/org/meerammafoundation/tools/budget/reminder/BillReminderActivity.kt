package org.meerammafoundation.tools.budget.reminder

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import org.meerammafoundation.tools.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BillReminderActivity : AppCompatActivity() {

    private lateinit var viewModel: BillReminderViewModel
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var backButton: TextView

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        // ✅ Use MAX_AMOUNT from ReminderConstants
        private const val MAX_AMOUNT = ReminderConstants.MAX_AMOUNT
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.reminder_activity_bill_reminder)

        viewModel = ViewModelProvider(this)[BillReminderViewModel::class.java]

        backButton = findViewById(R.id.backButton)
        backButton.setOnClickListener { finish() }

        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        fabAdd = findViewById(R.id.fabAddBill)

        val adapter = BillReminderPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Upcoming"
                1 -> "Paid"
                else -> ""
            }
        }.attach()

        fabAdd.setOnClickListener {
            showAddBillDialog()
        }

        // ✅ REMOVED: Notification channel creation (already in Application)
        // TestNotificationHelper.createNotificationChannel(this)

        // Request notification permission properly
        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> {
                    // Permission already granted
                }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    MaterialAlertDialogBuilder(this)
                        .setTitle("Permission Required")
                        .setMessage("Notification permission is needed for bill reminders")
                        .setPositiveButton("Allow") { _, _ ->
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                                PERMISSION_REQUEST_CODE
                            )
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }

                else -> {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                        PERMISSION_REQUEST_CODE
                    )
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notifications enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notifications disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddBillDialog() {
        val dialogView = layoutInflater.inflate(R.layout.reminder_dialog_add_bill_reminder, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.etBillName)
        val etAmount = dialogView.findViewById<TextInputEditText>(R.id.etBillAmount)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val spinnerRecurrence = dialogView.findViewById<Spinner>(R.id.spinnerRecurrence)
        val tvDueDate = dialogView.findViewById<TextView>(R.id.tvDueDate)
        val etNotes = dialogView.findViewById<TextInputEditText>(R.id.etNotes)

        // Default date set to 9:00 AM
        val defaultCalendar = Calendar.getInstance()
        defaultCalendar.set(Calendar.HOUR_OF_DAY, ReminderConstants.DEFAULT_REMINDER_HOUR)
        defaultCalendar.set(Calendar.MINUTE, ReminderConstants.DEFAULT_REMINDER_MINUTE)
        defaultCalendar.set(Calendar.SECOND, 0)
        defaultCalendar.set(Calendar.MILLISECOND, 0)
        var selectedDate: Long = defaultCalendar.timeInMillis

        // Set input type for amount
        etAmount.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        // Setup category spinner
        val categories = BillCategory.values().map { category ->
            category.name.replace("_", " ")
                .lowercase()
                .replaceFirstChar { c -> c.uppercase() }
        }
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter

        // Setup recurrence spinner
        val recurrences = RecurrenceType.values().map { recurrence ->
            recurrence.name.replace("_", " ")
                .lowercase()
                .replaceFirstChar { c -> c.uppercase() }
        }
        val recurrenceAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, recurrences)
        recurrenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRecurrence.adapter = recurrenceAdapter

        // Date picker with English locale
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate
        tvDueDate.text = dateFormat.format(calendar.time)

        tvDueDate.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.timeInMillis = selectedDate
            DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val date = Calendar.getInstance()
                    date.set(year, month, dayOfMonth, ReminderConstants.DEFAULT_REMINDER_HOUR, ReminderConstants.DEFAULT_REMINDER_MINUTE, 0)
                    selectedDate = date.timeInMillis
                    tvDueDate.text = dateFormat.format(date.time)
                    tvDueDate.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val dialog = MaterialAlertDialogBuilder(this@BillReminderActivity)
            .setTitle("Add Bill Reminder")
            .setView(dialogView)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(android.app.Dialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val name = etName.text.toString().trim()
                val amountStr = etAmount.text.toString().trim()
                val categoryIndex = spinnerCategory.selectedItemPosition
                val recurrenceIndex = spinnerRecurrence.selectedItemPosition

                // UI Validation
                if (name.isEmpty()) {
                    etName.error = "Enter bill name"
                    return@setOnClickListener
                }

                if (amountStr.isEmpty()) {
                    etAmount.error = "Enter amount"
                    return@setOnClickListener
                }

                val amount = amountStr.toDoubleOrNull()
                if (amount == null || amount <= 0 || amount > MAX_AMOUNT) {
                    etAmount.error = "Enter valid amount (1 to $MAX_AMOUNT)"
                    return@setOnClickListener
                }

                // Allow today's date (not just future)
                val todayStart = Calendar.getInstance()
                todayStart.set(Calendar.HOUR_OF_DAY, 0)
                todayStart.set(Calendar.MINUTE, 0)
                todayStart.set(Calendar.SECOND, 0)
                todayStart.set(Calendar.MILLISECOND, 0)

                if (selectedDate < todayStart.timeInMillis) {
                    Toast.makeText(this, "Please select today or a future date", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Safe enum retrieval
                val category = BillCategory.values().getOrNull(categoryIndex) ?: BillCategory.OTHER
                val recurrence = RecurrenceType.values().getOrNull(recurrenceIndex) ?: RecurrenceType.ONE_TIME
                val notes = etNotes.text.toString().trim()

                viewModel.createBill(name, amount, selectedDate, category, recurrence, notes)
                Toast.makeText(this, "Bill reminder added", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}