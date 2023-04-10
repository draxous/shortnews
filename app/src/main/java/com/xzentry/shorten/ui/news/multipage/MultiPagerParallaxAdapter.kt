package com.xzentry.shorten.ui.news.multipage

import android.content.Context
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.analytics.FirebaseAnalytics
import com.xzentry.shorten.R
import com.xzentry.shorten.data.remote.model.AdsModel
import com.xzentry.shorten.ui.news.VerticalPagerAdapter
import com.xzentry.shorten.ui.news.viewholder.Event
import com.xzentry.shorten.utils.CustomGestureListener
import com.xzentry.shorten.utils.FirebaseHelper
import com.xzentry.shorten.utils.toSnImageUrl
import kotlinx.android.synthetic.main.single_multipage_parallax_ad.view.*

class MultiPagerParallaxAdapter(
    private val context: Context,
    private val multiPageAd: AdsModel?,
    private val eventTriggerListener: VerticalPagerAdapter.CardEventTriggerListener,
    private val firebaseAnalytics: FirebaseAnalytics,
    private val firebaseHelper: FirebaseHelper
) : RecyclerView.Adapter<MultiPagerParallaxAdapter.PagerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder =
        PagerViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.single_multipage_parallax_ad, parent, false)
        )

    override fun getItemCount() = multiPageAd?.images?.size ?: 0


    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        Glide.with(context)
            .load( multiPageAd?.images?.get(position)?.imageUrl.toSnImageUrl(firebaseHelper))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.itemView.ad_image_px)

        holder.itemView.ad_image_px.setOnTouchListener { _, _ -> false }
        cardGestureDetection(context, holder.itemView, eventTriggerListener)
    }

    private fun cardGestureDetection(
        context: Context,
        itemView: View,
        eventTriggerListener: VerticalPagerAdapter.CardEventTriggerListener
    ) {
        val mGestureDetector = GestureDetector(
            context,
            object : CustomGestureListener(itemView) {
                override fun onAngledSwipeUp(): Boolean {
                    return false
                }

                override fun onAngledSwipeDown(): Boolean {
                    return false
                }

                override fun onTouch(): Boolean {
                    firebaseEvenLog(multiPageAd)
                    eventTriggerListener.onEventTriggered(Event.OnShareCard(itemView.ad_image_px, null))
                    return false
                }
            })

        itemView.findViewById<View>(R.id.clickableArea).setOnTouchListener { _, event ->
            !mGestureDetector.onTouchEvent(event)
        }

    }

    private fun firebaseEvenLog(adsModel: AdsModel?) {
        val bundle = Bundle()
        bundle.putString("share", adsModel?.id?.toString())
        firebaseAnalytics.logEvent("multipage_parralax_share", bundle)
    }

    class PagerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}