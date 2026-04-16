package org.meerammafoundation.tools

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.meerammafoundation.tools.data.PreferencesManager
import org.meerammafoundation.tools.ui.favorites.FavoritesFragment
import org.meerammafoundation.tools.ui.main.MainMenuFragment
import org.meerammafoundation.tools.ui.quickaction.billSplitter.BillSplitterFragment
import org.meerammafoundation.tools.ui.quickaction.goals.GoalsFragment
import org.meerammafoundation.tools.ui.quickaction.reminder.ReminderFragment

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var headerTitle: TextView
    private lateinit var preferencesManager: PreferencesManager
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply saved language BEFORE setContentView
        preferencesManager = PreferencesManager(this)
        applySavedLanguage()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_container)

        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navView)
        headerTitle = findViewById(R.id.headerTitle)

        // Listen for back stack changes to update header
        supportFragmentManager.addOnBackStackChangedListener {
            val currentFrag = supportFragmentManager.findFragmentById(R.id.contentFrame)
            updateHeaderForFragment(currentFrag)
        }

        setupHeaderListeners()
        setupFooterListeners()
        setupNavigationDrawer()

        if (savedInstanceState == null) {
            val loadFragment = intent.getStringExtra("load_fragment")
            when (loadFragment) {
                "reminder" -> {
                    loadFragment(ReminderFragment.newInstance(), "Reminders", true)
                }
                "bill_splitter" -> {
                    loadFragment(BillSplitterFragment(), "Bill Splitter", true)
                }
                "favorites" -> {
                    loadFragment(FavoritesFragment(), "My Favorites", true)
                }
                "goals" -> {
                    loadFragment(GoalsFragment.newInstance(), "Goals", true)
                }
                else -> {
                    loadFragment(MainMenuFragment(), getString(R.string.app_title), false)
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
            // Default to English
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

        // Update menu text based on current preferences
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

                    // Apply language and restart activity
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
            showMessage("Notifications - Coming Soon!")
        }

        findViewById<TextView>(R.id.profileIcon).setOnClickListener {
            showMessage("Profile - Coming Soon!")
        }
    }

    private fun setupFooterListeners() {
        // Home - Navigate to Main Menu
        findViewById<TextView>(R.id.footerHome)?.setOnClickListener {
            // Clear back stack and go to home
            supportFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)
            loadFragment(MainMenuFragment(), getString(R.string.app_title), false)
        }

        // Favorite
        findViewById<TextView>(R.id.footerFavorite)?.setOnClickListener {
            val favoritesFragment = FavoritesFragment()
            loadFragment(favoritesFragment, "My Favorites", true)
        }

        // Search
        findViewById<TextView>(R.id.footerSearch)?.setOnClickListener {
            showMessage("Search - Coming Soon!")
        }

        // Donate
        findViewById<TextView>(R.id.footerDonate)?.setOnClickListener {
            val donateUrl = "https://docs.google.com/forms/d/e/1FAIpQLSfazrD_iQVz1uftCxrDfxCaySaUEPTyl_uRe1WXYQVO3yWdOg/viewform"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(donateUrl))
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            finish()
        }
    }
}