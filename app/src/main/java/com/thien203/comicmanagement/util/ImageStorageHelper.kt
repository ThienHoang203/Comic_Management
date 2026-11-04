package com.thien203.comicmanagement.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImageStorageHelper {

    fun saveCoverImageToInternalStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val coversDir = File(context.filesDir, "covers")
            if (!coversDir.exists()) {
                coversDir.mkdirs()
            }

            val fileName = "cover_${System.currentTimeMillis()}.jpg"
            val destFile = File(coversDir, fileName)

            val inputStream: InputStream? = context.contentResolver.openInputStream(sourceUri)
            val outputStream = FileOutputStream(destFile)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }

            Uri.fromFile(destFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun deleteCoverImageFile(uriString: String?) {
        if (uriString.isNullOrBlank()) return
        try {
            val uri = Uri.parse(uriString)
            if (uri.scheme == "file" && uri.path != null) {
                val file = File(uri.path!!)
                if (file.exists()) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
