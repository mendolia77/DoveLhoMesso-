package com.dovelhomesso.app.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object FileUtils {

    /**
     * Copies a file from the given URI to the app's internal storage.
     * Returns the absolute path of the copied file.
     */
    fun copyUriToInternalStorage(context: Context, uri: Uri, subDir: String): String? {
        val returnCursor = context.contentResolver.query(uri, null, null, null, null)
        val nameIndex = returnCursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        returnCursor?.moveToFirst()
        val originalName = if (nameIndex != null && nameIndex >= 0) {
            returnCursor.getString(nameIndex)
        } else {
            "file_${System.currentTimeMillis()}"
        }
        returnCursor?.close()

        // Generate a unique filename to avoid collisions
        val extension = originalName.substringAfterLast('.', "")
        val fileName = if (extension.isNotEmpty()) {
            "${UUID.randomUUID()}.$extension"
        } else {
            "${UUID.randomUUID()}"
        }

        val directory = File(context.filesDir, subDir)
        if (!directory.exists()) {
            directory.mkdirs()
        }

        val destinationFile = File(directory, fileName)

        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(destinationFile)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            return destinationFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Deletes a file from internal storage.
     */
    fun deleteFile(path: String) {
        try {
            val file = File(path)
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
