package com.dovelhomesso.app.util

import android.content.Context
import android.net.Uri
// import com.google.mlkit.vision.common.InputImage
// import com.google.mlkit.vision.label.ImageLabeling
// import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
// import kotlinx.coroutines.tasks.await
import java.io.IOException

object ImageAnalyzer {
    
    suspend fun analyzeImage(context: Context, imageUri: Uri): List<String> {
        return emptyList()
        /* Feature disabled
        return try {
            // Check if file exists if it's a file URI
            if (imageUri.scheme == "file" && imageUri.path != null) {
                val file = java.io.File(imageUri.path!!)
                if (!file.exists()) return emptyList()
            }
            
            val image = InputImage.fromFilePath(context, imageUri)
            val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
            
            // Process image
            val labels = labeler.process(image).await()
            
            // Filter and map
            labels.filter { it.confidence > 0.5f } 
                  .map { it.text }
                  .take(7) 
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        } catch (e: Error) {
            // Catch LinkageError, NoClassDefFoundError, etc.
            e.printStackTrace()
            emptyList()
        }
        */
    }
}
