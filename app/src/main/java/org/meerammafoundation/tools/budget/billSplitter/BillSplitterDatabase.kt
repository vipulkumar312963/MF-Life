package org.meerammafoundation.tools.budget.billSplitter

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Group::class, Member::class, Bill::class, BillShare::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BillSplitterDatabase : RoomDatabase() {

    abstract fun groupDao(): GroupDao
    abstract fun memberDao(): MemberDao
    abstract fun billDao(): BillDao
    abstract fun billShareDao(): BillShareDao

    companion object {

        private const val DB_NAME = "bill_splitter_database"

        @Volatile
        private var INSTANCE: BillSplitterDatabase? = null

        @Suppress("unused")
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Future migration: Add new columns/tables here
                // Example: database.execSQL("ALTER TABLE bills ADD COLUMN notes TEXT DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): BillSplitterDatabase {
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BillSplitterDatabase::class.java,
                    DB_NAME
                )
                    // Development only - Replace with .addMigrations(MIGRATION_1_2) before Play Store release
                    .fallbackToDestructiveMigration()

                    // Enable WAL for better performance
                    .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}