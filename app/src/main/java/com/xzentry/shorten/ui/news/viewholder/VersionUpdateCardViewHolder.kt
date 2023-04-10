package com.xzentry.shorten.ui.news.viewholder

import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.google.firebase.analytics.FirebaseAnalytics
import com.xzentry.shorten.R
import com.xzentry.shorten.databinding.VersionUpdateCardBinding
import com.xzentry.shorten.ui.news.VerticalPagerAdapter


class VersionUpdateCardViewHolder(
    private val binding: VersionUpdateCardBinding
) : RecyclerView.ViewHolder(binding.root) {

    private lateinit var adView: AdView

    fun updateView(
        position: Int,
        eventTriggerListener: VerticalPagerAdapter.CardEventTriggerListener,
        firebaseAnalytics: FirebaseAnalytics
    ) {

        loadAd()
        binding.checkNew.setOnClickListener {
            eventTriggerListener.onEventTriggered(Event.OnUpdateVersion())
            firebaseEvenLog("whats_new_link", firebaseAnalytics)
        }

        binding.btnUpdate.setOnClickListener {
            eventTriggerListener.onEventTriggered(Event.OnUpdateVersion())
            firebaseEvenLog("new_updates_button", firebaseAnalytics)
        }
    }

    private fun firebaseEvenLog(
        clicked_event: String,
        firebaseAnalytics: FirebaseAnalytics
    ) {
        val bundle = Bundle()
        bundle.putString("update_triggered_by", clicked_event)
        firebaseAnalytics.logEvent("version_update_card", bundle)
    }


    private fun loadAd() {
        adView = AdView(
            binding.root.context,
            binding.root.context.getString(R.string.update_version_card_ad_id) ,
            AdSize.BANNER_HEIGHT_50
        )
        val adContainer = binding?.adView
        adContainer?.addView(adView)
        adView.loadAd()
    }
}
