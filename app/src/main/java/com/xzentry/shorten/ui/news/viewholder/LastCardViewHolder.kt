package com.xzentry.shorten.ui.news.viewholder

import androidx.recyclerview.widget.RecyclerView
import com.xzentry.shorten.databinding.LastCardBinding
import com.xzentry.shorten.ui.news.VerticalPagerAdapter

class LastCardViewHolder(
    private val binding: LastCardBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun updateView(
        position: Int,
        eventTriggerListener: VerticalPagerAdapter.CardEventTriggerListener
    ) {

        binding.btnBacktoTop.setOnClickListener {
            eventTriggerListener.onEventTriggered(Event.OnBackToTop())
        }
    }
}
