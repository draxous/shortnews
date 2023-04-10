package com.xzentry.shorten.utils

import android.graphics.Bitmap
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class FileUtil private constructor() {

    /**
     * Stores the given [Bitmap] to a path on the device.
     *
     * @param bitmap   The [Bitmap] that needs to be stored
     * @param filePath The path in which the bitmap is going to be stored.
     */
    fun storeBitmap(bitmap: Bitmap, filePath: String): File? {
        val imageFile = File(filePath)
        imageFile.parentFile.mkdirs()
        return try {
            val fout = FileOutputStream(imageFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout)
            fout.flush()
            fout.close()
            imageFile
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

    }

    companion object {

        private var mInstance: FileUtil? = null

        val instance: FileUtil?
            get() {
                if (mInstance == null) {
                    synchronized(FileUtil::class.java) {
                        if (mInstance == null) {
                            mInstance = FileUtil()
                        }
                    }
                }
                return mInstance
            }
    }
}