package com.codegarden.app.data.local

import android.content.Context
import android.net.Uri
import java.io.File

/** 植物照片存档：复制到应用内部存储，返回绝对路径；不依赖任何网络 */
object PhotoStore {

    fun savePhoto(context: Context, uri: Uri): String? = try {
        val dir = File(context.filesDir, "plant_photos").apply { mkdirs() }
        val file = File(dir, "plant_${System.currentTimeMillis()}.jpg")
        val copied = context.contentResolver.openInputStream(uri)?.use { input ->
            file.outputStream().use { input.copyTo(it) }
        }
        if (copied != null && file.length() > 0) file.absolutePath else {
            file.delete()
            null
        }
    } catch (e: Exception) {
        null
    }

    fun deletePhoto(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).delete() }
    }

    fun deleteAll(context: Context) {
        runCatching { File(context.filesDir, "plant_photos").deleteRecursively() }
    }
}