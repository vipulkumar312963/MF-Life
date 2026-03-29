package org.meerammafoundation.tools.favorites

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.children
import org.meerammafoundation.tools.R
import org.meerammafoundation.tools.utils.ToolRegistry

class AddFavoriteActivity : AppCompatActivity() {

    private lateinit var toolsContainer: LinearLayout
    private val categoryCheckboxes = mutableMapOf<String, CheckBox>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_add_favorite)

        val backButton = findViewById<TextView>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        toolsContainer = findViewById(R.id.toolsContainer)
        val saveButton = findViewById<Button>(R.id.saveButton)

        // Group tools by category
        val toolsByCategory = ToolRegistry.allTools.groupBy { it.category }

        val inflater = LayoutInflater.from(this)

        toolsByCategory.forEach { (category, tools) ->
            // Category header with "Select All" checkbox
            val categoryCheckBox = CheckBox(this).apply {
                text = category
                setTextColor(resources.getColor(R.color.primary, null))
                setTextSize(16f)
                setTypeface(null, Typeface.BOLD)
                setPadding(0, 16, 0, 8)
                isChecked = false
            }
            toolsContainer.addView(categoryCheckBox)
            categoryCheckboxes[category] = categoryCheckBox

            val toolCheckboxes = mutableListOf<CheckBox>()

            tools.forEach { tool ->
                // Inflate the custom item layout
                val itemView = inflater.inflate(R.layout.favorites_item_tool_select, toolsContainer, false)
                val iconView = itemView.findViewById<TextView>(R.id.toolIcon)
                val nameView = itemView.findViewById<TextView>(R.id.toolName)
                val checkBox = itemView.findViewById<CheckBox>(R.id.toolCheckbox)

                iconView.text = tool.icon
                nameView.text = tool.name
                checkBox.tag = tool.id

                toolsContainer.addView(itemView)
                toolCheckboxes.add(checkBox)

                // When tool checkbox changes, update category "select all" state
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    val allChecked = toolCheckboxes.all { it.isChecked }
                    categoryCheckBox.isChecked = allChecked
                }
            }

            // Category "select all" listener
            categoryCheckBox.setOnCheckedChangeListener { _, isChecked ->
                toolCheckboxes.forEach { it.isChecked = isChecked }
            }
        }

        // Pre-check already favorited tools
        val currentFavorites = FavoritesManager.loadFavorites(this)
        toolsContainer.children.forEach { view ->
            val checkBox = view.findViewById<CheckBox>(R.id.toolCheckbox)
            if (checkBox != null) {
                val toolId = checkBox.tag as? String
                if (toolId != null && currentFavorites.contains(toolId)) {
                    checkBox.isChecked = true
                }
            }
        }

        // Save button
        saveButton.setOnClickListener {
            val selectedIds = mutableSetOf<String>()
            toolsContainer.children.forEach { view ->
                val checkBox = view.findViewById<CheckBox>(R.id.toolCheckbox)
                if (checkBox != null && checkBox.isChecked) {
                    val toolId = checkBox.tag as? String
                    if (toolId != null) selectedIds.add(toolId)
                }
            }
            FavoritesManager.saveFavorites(this, selectedIds)
            Toast.makeText(this, "Favorites updated!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}