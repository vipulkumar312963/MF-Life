package org.meerammafoundation.tools

import android.app.Application
import kotlinx.coroutines.runBlocking
import org.meerammafoundation.tools.data.PreferencesManager
import org.meerammafoundation.tools.ui.quickaction.goals.GoalWorkManager
import org.meerammafoundation.tools.ui.quickaction.reminder.TestNotificationHelper

class MToolsApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize PreferencesManager
        val preferencesManager = PreferencesManager(this)

        // Apply saved language on app start
        try {
            val savedLanguage = runBlocking {
                preferencesManager.getLanguage()
            }
            preferencesManager.applyLanguage(savedLanguage)
        } catch (e: Exception) {
            // Default to English if error
            preferencesManager.applyLanguage("en")
        }

        // Create notification channel once when app starts
        TestNotificationHelper.createNotificationChannel(this)

        // ✅ Schedule daily recurring goal check (runs once when app starts)
        GoalWorkManager.schedule(this)
    }
}