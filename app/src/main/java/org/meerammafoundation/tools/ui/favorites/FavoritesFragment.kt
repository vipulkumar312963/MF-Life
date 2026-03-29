package org.meerammafoundation.tools.ui.favorites

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import org.meerammafoundation.tools.MainActivity
import org.meerammafoundation.tools.R
import org.meerammafoundation.tools.favorites.AddFavoriteActivity
import org.meerammafoundation.tools.favorites.FavoritesManager
import org.meerammafoundation.tools.utils.Tool
import org.meerammafoundation.tools.utils.ToolRegistry

class FavoritesFragment : Fragment() {

    private lateinit var favoritesContainer: LinearLayout
    private lateinit var addFavoriteButton: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoritesContainer = view.findViewById(R.id.favoritesContainer)
        addFavoriteButton = view.findViewById(R.id.addFavoriteButton)

        addFavoriteButton.setOnClickListener {
            val intent = android.content.Intent(requireContext(), AddFavoriteActivity::class.java)
            startActivity(intent)
        }

        loadFavorites()
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }

    private fun loadFavorites() {
        favoritesContainer.removeAllViews()
        val favoriteIds = FavoritesManager.loadFavorites(requireContext())

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
            val categoryHeader = TextView(requireContext()).apply {
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
        return CardView(requireContext()).apply {
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
                val intent = android.content.Intent(context, tool.targetActivity)
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