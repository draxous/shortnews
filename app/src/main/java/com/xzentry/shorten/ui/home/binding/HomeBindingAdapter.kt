package com.xzentry.shorten.ui.home.binding

import android.view.View
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter

@BindingAdapter("visibility")
internal fun setVisibility(view: View, isVisible: Boolean) {
    view.isVisible = isVisible
}
