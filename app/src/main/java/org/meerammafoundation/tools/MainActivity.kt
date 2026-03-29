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

class MainActivity : AppCompatActivity() {

    private lateinit var headerTitle: TextView
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_container)

        // Initialize header title
        headerTitle = findViewById(R.id.headerTitle)

        // Setup click listeners
        setupHeaderListeners()
        setupFooterListeners()

        // Load initial fragment if not restored
        if (savedInstanceState == null) {
            loadFragment(MainMenuFragment(), "Meeramma Tools")
        }
    }

    /**
     * Load a fragment into the container
     * @param fragment The fragment to load
     * @param title The title to display in header
     * @param addToBackStack Whether to add to back stack (default: true)
     */
    fun loadFragment(fragment: Fragment, title: String, addToBackStack: Boolean = true) {
        // Update header title
        headerTitle.text = title

        // Store current fragment
        currentFragment = fragment

        // Begin fragment transaction
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.contentFrame, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commit()
    }

    /**
     * Update header title dynamically
     */
    fun setHeaderTitle(title: String) {
        headerTitle.text = title
    }

    /**
     * Show a toast message (convenience method)
     */
    fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Setup header click listeners (hamburger menu, notification, profile)
     */
    private fun setupHeaderListeners() {
        // Hamburger menu
        findViewById<TextView>(R.id.hamburgerMenu).setOnClickListener {
            showMessage("Menu - Coming Soon!")
            // TODO: Open navigation drawer
        }

        // Notification icon
        findViewById<TextView>(R.id.notificationIcon).setOnClickListener {
            showMessage("Notifications - Coming Soon!")
            // TODO: Show notifications panel
        }

        // Profile icon
        findViewById<TextView>(R.id.profileIcon).setOnClickListener {
            showMessage("Profile - Coming Soon!")
            // TODO: Open profile/login screen
        }
    }

    /**
     * Setup footer click listeners (My Marks, Favorite, Search, Donate)
     */
    private fun setupFooterListeners() {
        // My Marks
        findViewById<TextView>(R.id.footerMyMarks)?.setOnClickListener {
            showMessage("My Marks - Coming Soon!")
            // TODO: Open My Marks screen
        }

        // Favorite
        findViewById<TextView>(R.id.footerFavorite)?.setOnClickListener {
            // Navigate to Favorites fragment
            val favoritesFragment = FavoritesFragment()
            loadFragment(favoritesFragment, "My Favorites")
        }

        // Search
        findViewById<TextView>(R.id.footerSearch)?.setOnClickListener {
            showMessage("Search - Coming Soon!")
            // TODO: Open global search
        }

        // Donate
        findViewById<TextView>(R.id.footerDonate)?.setOnClickListener {
            val donateUrl = "https://docs.google.com/forms/d/e/1FAIpQLSfazrD_iQVz1uftCxrDfxCaySaUEPTyl_uRe1WXYQVO3yWdOg/viewform"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(donateUrl))
            startActivity(intent)
        }
    }

    /**
     * Handle back button press
     * If back stack has more than one entry, pop back
     * Otherwise, finish activity (exit app)
     */
    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount <= 1) {
            // At root, exit app
            finish()
        } else {
            super.onBackPressed()
            // Update header title based on current fragment after back press
            val currentFrag = supportFragmentManager.findFragmentById(R.id.contentFrame)
            when (currentFrag) {
                is MainMenuFragment -> headerTitle.text = "Meeramma Tools"
                is FavoritesFragment -> headerTitle.text = "My Favorites"
                else -> { /* Keep existing title */ }
            }
        }
    }
}