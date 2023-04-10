package com.xzentry.shorten.ui.news.storage

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File

interface StorageHelper {
    fun getStorageFile(id: String): File?
    fun isSaved(id: String): Boolean
    fun delete(id: String): Boolean
    fun saveBitmapToFile(id: String, bitmap: Bitmap?): Boolean
    fun getFileUri(context: Context, file: File): Uri
}