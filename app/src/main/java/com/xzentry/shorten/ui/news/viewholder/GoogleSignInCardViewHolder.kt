package com.xzentry.shorten.ui.news.viewholder

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.recyclerview.widget.RecyclerView
import com.xzentry.shorten.R
import com.xzentry.shorten.databinding.ItemGoogleSigninLayoutBinding
import com.xzentry.shorten.ui.home.HomeActivity
import com.xzentry.shorten.ui.news.VerticalPagerAdapter
import com.xzentry.shorten.utils.FirebaseHelper
import com.xzentry.shorten.utils.network.ConnectivityChecker


internal class GoogleSignInCardViewHolder(
    private val binding: ItemGoogleSigninLayoutBinding,
    private val firebaseHelper: FirebaseHelper,
    private val connectivityChecker: ConnectivityChecker,
    private val verticalPagerAdapter: VerticalPagerAdapter
) : RecyclerView.ViewHolder(binding.root) {

    fun updateView(
        headerTitle: String?,
        newsCardListener: VerticalPagerAdapter.NewsCardListener?,
        position: Int
    ) {
        binding.btnSignIn.setOnClickListener {
            val signInIntent = firebaseHelper.configGoogleSignIn(binding.root.context).signInIntent
            val activity = binding.root.context as HomeActivity
            activity.startActivityForResult(signInIntent, HomeActivity.RC_SIGN_IN)
        }
        binding.headerTitle = headerTitle
        val wordtoSpan: Spannable =
            SpannableString(binding.motto.context.getString(R.string.motto))

        wordtoSpan.setSpan(
            ForegroundColorSpan(binding.motto.context.getColor(R.color.flag_red)),
            8,
            11,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        wordtoSpan.setSpan(
            ForegroundColorSpan(binding.motto.context.getColor(R.color.flag_yellow)),
            12,
            17,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.motto.text = wordtoSpan
    }
}