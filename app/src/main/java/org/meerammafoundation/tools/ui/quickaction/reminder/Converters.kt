package org.meerammafoundation.tools.ui.quickaction.reminder

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {

    private val gson = Gson()

    @TypeConverter
    fun fromReminderType(type: ReminderType): String = type.name

    @TypeConverter
    fun toReminderType(name: String): ReminderType = ReminderType.valueOf(name)

    @TypeConverter
    fun fromPriority(priority: Priority): String = priority.name

    @TypeConverter
    fun toPriority(name: String): Priority = Priority.valueOf(name)

    @TypeConverter
    fun fromRecurrenceType(recurrence: RecurrenceType): String = recurrence.name

    @TypeConverter
    fun toRecurrenceType(recurrence: String): RecurrenceType = RecurrenceType.valueOf(recurrence)

    @TypeConverter
    fun fromBillCategory(category: BillCategory): String = category.name

    @TypeConverter
    fun toBillCategory(category: String): BillCategory = BillCategory.valueOf(category)

    @TypeConverter
    fun fromBillMetadata(metadata: BillMetadata): String = gson.toJson(metadata)

    @TypeConverter
    fun toBillMetadata(json: String): BillMetadata {
        val type = object : TypeToken<BillMetadata>() {}.type
        return gson.fromJson(json, type)
    }

    fun billMetadataToJson(metadata: BillMetadata): String = gson.toJson(metadata)

    fun jsonToBillMetadata(json: String): BillMetadata {
        val type = object : TypeToken<BillMetadata>() {}.type
        return gson.fromJson(json, type)
    }
}

// Metadata data classes
data class BillMetadata(
    val amount: Double,
    val category: String
)

data class TaskMetadata(
    val estimatedMinutes: Int? = null,
    val tags: List<String> = emptyList()
)

data class EventMetadata(
    val location: String? = null,
    val durationMinutes: Int? = null
)

data class HabitMetadata(
    val targetDays: Int = 7,
    val currentStreak: Int = 0
)