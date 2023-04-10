package com.xzentry.shorten.utils.pagerTransformer

import android.view.View
import androidx.core.view.ViewCompat
import androidx.viewpager2.widget.ViewPager2
import com.xzentry.shorten.R
import com.xzentry.shorten.ui.news.viewholder.MultiPagedParallaxAdCardViewHolder
import kotlinx.android.synthetic.main.single_multipage_ad.view.*

class ZoomInFadeTransformer : ViewPager2.PageTransformer {
    private lateinit var planet: View
    private lateinit var name: View

    companion object {
        private const val MIN_SCALE = 0.85f
        private const val MIN_ALPHA = 0.5f
    }

    override fun transformPage(page: View, position: Float) {
        val viewPager = page.parent.parent as ViewPager2
        val offset =
            position * -(2 * page.context.resources.getDimension(R.dimen.offset) + page.context.resources.getDimension(
                R.dimen.pageMargin
            ))


        if (position >= -1 || position <= 1) {
            //shows two edges of next cards from sides
            if (viewPager.orientation == ViewPager2.ORIENTATION_HORIZONTAL) {
                if (ViewCompat.getLayoutDirection(viewPager) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                    page.translationX = -offset
                } else {
                    page.translationX = offset
                }
            } else {
                page.translationY = offset
            }

            // Modify the default slide transition to shrink the page as well
            val scaleFactor = MIN_SCALE.coerceAtLeast(1 - kotlin.math.abs(position))
            // Scale the page down (between MIN_SCALE and 1)
            page.scaleX = scaleFactor
            page.scaleY = scaleFactor

            // Fade the page relative to its size.
            page.alpha =
                MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA)
        }
    }

}