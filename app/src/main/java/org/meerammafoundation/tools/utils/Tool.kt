package org.meerammafoundation.tools.utils

/**
 * Data class representing a tool in the app.
 * @param id Unique identifier (e.g., "emi_calculator")
 * @param name Display name of the tool
 * @param icon Emoji icon
 * @param category Category name (e.g., "Financial")
 * @param targetActivity The Activity class to launch when the tool is clicked
 */
data class Tool(
    val id: String,
    val name: String,
    val icon: String,
    val category: String,
    val targetActivity: Class<*>
)