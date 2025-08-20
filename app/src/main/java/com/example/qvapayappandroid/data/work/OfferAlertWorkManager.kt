package com.example.qvapayappandroid.data.work

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class OfferAlertWorkManager(private val context: Context) {

    companion object {
        private const val UNIQUE_WORK_NAME = "offer_check_periodic_work"
    }

    fun startPeriodicChecks(intervalMinutes: Long = 30) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<OfferCheckWorker>(
            repeatInterval = maxOf(intervalMinutes, 15), // Mínimo 15 minutos
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag("offer_alerts")
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest
            )
    }

    fun stopPeriodicChecks() {
        WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    fun scheduleOneTimeCheck() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<OfferCheckWorker>()
            .setConstraints(constraints)
            .addTag("offer_alerts_immediate")
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }

    fun getWorkStatus(): Flow<List<WorkInfo>> {
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow(UNIQUE_WORK_NAME)
    }

    fun isWorkScheduled(): Boolean {
        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(UNIQUE_WORK_NAME)
            .get()
        
        return workInfos.any { workInfo ->
            workInfo.state == WorkInfo.State.RUNNING || workInfo.state == WorkInfo.State.ENQUEUED
        }
    }
}