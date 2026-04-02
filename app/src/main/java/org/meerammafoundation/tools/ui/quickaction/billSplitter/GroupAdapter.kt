package org.meerammafoundation.tools.ui.quickaction.billSplitter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.meerammafoundation.tools.R

class GroupAdapter(
    private var groups: List<Group>,
    private val onGroupClick: (Group) -> Unit,
    private val onGroupLongClick: (Group) -> Unit  // ✅ Added long-click listener
) : RecyclerView.Adapter<GroupAdapter.GroupViewHolder>() {

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvGroupName: TextView = itemView.findViewById(R.id.tvGroupName)
        private val tvMemberCount: TextView = itemView.findViewById(R.id.tvMemberCount)
        private val tvGroupIcon: TextView = itemView.findViewById(R.id.tvGroupIcon)

        fun bind(group: Group, onClick: (Group) -> Unit, onLongClick: (Group) -> Unit) {
            tvGroupName.text = group.name
            tvGroupIcon.text = group.name.take(1).uppercase()
            tvMemberCount.text = "Click to view"

            // ✅ Set click listeners
            itemView.setOnClickListener { onClick(group) }
            itemView.setOnLongClickListener {
                onLongClick(group)
                true  // Return true to consume the long press
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.billsplitter_item_group, parent, false)
        return GroupViewHolder(view)
    }

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(groups[position], onGroupClick, onGroupLongClick)
    }

    override fun getItemCount() = groups.size

    fun updateGroups(newGroups: List<Group>) {
        groups = newGroups
        notifyDataSetChanged()
    }
}