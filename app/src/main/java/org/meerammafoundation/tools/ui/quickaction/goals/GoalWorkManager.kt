package org.meerammafoundation.tools.ui.quickaction.goals

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.runBlocking
import java.util.concurrent.TimeUnit

class GoalWorkManager(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val database = GoalDatabase.getDatabase(context)
            val repository = GoalRepository(database)
            repository.regenerateRecurringGoalsIfNeeded()
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "goal_recurring_work"
        private const val TAG = "GoalWorkManager"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<GoalWorkManager>(
                1, TimeUnit.DAYS,
                15, TimeUnit.MINUTES // Flex interval - runs within 15 minutes of the scheduled time
            )
                .setConstraints(constraints)
                .setInitialDelay(1, TimeUnit.HOURS) // Small delay to ensure app is fully initialized
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP, // KEEP existing work, don't create duplicate
                workRequest
            )
        }
    }
}