package org.meerammafoundation.tools.ui.quickaction.billSplitter

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bill_shares",
    foreignKeys = [
        ForeignKey(
            entity = Bill::class,
            parentColumns = ["id"],
            childColumns = ["billId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("billId"),                          // Speed up queries by bill
        Index("memberId"),                         // Speed up queries by member
        Index(value = ["billId", "memberId"], unique = true)  // Prevent duplicate shares
    ]
)
data class BillShare(

    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val billId: Long,

    val memberId: Long,

    @ColumnInfo(name = "share_amount")
    val shareAmount: Double
)