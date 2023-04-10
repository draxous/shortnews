package com.xzentry.shorten.utils.pagerTransformer

import android.view.View
import androidx.viewpager2.widget.ViewPager2
import kotlinx.android.synthetic.main.single_multipage_ad.view.*

//https://dev.to/osman_elaldi/viewpager-with-parallax-effect-2hag
class ParallaxTransformer : ViewPager2.PageTransformer {
    private lateinit var planet: View
    private lateinit var name: View
    override fun transformPage(page: View, position: Float) {
        planet = page.ad_image
        page.apply {

            if (position <= 1 && position >= -1) {
                planet.translationX = position * (width / 2f)
                //name.translationX = - position * (width / 4f)
                /* If user drags the page right to left :
                   Planet : 0.5 of normal speed
                   Name : 1.25 of normal speed

                   If the user drags the page left to right :
                   Planet: 1.5 of normal speed
                   Name: 0.75 of normal speed
                 */
            }
        }
    }

}