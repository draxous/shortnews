package com.xzentry.shorten.utils

import android.graphics.Bitmap
import android.view.View
import android.view.View.MeasureSpec
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.xzentry.shorten.R


class ScreenshotUtil private constructor() {

    /**
     * Measures and takes a screenshot of the provided [View].
     *
     * @param view The view of which the screenshot is taken
     * @return A [Bitmap] for the taken screenshot.
     */
    fun takeScreenshotForView(view: View): Bitmap {
        view.measure(
            MeasureSpec.makeMeasureSpec(view.width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(view.height, MeasureSpec.EXACTLY)
        )
        view.layout(
            view.x.toInt() as Int,
            view.y.toInt() as Int,
            view.x.toInt() as Int + view.measuredWidth,
            view.y.toInt() as Int + view.measuredHeight
        )

        view.isDrawingCacheEnabled = true
        view.run { buildDrawingCache(true) }
        val bitmap: Bitmap
        val footerView = view.findViewById<RelativeLayout>(R.id.footer_screen_shot)
        if (footerView != null) {
            showFooterView(view, isShowFooterView = true)
            bitmap = Bitmap.createBitmap(view.drawingCache)
            showFooterView(view, isShowFooterView = false)

        } else {
            bitmap = Bitmap.createBitmap(view.drawingCache)
        }
        view.isDrawingCacheEnabled = false

        return bitmap
    }

    private fun showFooterView(
        view: View,
        isShowFooterView: Boolean
    ) {
        val footerView = view.findViewById<RelativeLayout>(R.id.footer_screen_shot)
        val btnShare = view.findViewById<ImageView>(R.id.btn_share)

        footerView.isInvisible = !isShowFooterView
        btnShare.isVisible = !isShowFooterView
    }

    companion object {
        private var mInstance: ScreenshotUtil? = null

        val instance: ScreenshotUtil?
            get() {
                if (mInstance == null) {
                    synchronized(ScreenshotUtil::class.java) {
                        if (mInstance == null) {
                            mInstance = ScreenshotUtil()
                        }
                    }
                }
                return mInstance
            }
    }
}