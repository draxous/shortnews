package com.xzentry.shorten.ui.news.viewholder

import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.analytics.FirebaseAnalytics
import com.xzentry.shorten.R
import com.xzentry.shorten.databinding.MultipageParralaxAdCardBinding
import com.xzentry.shorten.model.NewsData
import com.xzentry.shorten.ui.news.VerticalPagerAdapter
import com.xzentry.shorten.ui.news.multipage.MultiPagerParallaxAdapter
import com.xzentry.shorten.utils.FirebaseHelper
import com.xzentry.shorten.utils.pagerTransformer.ZoomInFadeTransformer
import com.xzentry.shorten.utils.toSnImageUrl

/**
 * <p>Intent to open the official Facebook app. If the Facebook app is not installed then the
 * default web browser will be used.</p>
 *
 * <p>Example usage:</p>
 *
 * {@code newFacebookIntent(ctx.getPackageManager(), "https://www.facebook.com/JRummyApps");}
 *
 * @param pm
 *     The {@link PackageManager}. You can find this class through {@link
 *     Context#getPackageManager()}.
 * @param url
 *     The full URL to the Facebook page or profile.
 * @return An intent that will open the Facebook page/profile.
 */
class MultiPagedParallaxAdCardViewHolder(
    private val binding: MultipageParralaxAdCardBinding,
    private val firebaseHelper: FirebaseHelper,
    private val firebaseAnalytics: FirebaseAnalytics
) : RecyclerView.ViewHolder(binding.root) {

    fun updateView(
        newsList: List<NewsData>,
        position: Int,
        eventTriggerListener: VerticalPagerAdapter.CardEventTriggerListener,
        childFragmentManager: FragmentManager
    ) {

        //inject ads/fact/info images fragment
        binding.apply {
            viewpager.adapter = MultiPagerParallaxAdapter(
                binding.root.context,
                newsList[position].getAd(),
                eventTriggerListener,
                firebaseAnalytics,
                firebaseHelper
            )

            newsList[position].getAd()?.backgroundImage?.let {imageUrl->
                Glide.with(root.context)
                    .load(imageUrl.toSnImageUrl(firebaseHelper))
                    .placeholder(R.drawable.ic_cloud_loading_dark)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(parallaxAdBackground)
            }

            indicator.setViewPager(binding.viewpager)
            with(viewpager) {
                clipToPadding = false
                clipChildren = false
                offscreenPageLimit = 3
            }

            viewpager.setPageTransformer(ZoomInFadeTransformer())
        }

    }
}


