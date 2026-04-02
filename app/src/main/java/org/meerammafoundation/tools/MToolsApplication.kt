package org.meerammafoundation.tools

import android.app.Application
import org.meerammafoundation.tools.ui.quickaction.reminder.TestNotificationHelper

class MToolsApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Create notification channel once when app starts
        TestNotificationHelper.createNotificationChannel(this)
    }
}