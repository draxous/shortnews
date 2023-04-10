package com.xzentry.shorten.ui.news.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.xzentry.shorten.databinding.IntroCardBinding
import com.xzentry.shorten.ui.news.VerticalPagerAdapter

class IntroCardViewHolder(
    private val binding: IntroCardBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun updateView(
        position: Int,
        eventTriggerListener: VerticalPagerAdapter.CardEventTriggerListener
    ) {

        binding.txtSkip.setOnClickListener {
            eventTriggerListener.onEventTriggered(Event.OnCardSkip())
        }
    }
}
