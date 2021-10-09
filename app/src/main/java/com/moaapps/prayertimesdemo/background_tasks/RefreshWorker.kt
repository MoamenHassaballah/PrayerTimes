package com.moaapps.prayertimesdemo.background_tasks

import android.content.Context
import android.util.Log
import androidx.work.*
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

@ExperimentalCoroutinesApi
class RefreshWorker(private val appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {
    companion object{
        private const val TAG = "RefreshWorker"
    }

    override fun doWork(): Result {
        Log.d(TAG, "doWork: ")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val refreshRequest =
            PeriodicWorkRequestBuilder<RefreshWorker>(1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()

        val workManager = WorkManager.getInstance(appContext)
        workManager.enqueueUniquePeriodicWork("refresh", ExistingPeriodicWorkPolicy.KEEP, refreshRequest)


        var result: Result
        val getLocation = GetLocation(appContext)
        val getPrayerTimes = GetPrayerTimes(appContext)

        runBlocking {
            val location = getLocation.getUserLocation()
            Log.d(TAG, "doWork: loc: $location")

            result = if (location != null) {
                val prayerTimes =
                    getPrayerTimes.getPrayerTimes(location.latitude, location.longitude)
                Log.d(TAG, "doWork: prayer times: $prayerTimes")
                if (prayerTimes != null) Result.success() else Result.retry()
            } else {
                Result.retry()
            }
        }

        when (result) {
            Result.success() -> Log.d(TAG, "doWork: task succeeded")
            Result.retry() -> Log.d(TAG, "doWork: task failed")
        }
        return result
    }


}