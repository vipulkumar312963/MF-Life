package org.meerammafoundation.tools.budget.billSplitter

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "groups",
    indices = [
        Index("created_at")  // Optional: speeds up sorting by date for large datasets
    ]
)
data class Group(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)