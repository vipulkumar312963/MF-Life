package org.meerammafoundation.tools.budget.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.*
import org.meerammafoundation.tools.BuildConfig
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_LOCKED_BOOT_COMPLETED -> {

                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()

                // Match interval with ViewModel (15 min debug / 24 hours release)
                val interval = if (BuildConfig.DEBUG) 15L else 24L
                val unit = if (BuildConfig.DEBUG) TimeUnit.MINUTES else TimeUnit.HOURS

                val workRequest = PeriodicWorkRequestBuilder<BillReminderWorker>(
                    interval, unit
                )
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(context.applicationContext)
                    .enqueueUniquePeriodicWork(
                        "bill_reminder_check",
                        ExistingPeriodicWorkPolicy.KEEP,
                        workRequest
                    )
            }
        }
    }
}