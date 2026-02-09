package com.dovelhomesso.app.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dovelhomesso.app.DoveLhoMessoApp
import com.dovelhomesso.app.util.NotificationHelper
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

class ExpiryCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repository = (applicationContext as DoveLhoMessoApp).repository
        
        try {
            val documents = repository.getAllDocuments().first()
            val now = System.currentTimeMillis()
            
            documents.forEach { doc ->
                if (doc.expiryDate != null) {
                    val diff = doc.expiryDate - now
                    val daysLeft = TimeUnit.MILLISECONDS.toDays(diff).toInt()
                    
                    // Notify at 30, 7, 1, 0 days
                    // Note: This logic assumes the worker runs daily. 
                    // If it skips a day, we might miss the notification.
                    // Ideally we should store "last notification date" in DB, but this is a good start.
                    
                    if (daysLeft == 30 || daysLeft == 7 || daysLeft == 1 || daysLeft == 0) {
                        NotificationHelper.showExpiryNotification(
                            applicationContext, 
                            doc.id, 
                            doc.title, 
                            daysLeft
                        )
                    }
                }
            }
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }
}
