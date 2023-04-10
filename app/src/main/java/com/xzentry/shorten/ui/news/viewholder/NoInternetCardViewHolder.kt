package com.xzentry.shorten.ui.news.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.xzentry.shorten.databinding.NoInternetCardBinding
import com.xzentry.shorten.ui.news.VerticalPagerAdapter

class NoInternetCardViewHolder(
    private val binding: NoInternetCardBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun updateView(
        eventTriggerListener: VerticalPagerAdapter.CardEventTriggerListener
    ) {
        binding.btnRetry.setOnClickListener {
            eventTriggerListener.onEventTriggered(Event.OnLoadMoreItems())
        }
        binding.btnSettings.setOnClickListener {
            eventTriggerListener.onEventTriggered(Event.OnOpenNetworkSettingsScreen())
        }
    }
}
