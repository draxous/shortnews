package com.xzentry.shorten.ui.news.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.xzentry.shorten.R
import com.xzentry.shorten.databinding.PinnedCardBinding
import com.xzentry.shorten.model.NewsData
import com.xzentry.shorten.ui.news.VerticalPagerAdapter
import com.xzentry.shorten.utils.FirebaseHelper
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
class PinnedCardViewHolder(
    private val binding: PinnedCardBinding,
    private val firebaseHelper: FirebaseHelper
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private const val fbUrl = "https://hpb.health.gov.lk/"
    }

    fun updateView(
        newsList: List<NewsData>,
        position: Int,
        eventTriggerListener: VerticalPagerAdapter.CardEventTriggerListener
    ) {
        if (firebaseHelper.pinnedImageUrlRemote?.isNotEmpty() == true) {

            Glide.with(binding.root.context)
                .load(firebaseHelper.pinnedImageUrlRemote.toSnImageUrl(firebaseHelper))
                .placeholder(R.drawable.ic_cloud_loading_dark)
                .into(binding.imgPinned)
        }
    }
}
