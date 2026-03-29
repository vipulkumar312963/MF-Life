package org.meerammafoundation.tools.budget.reminder

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromBillCategory(category: BillCategory): String = category.name

    @TypeConverter
    fun toBillCategory(category: String): BillCategory = BillCategory.valueOf(category)

    @TypeConverter
    fun fromRecurrenceType(recurrence: RecurrenceType): String = recurrence.name

    @TypeConverter
    fun toRecurrenceType(recurrence: String): RecurrenceType = RecurrenceType.valueOf(recurrence)
}