package org.meerammafoundation.tools.notifications

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.meerammafoundation.tools.R

object NotificationDialog {

    fun show(
        context: Context,
        notifications: List<NotificationItem>,
        onItemClick: (NotificationItem) -> Unit
    ): AlertDialog {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_notifications, null)
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.notificationRecyclerView)

        recyclerView.layoutManager = LinearLayoutManager(context)

        val adapter = NotificationAdapter(notifications, onItemClick)
        recyclerView.adapter = adapter

        val unreadCount = notifications.count { !it.isRead }
        val title = when {
            unreadCount == 0 -> "Notifications (${notifications.size})"
            unreadCount == 1 -> "1 New Notification"
            else -> "$unreadCount New Notifications"
        }

        return MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton("Close") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}