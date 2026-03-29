package org.meerammafoundation.tools.favorites

import android.content.Context
import android.content.SharedPreferences

object FavoritesManager {
    private const val PREFS_NAME = "favorites_prefs"
    private const val KEY_FAVORITES = "favorites_set"

    fun saveFavorites(context: Context, favoriteIds: Set<String>) {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putStringSet(KEY_FAVORITES, favoriteIds).apply()
    }

    fun loadFavorites(context: Context): Set<String> {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()
    }

    fun addFavorite(context: Context, toolId: String) {
        val current = loadFavorites(context).toMutableSet()
        current.add(toolId)
        saveFavorites(context, current)
    }

    fun removeFavorite(context: Context, toolId: String) {
        val current = loadFavorites(context).toMutableSet()
        current.remove(toolId)
        saveFavorites(context, current)
    }

    fun isFavorite(context: Context, toolId: String): Boolean {
        return loadFavorites(context).contains(toolId)
    }
}