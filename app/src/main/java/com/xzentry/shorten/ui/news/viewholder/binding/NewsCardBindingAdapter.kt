package com.xzentry.shorten.ui.news.viewholder.binding

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.xzentry.shorten.R
import com.xzentry.shorten.data.remote.model.Topic
import com.xzentry.shorten.utils.toDateyyyyMMddhhmmss
import com.xzentry.shorten.utils.toStringyyyyMMddhhmm


@BindingAdapter("dateTimeText")
internal fun setDateTimeText(view: TextView, time: String?) {
    view.text = time?.toDateyyyyMMddhhmmss().toStringyyyyMMddhhmm()
}

@BindingAdapter("sourcePrefixLabel")
internal fun setSourcePrefixLabel(view: TextView, category: Topic?) {
    view.text = if (category?.catId ?: -1 == 3) {
        "Gossip by "
    } else {
        view.context.resources.getString(R.string.moreAt)
    }
}