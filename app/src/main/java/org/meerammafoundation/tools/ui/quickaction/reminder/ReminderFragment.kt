package org.meerammafoundation.tools.ui.quickaction.reminder

import android.Manifest
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
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

class ReminderFragment : Fragment() {

    private lateinit var viewModel: ReminderViewModel
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var fabAdd: FloatingActionButton

    companion object {
        private const val PERMISSION_REQUEST_CODE = 100
        private const val MAX_AMOUNT = ReminderConstants.MAX_AMOUNT

        fun newInstance(): ReminderFragment {
            return ReminderFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bill_reminder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[ReminderViewModel::class.java]

        tabLayout = view.findViewById(R.id.tabLayout)
        viewPager = view.findViewById(R.id.viewPager)
        fabAdd = view.findViewById(R.id.fabAddBill)

        val adapter = ReminderPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Upcoming"
                1 -> "Completed"
                else -> ""
            }
        }.attach()

        fabAdd.setOnClickListener {
            showAddReminderDialog()
        }

        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED -> { }

                ActivityCompat.shouldShowRequestPermissionRationale(
                    requireActivity(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) -> {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Permission Required")
                        .setMessage("Notification permission is needed for reminders")
                        .setPositiveButton("Allow") { _, _ ->
                            ActivityCompat.requestPermissions(
                                requireActivity(),
                                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                                PERMISSION_REQUEST_CODE
                            )
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }

                else -> {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
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
                Toast.makeText(requireContext(), "Notifications enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Notifications disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showAddReminderDialog() {
        val dialogView = layoutInflater.inflate(R.layout.reminder_dialog_add_reminder, null)

        // Generic fields
        val etTitle = dialogView.findViewById<TextInputEditText>(R.id.etReminderTitle)
        val etDescription = dialogView.findViewById<TextInputEditText>(R.id.etReminderDescription)
        val spinnerType = dialogView.findViewById<Spinner>(R.id.spinnerReminderType)
        val spinnerPriority = dialogView.findViewById<Spinner>(R.id.spinnerPriority)
        val spinnerRecurrence = dialogView.findViewById<Spinner>(R.id.spinnerRecurrence)
        val tvDueDate = dialogView.findViewById<TextView>(R.id.tvDueDate)

        // Bill-specific fields
        val layoutBillFields = dialogView.findViewById<LinearLayout>(R.id.layoutBillFields)
        val etAmount = dialogView.findViewById<TextInputEditText>(R.id.etAmount)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)

        // Default date set to 9:00 AM
        val defaultCalendar = Calendar.getInstance()
        defaultCalendar.set(Calendar.HOUR_OF_DAY, ReminderConstants.DEFAULT_REMINDER_HOUR)
        defaultCalendar.set(Calendar.MINUTE, ReminderConstants.DEFAULT_REMINDER_MINUTE)
        defaultCalendar.set(Calendar.SECOND, 0)
        defaultCalendar.set(Calendar.MILLISECOND, 0)
        var selectedDate: Long = defaultCalendar.timeInMillis

        // Setup Reminder Type Spinner
        val types = ReminderType.values().map { type ->
            type.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
        }
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, types)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = typeAdapter

        // Setup Priority Spinner
        val priorities = Priority.values().map { priority ->
            priority.name.replaceFirstChar { it.uppercase() }
        }
        val priorityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, priorities)
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPriority.adapter = priorityAdapter

        // Setup Recurrence Spinner
        val recurrences = RecurrenceType.values().map { recurrence ->
            recurrence.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
        }
        val recurrenceAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, recurrences)
        recurrenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRecurrence.adapter = recurrenceAdapter

        // Setup Category Spinner (for bills)
        val categories = BillCategory.values().map { category ->
            category.name.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
        }
        val categoryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = categoryAdapter

        // Show/hide bill fields based on selected type
        fun updateBillFieldsVisibility(type: ReminderType) {
            layoutBillFields.visibility = if (type == ReminderType.BILL) View.VISIBLE else View.GONE
        }

        spinnerType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedType = ReminderType.values()[position]
                updateBillFieldsVisibility(selectedType)
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Date picker
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDate
        tvDueDate.text = dateFormat.format(calendar.time)

        tvDueDate.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.timeInMillis = selectedDate
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val date = Calendar.getInstance()
                    date.set(year, month, dayOfMonth, ReminderConstants.DEFAULT_REMINDER_HOUR, ReminderConstants.DEFAULT_REMINDER_MINUTE, 0)
                    selectedDate = date.timeInMillis
                    tvDueDate.text = dateFormat.format(date.time)
                    tvDueDate.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary))
                },
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Reminder")
            .setView(dialogView)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val title = etTitle.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val typeIndex = spinnerType.selectedItemPosition
                val priorityIndex = spinnerPriority.selectedItemPosition
                val recurrenceIndex = spinnerRecurrence.selectedItemPosition

                if (title.isEmpty()) {
                    etTitle.error = "Enter title"
                    return@setOnClickListener
                }

                val reminderType = ReminderType.values().getOrNull(typeIndex) ?: ReminderType.TASK
                val priority = Priority.values().getOrNull(priorityIndex) ?: Priority.MEDIUM
                val recurrence = RecurrenceType.values().getOrNull(recurrenceIndex) ?: RecurrenceType.ONE_TIME

                // Allow today's date
                val todayStart = Calendar.getInstance()
                todayStart.set(Calendar.HOUR_OF_DAY, 0)
                todayStart.set(Calendar.MINUTE, 0)
                todayStart.set(Calendar.SECOND, 0)
                todayStart.set(Calendar.MILLISECOND, 0)

                if (selectedDate < todayStart.timeInMillis) {
                    Toast.makeText(requireContext(), "Please select today or a future date", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                var metadata: String? = null

                // Handle bill-specific metadata
                if (reminderType == ReminderType.BILL) {
                    val amountStr = etAmount.text.toString().trim()
                    if (amountStr.isEmpty()) {
                        etAmount.error = "Enter amount"
                        return@setOnClickListener
                    }
                    val amount = amountStr.toDoubleOrNull()
                    if (amount == null || amount <= 0 || amount > MAX_AMOUNT) {
                        etAmount.error = "Enter valid amount (1 to $MAX_AMOUNT)"
                        return@setOnClickListener
                    }
                    val categoryIndex = spinnerCategory.selectedItemPosition
                    val category = BillCategory.values().getOrNull(categoryIndex) ?: BillCategory.OTHER
                    metadata = Converters().billMetadataToJson(BillMetadata(amount = amount, category = category.name))
                }

                viewModel.createReminder(
                    title = title,
                    description = description,
                    dueDate = selectedDate,
                    reminderType = reminderType,
                    priority = priority,
                    recurrence = recurrence,
                    metadata = metadata
                )
                Toast.makeText(requireContext(), "Reminder added", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }

        dialog.show()
    }
}