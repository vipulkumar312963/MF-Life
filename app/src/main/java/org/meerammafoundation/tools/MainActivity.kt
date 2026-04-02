package org.meerammafoundation.tools

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import org.meerammafoundation.tools.ui.favorites.FavoritesFragment
import org.meerammafoundation.tools.ui.main.MainMenuFragment
import org.meerammafoundation.tools.ui.quickaction.billSplitter.BillSplitterFragment
import org.meerammafoundation.tools.ui.quickaction.goals.GoalsFragment
import org.meerammafoundation.tools.ui.quickaction.reminder.ReminderFragment

class MainActivity : AppCompatActivity() {

    private lateinit var headerTitle: TextView
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_container)

        headerTitle = findViewById(R.id.headerTitle)

        // Listen for back stack changes to update header
        supportFragmentManager.addOnBackStackChangedListener {
            val currentFrag = supportFragmentManager.findFragmentById(R.id.contentFrame)
            updateHeaderForFragment(currentFrag)
        }

        setupHeaderListeners()
        setupFooterListeners()

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
                // If fragment is null or unknown, check if back stack is empty
                if (supportFragmentManager.backStackEntryCount == 0) {
                    headerTitle.text = getString(R.string.app_title)
                }
            }
        }
    }

    private fun setupHeaderListeners() {
        findViewById<TextView>(R.id.hamburgerMenu).setOnClickListener {
            showMessage("Menu - Coming Soon!")
        }

        findViewById<TextView>(R.id.notificationIcon).setOnClickListener {
            showMessage("Notifications - Coming Soon!")
        }

        findViewById<TextView>(R.id.profileIcon).setOnClickListener {
            showMessage("Profile - Coming Soon!")
        }
    }

    private fun setupFooterListeners() {
        findViewById<TextView>(R.id.footerMyMarks)?.setOnClickListener {
            showMessage("My Marks - Coming Soon!")
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

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            // Pop the current fragment
            supportFragmentManager.popBackStack()
            // Header will be updated by the addOnBackStackChangedListener
        } else {
            // No fragments in back stack, exit app
            finish()
        }
    }
}