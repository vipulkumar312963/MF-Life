package org.meerammafoundation.tools.budget.reminder

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import org.meerammafoundation.tools.BuildConfig

@Database(
    entities = [BillReminder::class],
    version = 3,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class BillReminderDatabase : RoomDatabase() {

    abstract fun billReminderDao(): BillReminderDao

    companion object {
        private const val DB_NAME = "bill_reminder_database"

        @Volatile
        private var INSTANCE: BillReminderDatabase? = null

        // ✅ Migration from version 1 to 2 (no try-catch - let Room handle errors)
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add last_notified_at column with DEFAULT NULL
                database.execSQL(
                    "ALTER TABLE bill_reminders ADD COLUMN last_notified_at INTEGER DEFAULT NULL"
                )

                // Create indices for better performance
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS idx_bill_reminders_due_date ON bill_reminders(due_date)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS idx_bill_reminders_is_paid ON bill_reminders(is_paid)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS idx_bill_reminders_snoozed_until ON bill_reminders(snoozed_until)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS idx_bill_reminders_last_notified_at ON bill_reminders(last_notified_at)"
                )
            }
        }

        fun getDatabase(context: Context): BillReminderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = buildDatabase(context)
                INSTANCE = instance
                instance
            }
        }

        private fun buildDatabase(context: Context): BillReminderDatabase {
            val builder = Room.databaseBuilder(
                context.applicationContext,
                BillReminderDatabase::class.java,
                DB_NAME
            )

            // ✅ Add both migrations
            builder.addMigrations(
                BillReminderMigrations.MIGRATION_1_2,
                BillReminderMigrations.MIGRATION_2_3
            )

            // ✅ Only allow destructive migration in debug mode
            if (BuildConfig.DEBUG) {
                builder.fallbackToDestructiveMigration()
            } else {
                builder.fallbackToDestructiveMigrationOnDowngrade()
            }

            builder.setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)

            return builder.build()
        }
    }
}