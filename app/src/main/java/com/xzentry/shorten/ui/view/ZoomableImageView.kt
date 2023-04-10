package com.xzentry.shorten.ui.view

import android.content.Context
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import android.widget.ImageView
import androidx.annotation.AttrRes
import com.github.chrisbanes.photoview.PhotoViewAttacher

class ZoomableImageView(
    context: Context,
    attributeSet: AttributeSet?,
    @AttrRes defaultStyleAttr: Int
) : NetworkImageView(context, attributeSet, defaultStyleAttr) {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    private var attacher: PhotoViewAttacher? = null

    private var onClickListener: OnClickListener? = null

    override fun setScaleType(scaleType: ImageView.ScaleType) {
        attacher?.scaleType = scaleType
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        // setImageBitmap calls through to this method
        attacher?.update()
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        attacher?.update()
    }

    override fun setImageURI(uri: Uri?) {
        super.setImageURI(uri)
        attacher?.update()
    }

    override fun getImageMatrix(): Matrix {
        return attacher?.run {
            update()
            imageMatrix
        } ?: super.getImageMatrix()
    }

    override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
        return super.setFrame(l, t, r, b).also {
            if (it) {
                attacher?.update()
            }
        }
    }

    override fun onImageLoaded(success: Boolean) {
        if (success) {
            super.setScaleType(ImageView.ScaleType.MATRIX)
            attacher = PhotoViewAttacher(this).apply {
                setOnClickListener {
                    onClickListener?.onClick(it)
                }
            }
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        onClickListener = l
    }
}