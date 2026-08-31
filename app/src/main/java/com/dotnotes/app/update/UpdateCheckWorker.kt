package com.dotnotes.app.update

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dotnotes.app.BuildConfig

class UpdateCheckWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val updateManager = UpdateManager()
            val release = updateManager.checkForUpdate(BuildConfig.VERSION_NAME)
            if (release != null) {
                updateManager.showUpdateNotification(context, release)
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "periodic_update_check_work"
    }
}
