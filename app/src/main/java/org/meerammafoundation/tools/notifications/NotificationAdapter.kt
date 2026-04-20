package org.meerammafoundation.tools.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.meerammafoundation.tools.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter(
    private var notifications: List<NotificationItem>,
    private val onItemClick: (NotificationItem) -> Unit
) : RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val notificationIcon: TextView = itemView.findViewById(R.id.notificationIcon)
        private val notificationTitle: TextView = itemView.findViewById(R.id.notificationTitle)
        private val notificationMessage: TextView = itemView.findViewById(R.id.notificationMessage)
        private val notificationTime: TextView = itemView.findViewById(R.id.notificationTime)

        fun bind(
            item: NotificationItem,
            dateFormat: SimpleDateFormat,
            onItemClick: (NotificationItem) -> Unit
        ) {
            notificationIcon.text = when (item.type) {
                NotificationType.REMINDER -> "🔔"
                NotificationType.GOAL -> "🎯"
                NotificationType.GOAL_MILESTONE -> "🎯"
                NotificationType.GOAL_COMPLETED -> "🎉"
                NotificationType.BIRTHDAY -> "🎂"
                else -> "📌"
            }

            notificationTitle.text = item.title
            notificationMessage.text = item.message
            notificationTime.text = dateFormat.format(Date(item.time))

            // Visual distinction for read/unread
            if (item.isRead) {
                notificationTitle.setTextColor(0xFF888888.toInt())
                notificationMessage.setTextColor(0xFFAAAAAA.toInt())
                itemView.setBackgroundColor(0x00000000)
            } else {
                notificationTitle.setTextColor(0xFF333333.toInt())
                notificationMessage.setTextColor(0xFF666666.toInt())
                itemView.setBackgroundColor(0x0A2196F3.toInt())
            }

            itemView.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(
            notifications[position],
            dateFormat,
            onItemClick
        )
    }

    override fun getItemCount() = notifications.size

    fun updateData(newNotifications: List<NotificationItem>) {
        notifications = newNotifications
        notifyDataSetChanged()
    }
}