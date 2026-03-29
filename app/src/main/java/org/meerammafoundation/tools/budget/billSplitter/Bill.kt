package org.meerammafoundation.tools.budget.billSplitter

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bills",
    foreignKeys = [
        ForeignKey(
            entity = Group::class,
            parentColumns = ["id"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["paidById"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("groupId"),      // Speed up queries by group
        Index("paidById")      // Speed up queries by payer
    ]
)
data class Bill(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val groupId: Long,

    val description: String,

    val amount: Double,

    val paidById: Long,

    val splitType: SplitType,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

enum class SplitType {
    EQUAL,
    CUSTOM
}