package com.dovelhomesso.app.util

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import kotlinx.coroutines.tasks.await
import java.io.IOException

object ImageAnalyzer {
    
    suspend fun analyzeImage(context: Context, imageUri: Uri): List<String> {
        return try {
            val image = InputImage.fromFilePath(context, imageUri)
            val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
            
            // Process image
            val labels = labeler.process(image).await()
            
            // Filter and map
            labels.filter { it.confidence > 0.6f } // Slightly lower confidence to get more results
                  .map { it.text }
                  .take(7) // Top 7 labels
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
