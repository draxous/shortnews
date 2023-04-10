package com.xzentry.shorten.ui.view

import android.graphics.Bitmap
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import com.bumptech.glide.load.resource.bitmap.TransformationUtils
import java.security.MessageDigest

class CropSquareTransformation : BitmapTransformation() {
    private var size = 0

    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        size = outWidth.coerceAtLeast(outHeight)
        return TransformationUtils.centerCrop(pool, toTransform, size, size)
    }

    override fun toString(): String {
        return "CropSquareTransformation(size=$size)"
    }

    override fun equals(other: Any?): Boolean {
        return other is CropSquareTransformation && other.size == size
    }

    override fun hashCode(): Int {
        return ID.hashCode() + size * 10
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update((ID + size).toByteArray(Key.CHARSET))
    }

    companion object {
        private const val VERSION = 1
        private const val ID = "jp.co.rakuten.rms.android.glide.transformations.CropSquareTransformation.$VERSION"
    }
}