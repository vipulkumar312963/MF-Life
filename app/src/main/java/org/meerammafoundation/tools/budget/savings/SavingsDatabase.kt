package org.meerammafoundation.tools.budget.savings

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SavingsGoal::class],
    version = 1,
    exportSchema = false
)
abstract class SavingsDatabase : RoomDatabase() {

    abstract fun savingsGoalDao(): SavingsGoalDao

    companion object {
        private const val DB_NAME = "savings_database"

        @Volatile
        private var INSTANCE: SavingsDatabase? = null

        fun getDatabase(context: Context): SavingsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SavingsDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration()
                    .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}