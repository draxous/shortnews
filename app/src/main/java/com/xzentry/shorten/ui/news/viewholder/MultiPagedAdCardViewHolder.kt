package com.xzentry.shorten.ui.news.viewholder

import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.xzentry.shorten.databinding.MultipageAdCardBinding
import com.xzentry.shorten.model.NewsData
import com.xzentry.shorten.ui.news.VerticalPagerAdapter
import com.xzentry.shorten.ui.news.multipage.MultiPagerAdapter
import com.xzentry.shorten.utils.FirebaseHelper
import com.xzentry.shorten.utils.pagerTransformer.ParallaxTransformer

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
class MultiPagedAdCardViewHolder(
    private val binding: MultipageAdCardBinding,
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
            viewpager.adapter =
                MultiPagerAdapter(
                    binding.root.context,
                    newsList[position].getAd(),
                    eventTriggerListener,
                    firebaseAnalytics,
                    firebaseHelper
                )
            indicator.setViewPager(binding.viewpager)
            viewpager.setPageTransformer(ParallaxTransformer())
            binding.indicator.isVisible = newsList[position].getAd()?.images?.size?:0 > 1
        }
    }
}
