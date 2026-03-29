package org.meerammafoundation.tools.budget.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import kotlinx.coroutines.*

class NotificationReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MARK_AS_PAID = "MARK_AS_PAID"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        when (intent.action) {
            ACTION_MARK_AS_PAID -> {
                val billId = intent.getLongExtra("bill_id", -1)
                val billName = intent.getStringExtra("bill_name") ?: "Bill"

                if (billId != -1L) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val database = BillReminderDatabase.getDatabase(
                                context.applicationContext
                            )
                            val repository = BillReminderRepository(database)
                            repository.markAsPaid(billId)

                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    context,
                                    "$billName marked as paid!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            // ✅ Pass Long directly - NotificationHelper expects Long
                            NotificationHelper.cancelNotification(context, billId)
                        } finally {
                            pendingResult.finish()
                        }
                    }
                } else {
                    pendingResult.finish()
                }
            }
            else -> pendingResult.finish()
        }
    }
}