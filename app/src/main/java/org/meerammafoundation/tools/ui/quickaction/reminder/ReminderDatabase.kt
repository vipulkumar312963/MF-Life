package org.meerammafoundation.tools.ui.quickaction.reminder

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.meerammafoundation.tools.BuildConfig

@Database(
    entities = [Reminder::class],
    version = 1,  // Fresh start with version 1
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ReminderDatabase : RoomDatabase() {

    abstract fun reminderDao(): ReminderDao

    companion object {
        private const val DB_NAME = "reminder_database"

        @Volatile
        private var INSTANCE: ReminderDatabase? = null

        fun getDatabase(context: Context): ReminderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = buildDatabase(context)
                INSTANCE = instance
                instance
            }
        }

        private fun buildDatabase(context: Context): ReminderDatabase {
            val builder = Room.databaseBuilder(
                context.applicationContext,
                ReminderDatabase::class.java,
                DB_NAME
            )

            // ✅ Fresh start - fallback to destructive migration
            // This will delete old tables and create new ones
            builder.fallbackToDestructiveMigration()

            builder.setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)

            return builder.build()
        }
    }
}