package org.meerammafoundation.tools

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.meerammafoundation.tools.data.PreferencesManager
import org.meerammafoundation.tools.notifications.*
import org.meerammafoundation.tools.ui.favorites.FavoritesFragment
import org.meerammafoundation.tools.ui.main.MainMenuFragment
import org.meerammafoundation.tools.ui.quickaction.billSplitter.BillSplitterFragment
import org.meerammafoundation.tools.ui.quickaction.goals.GoalDatabase
import org.meerammafoundation.tools.ui.quickaction.goals.GoalRepository
import org.meerammafoundation.tools.ui.quickaction.goals.GoalsFragment
import org.meerammafoundation.tools.ui.quickaction.reminder.*

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var headerTitle: TextView
    private lateinit var notificationBadge: TextView
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var readPrefs: SharedPreferences
    private var currentFragment: Fragment? = null
    private var currentNotificationDialog: AlertDialog? = null
    private var isOpeningFromNotification = false

    companion object {
        private const val PREFS_NAME = "notification_prefs"
        private const val KEY_READ_NOTIFICATIONS = "read_notifications"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        preferencesManager = PreferencesManager(this)
        applySavedLanguage()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_container)

        readPrefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navView)
        headerTitle = findViewById(R.id.headerTitle)
        notificationBadge = findViewById(R.id.notificationBadge)

        NotificationChannelManager.createAllChannels(this)

        supportFragmentManager.addOnBackStackChangedListener {
            val currentFrag = supportFragmentManager.findFragmentById(R.id.contentFrame)
            updateHeaderForFragment(currentFrag)
        }

        setupHeaderListeners()
        setupFooterListeners()
        setupNavigationDrawer()

        // Handle intent from notification
        handleNotificationIntent(intent)

        if (savedInstanceState == null && !isOpeningFromNotification) {
            loadFragment(MainMenuFragment(), getString(R.string.app_title), false)
        }

        updateNotificationBadge()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleNotificationIntent(intent: Intent?) {
        intent?.let {
            val loadFragment = it.getStringExtra("load_fragment")
            val sourceId = it.getLongExtra("reminder_id", -1)

            when (loadFragment) {
                "reminder" -> {
                    isOpeningFromNotification = true
                    // Clear back stack and load main menu as root, then add reminder
                    supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

                    // First load main menu as root (not added to back stack)
                    loadFragment(MainMenuFragment(), getString(R.string.app_title), false)

                    // Then load reminder fragment (added to back stack)
                    loadFragment(ReminderFragment.newInstance(), "Reminders", true)

                    if (sourceId != -1L) {
                        markNotificationAsRead("reminder", sourceId)
                    }
                }
                "goals" -> {
                    isOpeningFromNotification = true
                    supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

                    loadFragment(MainMenuFragment(), getString(R.string.app_title), false)
                    loadFragment(GoalsFragment.newInstance(), "Goals", true)

                    if (sourceId != -1L) {
                        markNotificationAsRead("goal", sourceId)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateNotificationBadge()
    }

    private fun isNotificationRead(sourceType: String, sourceId: Long): Boolean {
        val readSet = readPrefs.getStringSet(KEY_READ_NOTIFICATIONS, emptySet()) ?: emptySet()
        return readSet.contains("${sourceType}_${sourceId}")
    }

    private fun markNotificationAsRead(sourceType: String, sourceId: Long) {
        val readSet = readPrefs.getStringSet(KEY_READ_NOTIFICATIONS, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        readSet.add("${sourceType}_${sourceId}")
        readPrefs.edit().putStringSet(KEY_READ_NOTIFICATIONS, readSet).apply()
        updateNotificationBadge()
    }

    private fun updateNotificationBadge() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val notifications = buildNotificationList()
                val unreadCount = notifications.count { !it.isRead }

                withContext(Dispatchers.Main) {
                    if (unreadCount > 0) {
                        notificationBadge.text = if (unreadCount > 99) "99+" else unreadCount.toString()
                        notificationBadge.visibility = View.VISIBLE
                    } else {
                        notificationBadge.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    notificationBadge.visibility = View.GONE
                }
            }
        }
    }

    private fun applySavedLanguage() {
        try {
            val savedLanguage = runBlocking {
                preferencesManager.getLanguage()
            }
            preferencesManager.applyLanguage(savedLanguage)
        } catch (e: Exception) {
            preferencesManager.applyLanguage("en")
        }
    }

    private fun setupNavigationDrawer() {
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_language -> showLanguageDialog()
                R.id.nav_currency -> showCurrencyDialog()
                R.id.nav_usage_policy -> showLegalPage("Usage Policy", getUsagePolicyText())
                R.id.nav_privacy_policy -> showLegalPage("Privacy Policy", getPrivacyPolicyText())
                R.id.nav_terms_conditions -> showLegalPage("Terms & Conditions", getTermsText())
                R.id.nav_about -> showLegalPage("About", getAboutText())
                R.id.nav_contact -> contactUs()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        updateMenuTexts()
    }

    private fun updateMenuTexts() {
        lifecycleScope.launch {
            val currentLang = preferencesManager.languageFlow.first()
            val currentCurrency = preferencesManager.currencyFlow.first()

            val languageMenu = navView.menu.findItem(R.id.nav_language)
            val currencyMenu = navView.menu.findItem(R.id.nav_currency)

            languageMenu?.title = "Language: ${if (currentLang == "en") "English" else "हिंदी"}"
            currencyMenu?.title = "Currency: $currentCurrency"
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("English", "हिंदी")
        val languageCodes = arrayOf("en", "hi")

        MaterialAlertDialogBuilder(this)
            .setTitle("Select Language")
            .setItems(languages) { _, which ->
                val language = languageCodes[which]
                lifecycleScope.launch {
                    preferencesManager.saveLanguage(language)
                    updateMenuTexts()
                    preferencesManager.applyLanguage(language)
                    recreate()
                    Toast.makeText(this@MainActivity,
                        "Language changed to ${languages[which]}",
                        Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCurrencyDialog() {
        val currencies = arrayOf("INR (₹)", "USD ($)", "EUR (€)")
        val currencyCodes = arrayOf("INR", "USD", "EUR")

        MaterialAlertDialogBuilder(this)
            .setTitle("Select Currency")
            .setItems(currencies) { _, which ->
                val currency = currencyCodes[which]
                lifecycleScope.launch {
                    preferencesManager.saveCurrency(currency)
                    updateMenuTexts()
                    Toast.makeText(this@MainActivity, "Currency changed to $currency", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLegalPage(title: String, content: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(content)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun contactUs() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:support@meerammafoundation.org")
            putExtra(Intent.EXTRA_SUBJECT, "Support Request - Meeramma Tools")
        }
        startActivity(intent)
    }

    private fun getUsagePolicyText(): String {
        return """
            Usage Policy
            
            1. This app is provided for personal use only.
            2. You may use the app for tracking bills, reminders, and goals.
            3. Do not misuse any features or attempt to reverse engineer the app.
            4. The app is free and contains no ads.
            5. Please report any bugs or issues to support.
            
            Thank you for using Meeramma Tools!
        """.trimIndent()
    }

    private fun getPrivacyPolicyText(): String {
        return """
            Privacy Policy
            
            Data Collection:
            • All your data is stored locally on your device
            • We do not collect or share any personal information
            • No data is sent to any server
            
            Permissions:
            • Notifications: To remind you about bills and tasks
            • Storage: To backup your data (if you choose to)
            
            Your data remains yours. Always.
        """.trimIndent()
    }

    private fun getTermsText(): String {
        return """
            Terms & Conditions
            
            1. This app is provided "as is" without any warranties.
            2. We are not responsible for any financial decisions made using this app.
            3. You agree to use this app responsibly.
            4. We reserve the right to update these terms.
            
            By using this app, you agree to these terms.
        """.trimIndent()
    }

    private fun getAboutText(): String {
        return """
            Meeramma Tools
            
            Version 1.4
            
            A free, offline, ad-free toolkit for financial planning,
            reminders, goals, and daily productivity.
            
            Developed with ❤️ by Meeramma Foundation
            
            For support: support@meerammafoundation.org
        """.trimIndent()
    }

    fun loadFragment(fragment: Fragment, title: String, addToBackStack: Boolean = true) {
        headerTitle.text = title
        currentFragment = fragment

        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.contentFrame, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }

    fun setHeaderTitle(title: String) {
        headerTitle.text = title
    }

    fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun updateHeaderForFragment(fragment: Fragment?) {
        when (fragment) {
            is MainMenuFragment -> headerTitle.text = getString(R.string.app_title)
            is ReminderFragment -> headerTitle.text = "Reminders"
            is BillSplitterFragment -> headerTitle.text = "Bill Splitter"
            is GoalsFragment -> headerTitle.text = "Goals"
            is FavoritesFragment -> headerTitle.text = "My Favorites"
            else -> {
                if (supportFragmentManager.backStackEntryCount == 0) {
                    headerTitle.text = getString(R.string.app_title)
                }
            }
        }
    }

    private fun setupHeaderListeners() {
        findViewById<TextView>(R.id.hamburgerMenu).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        findViewById<TextView>(R.id.notificationIcon).setOnClickListener {
            showUnifiedNotificationsDialog()
        }
    }

    private fun setupFooterListeners() {
        findViewById<TextView>(R.id.footerHome)?.setOnClickListener {
            clearBackStackAndGoHome()
        }

        findViewById<TextView>(R.id.footerFavorite)?.setOnClickListener {
            val favoritesFragment = FavoritesFragment()
            loadFragment(favoritesFragment, "My Favorites", true)
        }

        findViewById<TextView>(R.id.footerSearch)?.setOnClickListener {
            showMessage("Search - Coming Soon!")
        }

        findViewById<TextView>(R.id.footerDonate)?.setOnClickListener {
            val donateUrl = "https://docs.google.com/forms/d/e/1FAIpQLSfazrD_iQVz1uftCxrDfxCaySaUEPTyl_uRe1WXYQVO3yWdOg/viewform"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(donateUrl))
            startActivity(intent)
        }
    }

    private fun clearBackStackAndGoHome() {
        supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        loadFragment(MainMenuFragment(), getString(R.string.app_title), false)
        isOpeningFromNotification = false
    }

    // ==================== UNIFIED NOTIFICATION SYSTEM ====================

    private fun formatValue(value: Double, unit: String): String {
        return when (unit) {
            "₹" -> "₹${String.format("%.0f", value)}"
            "%" -> "${String.format("%.0f", value)}%"
            "kg" -> "${String.format("%.1f", value)} kg"
            "km" -> "${String.format("%.1f", value)} km"
            "books" -> "${String.format("%.0f", value)} books"
            else -> "${String.format("%.0f", value)} $unit"
        }
    }

    private suspend fun buildNotificationList(): List<NotificationItem> {
        val notifications = mutableListOf<NotificationItem>()
        val currentTime = System.currentTimeMillis()
        val dayMs = 24L * 60L * 60L * 1000L

        // Get Reminder notifications
        try {
            val reminderDatabase = ReminderDatabase.getDatabase(this@MainActivity)
            val reminderRepository = ReminderRepository(reminderDatabase)
            val allReminders = reminderRepository.getAllReminders().first()

            allReminders.filter { !it.isCompleted }.forEach { reminder ->
                val daysUntilDue = ((reminder.dueDate - currentTime) / dayMs).toInt()
                val priority = when {
                    daysUntilDue < 0 -> NotificationPriority.HIGH
                    daysUntilDue == 0 -> NotificationPriority.HIGH
                    daysUntilDue <= 3 -> NotificationPriority.MEDIUM
                    else -> NotificationPriority.LOW
                }

                val message = when (reminder.reminderType) {
                    ReminderType.BILL -> {
                        try {
                            val metadata = Converters().jsonToBillMetadata(reminder.metadata ?: "")
                            "Amount: ₹${String.format("%.2f", metadata.amount)}"
                        } catch (e: Exception) { "" }
                    }
                    else -> reminder.description ?: ""
                }

                val isRead = isNotificationRead("reminder", reminder.id)

                notifications.add(
                    NotificationItem(
                        id = reminder.id,
                        title = reminder.title,
                        message = message,
                        time = reminder.dueDate,
                        type = NotificationType.REMINDER,
                        priority = priority,
                        sourceId = reminder.id,
                        sourceType = "reminder",
                        isRead = isRead
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Get Goal notifications
        try {
            val goalDatabase = GoalDatabase.getDatabase(this@MainActivity)
            val goalRepository = GoalRepository(goalDatabase)
            val allGoals = goalRepository.getAllGoals().first()

            allGoals.filter { !it.isCompleted }.forEach { goal ->
                val progress = (goal.currentValue / goal.targetValue * 100).toInt()
                val isRead = isNotificationRead("goal", goal.id)

                val (title, message, type, priority) = when {
                    progress >= 100 -> arrayOf(
                        "🎉 Goal Completed! 🎉",
                        "Congratulations! You've completed '${goal.title}'!",
                        NotificationType.GOAL_COMPLETED,
                        NotificationPriority.HIGH
                    )
                    progress >= 75 -> arrayOf(
                        "Goal Milestone: 75%",
                        "You're 75% done with '${goal.title}'! Keep going!",
                        NotificationType.GOAL_MILESTONE,
                        NotificationPriority.MEDIUM
                    )
                    progress >= 50 -> arrayOf(
                        "Goal Milestone: 50%",
                        "Halfway there! '${goal.title}' is 50% complete!",
                        NotificationType.GOAL_MILESTONE,
                        NotificationPriority.MEDIUM
                    )
                    progress >= 25 -> arrayOf(
                        "Goal Milestone: 25%",
                        "Great start! You're 25% done with '${goal.title}'!",
                        NotificationType.GOAL_MILESTONE,
                        NotificationPriority.MEDIUM
                    )
                    else -> arrayOf(
                        "Goal: ${goal.title}",
                        "Progress: ${progress}% complete (${formatValue(goal.currentValue, goal.unit)} of ${formatValue(goal.targetValue, goal.unit)})",
                        NotificationType.GOAL,
                        NotificationPriority.LOW
                    )
                }

                notifications.add(
                    NotificationItem(
                        id = goal.id,
                        title = title as String,
                        message = message as String,
                        time = System.currentTimeMillis(),
                        type = type as NotificationType,
                        priority = priority as NotificationPriority,
                        sourceId = goal.id,
                        sourceType = "goal",
                        isRead = isRead
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return notifications.sortedWith(
            compareByDescending<NotificationItem> { !it.isRead }
                .thenByDescending { it.priority }
                .thenBy { it.time }
        )
    }

    private fun showUnifiedNotificationsDialog() {
        currentNotificationDialog?.dismiss()

        CoroutineScope(Dispatchers.IO).launch {
            val notifications = buildNotificationList()

            withContext(Dispatchers.Main) {
                if (notifications.isEmpty()) {
                    showMessage("No notifications")
                    return@withContext
                }

                currentNotificationDialog = NotificationDialog.show(
                    context = this@MainActivity,
                    notifications = notifications,
                    onItemClick = { item ->
                        currentNotificationDialog?.dismiss()
                        currentNotificationDialog = null
                        if (!item.isRead) {
                            markNotificationAsRead(item.sourceType, item.sourceId)
                        }
                        when (item.sourceType) {
                            "reminder" -> {
                                loadFragment(ReminderFragment.newInstance(), "Reminders", true)
                            }
                            "goal" -> {
                                loadFragment(GoalsFragment.newInstance(), "Goals", true)
                            }
                        }
                    }
                )

                currentNotificationDialog?.setOnDismissListener {
                    currentNotificationDialog = null
                    updateNotificationBadge()
                }
            }
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
            val currentFrag = supportFragmentManager.findFragmentById(R.id.contentFrame)
            if (currentFrag is MainMenuFragment) {
                headerTitle.text = getString(R.string.app_title)
                isOpeningFromNotification = false
            }
        } else {
            finish()
        }
    }
}