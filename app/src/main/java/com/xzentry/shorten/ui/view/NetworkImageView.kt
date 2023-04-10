package com.xzentry.shorten.ui.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.xzentry.shorten.R
import java.io.File

open class NetworkImageView(
    context: Context,
    attributeSet: AttributeSet?,
    @AttrRes defaultStyleAttr: Int
) : AppCompatImageView(context, attributeSet, defaultStyleAttr) {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : this(context, attributeSet, 0)

    private var imageLoadListener: ((Boolean) -> Unit)? = null

    private var networkLoadListener: ((String) -> Unit)? = null

    private val headers = mutableMapOf<String, String>()

    private val requestManager
        get() = Glide.with(this)

    @DrawableRes
    internal val fallbackImageResId: Int

    @DrawableRes
    internal val errorImageResId: Int

    var placeholder: Drawable? = null

    var thumbnail: String? = null

    private val animate: Boolean

    private var cornerRadiusTopLeft: Float
    private var cornerRadiusTopRight: Float
    private var cornerRadiusBottomLeft: Float
    private var cornerRadiusBottomRight: Float

    var isSquare: Boolean

    init {
        val a = context.obtainStyledAttributes(attributeSet, R.styleable.NetworkImageView)
        fallbackImageResId = a.getResourceId(R.styleable.NetworkImageView_fallbackImage, R.drawable.image_unavailable)
        errorImageResId = a.getResourceId(R.styleable.NetworkImageView_errorImage, R.drawable.image_error)
        animate = a.getBoolean(R.styleable.NetworkImageView_animate, false)

        val cornerRadius = a.getDimension(R.styleable.NetworkImageView_cornerRadius, 0F)
        cornerRadiusTopLeft = a.getDimension(R.styleable.NetworkImageView_cornerRadiusTopLeft, cornerRadius)
        cornerRadiusTopRight = a.getDimension(R.styleable.NetworkImageView_cornerRadiusTopRight, cornerRadius)
        cornerRadiusBottomRight = a.getDimension(R.styleable.NetworkImageView_cornerRadiusBottomLeft, cornerRadius)
        cornerRadiusBottomLeft = a.getDimension(R.styleable.NetworkImageView_cornerRadiusBottomRight, cornerRadius)
        isSquare = a.getBoolean(R.styleable.NetworkImageView_square, false)

        val url = a.getString(R.styleable.NetworkImageView_url)
        a.recycle()
        setUrl(url)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, if (isSquare) widthMeasureSpec else heightMeasureSpec)
    }

    fun setHeaders(headers: Map<String, String>) {
        this.headers.clear()
        addHeaders(headers)
    }

    fun addHeaders(headers: Map<String, String>) {
        this.headers.putAll(headers)
    }

    fun setHeader(key: String, value: String) {
        this.headers[key] = value
    }

    fun clearHeader(key: String) {
        this.headers.remove(key)
    }

    fun setUrl(url: String?) {
        if (!url.isNullOrBlank()) {
            setImage(url)
        } else {
            setImage(fallbackImageResId)
        }
    }

    fun setCornerRadiusTopLeft(cornerRadius: Float) {
        cornerRadiusTopLeft = cornerRadius
    }

    fun setCornerRadiusTopRight(cornerRadius: Float) {
        cornerRadiusTopRight = cornerRadius
    }

    fun setCornerRadiusBottomLeft(cornerRadius: Float) {
        cornerRadiusBottomLeft = cornerRadius
    }

    fun setCornerRadiusBottomRight(cornerRadius: Float) {
        cornerRadiusBottomRight = cornerRadius
    }

    fun setNetworkLoadListener(listener: (String) -> Unit) {
        networkLoadListener = listener
    }

    fun setImageLoadListener(listener: (Boolean) -> Unit) {
        imageLoadListener = listener
    }

    fun setImage(url: String) {
        networkLoadListener?.invoke(url)
        requestManager.load(GlideUrl(url) { headers }).withDefaults()
    }

    fun setImage(uri: Uri) {
        networkLoadListener?.invoke(uri.toString())
        requestManager.load(GlideUrl(uri.toString()) { headers }).withDefaults()
    }

    fun setImage(file: File) {
        requestManager.load(file).withDefaults()
    }

    fun setImage(@DrawableRes resId: Int) {
        requestManager.load(resId).withDefaults()
    }

    internal fun <T> setImage(type: T) {
        requestManager.load(type).withDefaults()
    }

    fun getImageFile(fileKey: Any, width: Int, height: Int, onFileReady: (File) -> Unit) {
        requestManager.downloadOnly()
            .load(fileKey)
            .let { if (width > 0 || height > 0) it.override(width, height) else it}
            .into(object: CustomTarget<File>() {
                override fun onLoadCleared(placeholder: Drawable?) {
                    // No-op
                }

                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                    onFileReady(resource)
                }
            })
    }

    protected open fun onImageLoaded(success: Boolean) {

    }

    private fun RequestBuilder<Drawable>.withDefaults() {
        var baseRequestOptions = when (scaleType) {
            ScaleType.FIT_CENTER -> fitCenter()
            ScaleType.CENTER_CROP -> centerCrop()
            ScaleType.CENTER_INSIDE -> centerInside()
            // No need to take action for other scale types since
            // Glide's default DownsampleStrategy CENTER_OUTSIDE works for them
            else -> this
        }

        if (placeholder != null) {
            baseRequestOptions = baseRequestOptions.placeholder(placeholder)
        }

        if (!thumbnail.isNullOrBlank()) {
            baseRequestOptions = baseRequestOptions.thumbnail(requestManager.load(thumbnail))
        }

        val transformations = createTransformations()
        if (transformations.isNotEmpty()) {
            baseRequestOptions = baseRequestOptions.transform(*transformations)
        }

        if (!animate) {
            baseRequestOptions = baseRequestOptions.dontAnimate()
        }

        baseRequestOptions.listener(object: RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>?,
                isFirstResource: Boolean
            ): Boolean {
                onImageLoaded(false)
                imageLoadListener?.invoke(false)
                return false
            }

            override fun onResourceReady(
                resource: Drawable?,
                model: Any?,
                target: Target<Drawable>?,
                dataSource: DataSource?,
                isFirstResource: Boolean
            ): Boolean {
                onImageLoaded(true)
                imageLoadListener?.invoke(true)
                return false
            }
        })
        .error(errorImageResId)
        .into(this@NetworkImageView)
    }

    private fun createTransformations(): Array<Transformation<Bitmap>> {
        val transformations = mutableListOf<Transformation<Bitmap>>()

        if (scaleType == ScaleType.CENTER_CROP && isSquare) {
            transformations.add(CropSquareTransformation())
        }

        if (cornerRadiusTopLeft > 0 || cornerRadiusTopRight > 0 || cornerRadiusBottomLeft > 0 || cornerRadiusBottomRight > 0) {
            transformations.add(
                GranularRoundedCorners(
                    cornerRadiusTopLeft,
                    cornerRadiusTopRight,
                    cornerRadiusBottomRight,
                    cornerRadiusBottomLeft
                )
            )
        }

        return transformations.toTypedArray()
    }
}