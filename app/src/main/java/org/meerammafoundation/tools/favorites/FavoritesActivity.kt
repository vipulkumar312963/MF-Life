package org.meerammafoundation.tools.favorites

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import org.meerammafoundation.tools.AboutActivity
import org.meerammafoundation.tools.MainActivity
import org.meerammafoundation.tools.R
import org.meerammafoundation.tools.ui.favorites.AddFavoriteActivity
import org.meerammafoundation.tools.ui.favorites.FavoritesManager
import org.meerammafoundation.tools.utils.Tool
import org.meerammafoundation.tools.utils.ToolRegistry

class FavoritesActivity : AppCompatActivity() {

    private lateinit var favoritesContainer: LinearLayout
    private lateinit var addFavoriteButton: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_favorites)

        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        favoritesContainer = findViewById(R.id.favoritesContainer)
        addFavoriteButton = findViewById(R.id.addFavoriteButton)

        addFavoriteButton.setOnClickListener {
            val intent = Intent(this, AddFavoriteActivity::class.java)
            startActivity(intent)
        }

        // Bottom navigation
        val bottomHome = findViewById<TextView>(R.id.bottomHome)
        val bottomAbout = findViewById<TextView>(R.id.bottomAbout)
        val bottomUpdate = findViewById<TextView>(R.id.bottomUpdate)

        bottomHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        bottomAbout.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        bottomUpdate.setOnClickListener {
            Toast.makeText(this, "Update screen - Coming Soon!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    private fun loadFavorites() {
        favoritesContainer.removeAllViews()
        val favoriteIds = FavoritesManager.loadFavorites(this)

        if (favoriteIds.isEmpty()) {
            // Show clean empty state
            val emptyView = layoutInflater.inflate(R.layout.favorites_item_favorite_empty, favoritesContainer, false)
            favoritesContainer.addView(emptyView)
            return
        }

        // Group favorites by category
        val favoriteTools = favoriteIds.mapNotNull { ToolRegistry.getToolById(it) }
        val grouped = favoriteTools.groupBy { it.category }

        grouped.forEach { (category, tools) ->
            // Category header
            val categoryHeader = TextView(this).apply {
                text = category
                setTextColor(resources.getColor(R.color.primary, null))
                setTextSize(16f)
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 16, 0, 8)
            }
            favoritesContainer.addView(categoryHeader)

            // Add each tool card (single column – full width)
            tools.forEach { tool ->
                val card = createFavoriteCard(tool)
                favoritesContainer.addView(card)
            }
        }
    }

    private fun createFavoriteCard(tool: Tool): CardView {
        return CardView(this).apply {
            setCardBackgroundColor(resources.getColor(R.color.card_background, null))
            radius = 12f
            cardElevation = 2f
            setContentPadding(16, 16, 16, 16)

            // Full width, wrap content height
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(6, 6, 6, 6)
            }

            val layout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }

            val iconView = TextView(context).apply {
                text = tool.icon
                textSize = 28f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { marginEnd = 16 }
            }
            layout.addView(iconView)

            val nameView = TextView(context).apply {
                text = tool.name
                textSize = 16f
                setTextColor(resources.getColor(R.color.text_primary, null))
                setTypeface(null, Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }
            layout.addView(nameView)

            val starView = TextView(context).apply {
                text = "⭐"
                textSize = 20f
                setTextColor(resources.getColor(R.color.primary, null))
            }
            layout.addView(starView)

            addView(layout)

            setOnClickListener {
                val intent = Intent(context, tool.targetActivity)
                context.startActivity(intent)
            }

            setOnLongClickListener {
                FavoritesManager.removeFavorite(context, tool.id)
                loadFavorites()
                Toast.makeText(context, "${tool.name} removed from favorites", Toast.LENGTH_SHORT).show()
                true
            }
        }
    }
}