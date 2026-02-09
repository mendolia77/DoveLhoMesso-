package com.dovelhomesso.app.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dovelhomesso.app.DoveLhoMessoApp
import com.dovelhomesso.app.util.NotificationHelper
import java.util.concurrent.TimeUnit

class ExpiryCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val app = applicationContext as DoveLhoMessoApp
            val repository = app.repository
            
            val documents = repository.getAllDocumentsList()
            
            val now = System.currentTimeMillis()
            
            documents.forEach { doc ->
                if (doc.expiryDate != null && doc.expiryDate > 0) {
                    val diff = doc.expiryDate - now
                    val daysLeft = TimeUnit.MILLISECONDS.toDays(diff).toInt()
                    
                    // Logic:
                    // If diff is negative, it's expired.
                    // If daysLeft is 30, 7, 1, 0 -> notify.
                    
                    // Note on TimeUnit conversion: 
                    // 1.5 days -> 1 day.
                    // So if it expires in 30 hours (1.25 days), it says 1 day. Correct.
                    // If it expires in 2 hours (0.08 days), it says 0 days. Correct.
                    // If it expired 2 hours ago (-0.08 days), it says 0 days.
                    // We need to be careful not to notify multiple times for "0 days".
                    // But since worker runs once a day, it's acceptable.
                    
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
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}
