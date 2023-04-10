package com.xzentry.shorten.ui.topics

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.analytics.FirebaseAnalytics
import com.xzentry.shorten.R
import com.xzentry.shorten.data.remote.model.Topic
import com.xzentry.shorten.databinding.ItemTopicsLayoutBinding
import com.xzentry.shorten.ui.selectednews.SelectedNewsActivity
import com.xzentry.shorten.utils.FirebaseHelper
import com.xzentry.shorten.utils.toSnImageUrl
import javax.inject.Inject


class TopicsListAdapter @Inject constructor(
    private val context: Context,
    private val firebaseAnalytics: FirebaseAnalytics,
    private val firebaseHelper: FirebaseHelper
) :
    RecyclerView.Adapter<TopicsListAdapter.TopicsViewHolder>() {
    private var topics: MutableList<Topic> = mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TopicsListAdapter.TopicsViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemBinding = ItemTopicsLayoutBinding.inflate(layoutInflater, parent, false)
        return TopicsViewHolder(itemBinding)
    }

    fun setItems(categories: List<Topic>) {
        val startPosition = this.topics.size
        this.topics.clear()
        this.topics.addAll(categories)
        if (startPosition > categories.size) {
            notifyItemRemoved(this.topics.size)  //item removed from the categories list
        } else {
            notifyItemRangeChanged(startPosition, categories.size) //item added to categories list
        }
    }

    override fun getItemCount(): Int {
        return topics.size
    }

    fun getItem(position: Int): Topic {
        return topics[position]
    }

    override fun onBindViewHolder(holder: TopicsListAdapter.TopicsViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    inner class TopicsViewHolder(private val binding: ItemTopicsLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindTo(topic: Topic) {

            binding.model = topic

            Glide.with(binding.root.context)
                .load(topic.catImageUrl.toSnImageUrl(firebaseHelper))
                .placeholder(R.drawable.ic_placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.image)

            binding.cardCategory.setOnClickListener {
                context.startActivity(
                    SelectedNewsActivity.newIntent(
                        context,
                        topic.catId,
                        topic.category
                    )
                )
                firebaseEvenLog(topic)

            }
        }

        private fun firebaseEvenLog(topic: Topic) {
            val params = Bundle()
            params.putString("topicId", topic.catId.toString())
            params.putString("topic", topic.category)
            firebaseAnalytics.logEvent("selected_topic_page", params)
        }
    }
}
