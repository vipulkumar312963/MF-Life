package org.meerammafoundation.tools.budget.billSplitter

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromSplitType(value: SplitType): String {
        return value.name
    }

    @TypeConverter
    fun toSplitType(value: String): SplitType {
        return SplitType.valueOf(value)
    }
}