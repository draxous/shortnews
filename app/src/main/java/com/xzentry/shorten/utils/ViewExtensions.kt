package com.xzentry.shorten.utils

import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.core.content.getSystemService

/*// Snackbar
fun Snackbar.default(): Snackbar {
    view.apply {
        background = context.getDrawable(R.drawable.snackbar_background)
        ViewCompat.setElevation(this, 0f)
        textColor(ContextCompat.getColor(context, R.color.deep_black))
        textSize(resources.getDimension(R.dimen.text_size_14))
        updateLayoutParams<ViewGroup.MarginLayoutParams> {
            val margins = 16.toPx(resources).toInt()
            setMargins(margins, margins, margins, margins)
        }
    }

    return this
}

fun Snackbar.textColor(@ColorInt color: Int): Snackbar {
    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextColor(color)
    return this
}

fun Snackbar.textSize(@Dimension size: Float): Snackbar {
    view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text).setTextSize(
        TypedValue.COMPLEX_UNIT_PX, size)
    return this
}*/

fun View.closeKeyboard() {
    context.getSystemService<InputMethodManager>()?.hideSoftInputFromWindow(windowToken, 0)
}
