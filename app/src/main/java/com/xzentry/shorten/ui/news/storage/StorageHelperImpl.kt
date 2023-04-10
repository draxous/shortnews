package com.xzentry.shorten.ui.news.storage

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject

class StorageHelperImpl @Inject constructor(private val context: Context) :
    StorageHelper {

    private val screenShotDir
        get() = context.externalCacheDir?.let { rootDir ->
            File(rootDir, "news-screen-shot")
        }

    override fun getStorageFile(id: String): File? {
        return screenShotDir?.let { dir ->
            val isFileValid = if (dir.exists()) {
                !dir.isFile || (dir.delete() && dir.mkdir())
            } else {
                dir.mkdir()
            }

            if (isFileValid) {
                File(dir, "${id}.png")
            } else {
                null
            }
        }
    }

    override fun isSaved(id: String): Boolean {
        return screenShotDir?.let { dir ->
            val file = File(dir, "${id}.png")
            file.exists() && file.length() > 0
        } ?: false
    }

    override fun delete(id: String): Boolean {
        return screenShotDir?.let { dir ->
            val file = File(dir, "${id}.png")
            file.delete()
        } ?: false
    }

    override fun getFileUri(context: Context, file: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } else {
            Uri.fromFile(file)
        }
    }

    override fun saveBitmapToFile(id: String, bitmap: Bitmap?): Boolean {
        return bitmap?.let { bmp ->
            getStorageFile(id)
                ?.let { file ->
                    try {
                        FileOutputStream(file.path).use { out ->
                            bmp.compress(
                                Bitmap.CompressFormat.PNG,
                                100,
                                out
                            ) // bmp is your Bitmap instance
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        false
                    }
                    true
                }
        } ?: false
    }
}


