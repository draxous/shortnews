package com.xzentry.shorten.ui.search.binding

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.xzentry.shorten.data.remote.model.Post


@BindingAdapter("searchPostTitle")
internal fun setSearchPostTitle(view: TextView, post: Post?) {
    view.text = post?.source?.source?.let {
        it
    } ?: ""
}