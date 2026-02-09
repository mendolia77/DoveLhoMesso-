package com.dovelhomesso.app.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageHelper {
    private const val IMAGES_DIR = "item_images"

    /**
     * Copies the image from the given URI/Path to the app's internal storage.
     * Returns the absolute path of the saved file.
     */
    fun copyImageToInternalStorage(context: Context, uriString: String?): String? {
        if (uriString.isNullOrBlank()) return null
        
        try {
            // Check if it's already in our internal storage
            if (uriString.contains(IMAGES_DIR) && File(uriString).exists()) {
                return uriString
            }

            val uri = Uri.parse(uriString)
            val directory = File(context.filesDir, IMAGES_DIR)
            if (!directory.exists()) {
                directory.mkdirs()
            }
            
            val filename = "img_${UUID.randomUUID()}.jpg"
            val destFile = File(directory, filename)
            
            val contentResolver = context.contentResolver
            
            // Handle content:// URIs and file:// URIs/Paths
            val inputStream = if (uriString.startsWith("content://")) {
                contentResolver.openInputStream(uri)
            } else {
                // It's a file path
                val sourceFile = File(uriString)
                if (sourceFile.exists()) {
                    sourceFile.inputStream()
                } else {
                    null
                }
            }
            
            inputStream?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return null // Failed to open input
            
            return destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null // Return null on failure, caller might decide to keep original or fail
        }
    }
}
