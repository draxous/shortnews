package com.xzentry.shorten.ui.search

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.xzentry.shorten.R
import com.xzentry.shorten.data.remote.model.Post
import com.xzentry.shorten.databinding.ItemSearchResultBinding
import com.xzentry.shorten.ui.selectednews.SelectedNewsActivity
import com.xzentry.shorten.utils.FirebaseHelper
import com.xzentry.shorten.utils.toSnImageUrl
import javax.inject.Inject


class SearchResultsAdapter @Inject constructor(
    private val context: Context,
    private val firebaseHelper: FirebaseHelper
) :
    RecyclerView.Adapter<SearchResultsAdapter.SearchResultViewHolder>() {

    private var posts: MutableList<Post?> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchResultViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemBinding = ItemSearchResultBinding.inflate(layoutInflater, parent, false)
        return SearchResultViewHolder(itemBinding)
    }

    fun setItems(posts: List<Post?>) {
        this.posts.clear()
        this.posts.addAll(posts)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    private fun getItem(position: Int): Post? {
        return posts[position]
    }

    override fun onBindViewHolder(holder: SearchResultViewHolder, position: Int) {
        holder.bindTo(getItem(position), position)
    }

    inner class SearchResultViewHolder(private val binding: ItemSearchResultBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindTo(post: Post?, position: Int) {
            post?.let {
                binding.apply {
                    model= post

                    Glide.with(root.context)
                        .load(it.imageUrl.toSnImageUrl(firebaseHelper))
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.ic_news_placeholder)
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.imgNews)

                    binding.container.setOnClickListener {
                        context.startActivity(
                            SelectedNewsActivity.newIntent(
                                context,
                                ArrayList(posts),
                                position,
                                binding.root.context.getString(R.string.search_news)
                            )
                        )
                    }
                }

            }

        }
    }
}
