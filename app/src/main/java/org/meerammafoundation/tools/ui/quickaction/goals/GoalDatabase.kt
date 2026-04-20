package org.meerammafoundation.tools.ui.quickaction.goals

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Goal::class],
    version = 3,  // ✅ Changed from 1 to 2
    exportSchema = false
)
abstract class GoalDatabase : RoomDatabase() {

    abstract fun goalDao(): GoalDao

    companion object {
        private const val DB_NAME = "goals_database"

        @Volatile
        private var INSTANCE: GoalDatabase? = null

        fun getDatabase(context: Context): GoalDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GoalDatabase::class.java,
                    DB_NAME
                )
                    .fallbackToDestructiveMigration()  // ✅ Clears old data, creates fresh schema
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}