package org.meerammafoundation.tools.budget.reminder

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object BillReminderMigrations {

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL(
                "ALTER TABLE bill_reminders ADD COLUMN last_notified_at INTEGER DEFAULT NULL"
            )

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

    // ✅ Add migration from version 2 to 3 (if needed)
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // If you added any new columns in version 3, add them here
            // For now, version 3 just increments version number
            // No schema changes from 2 to 3
        }
    }
}