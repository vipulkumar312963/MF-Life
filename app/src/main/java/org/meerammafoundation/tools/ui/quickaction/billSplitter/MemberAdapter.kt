package org.meerammafoundation.tools.ui.quickaction.billSplitter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.meerammafoundation.tools.R

class MemberAdapter(
    private val members: List<Member>,
    private val onDeleteClick: (Member) -> Unit
) : RecyclerView.Adapter<MemberAdapter.MemberViewHolder>() {

    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMemberName: TextView = itemView.findViewById(R.id.tvMemberName)
        private val tvMemberIcon: TextView = itemView.findViewById(R.id.tvMemberIcon)
        private val ivDelete: ImageView = itemView.findViewById(R.id.ivDeleteMember)

        fun bind(member: Member, onDelete: (Member) -> Unit) {
            tvMemberName.text = member.name
            tvMemberIcon.text = member.name.take(1).uppercase()
            ivDelete.setOnClickListener { onDelete(member) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.billsplitter_item_member, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        holder.bind(members[position], onDeleteClick)
    }

    override fun getItemCount() = members.size
}