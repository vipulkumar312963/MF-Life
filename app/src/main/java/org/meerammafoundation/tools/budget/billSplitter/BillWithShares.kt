package org.meerammafoundation.tools.budget.billSplitter

import androidx.room.Embedded
import androidx.room.Relation

data class BillWithShares(
    @Embedded val bill: Bill,
    @Relation(
        parentColumn = "id",
        entityColumn = "billId"
    )
    val shares: List<BillShare>
)