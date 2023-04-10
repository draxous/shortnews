package com.xzentry.shorten.ui.news.viewholder

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.recyclerview.widget.RecyclerView
import com.xzentry.shorten.databinding.IntroFbCardBinding
import com.xzentry.shorten.ui.news.VerticalPagerAdapter

/**
 * <p>Intent to open the official Facebook app. If the Facebook app is not installed then the
 * default web browser will be used.</p>
 *
 * <p>Example usage:</p>
 *
 * {@code newFacebookIntent(ctx.getPackageManager(), "https://www.facebook.com/JRummyApps");}
 *
 * @param pm
 *     The {@link PackageManager}. You can find this class through {@link
 *     Context#getPackageManager()}.
 * @param url
 *     The full URL to the Facebook page or profile.
 * @return An intent that will open the Facebook page/profile.
 */
class IntroFbCardViewHolder(
    private val binding: IntroFbCardBinding
) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        private const val fbUrl = "https://www.facebook.com/ShortNewslk"
    }

    fun updateView(
        position: Int,
        eventTriggerListener: VerticalPagerAdapter.CardEventTriggerListener
    ) {

        binding.txtSkip.setOnClickListener {
            eventTriggerListener.onEventTriggered(Event.OnCardSkip())
        }

        val context = binding.root.context
        binding.btnFbPage.setOnClickListener {
            var uri = Uri.parse(fbUrl)
            try {
                val applicationInfo =
                    context.packageManager.getApplicationInfo("com.facebook.katana", 0)
                if (applicationInfo.enabled) {
                    // http://stackoverflow.com/a/24547437/1048340
                    uri = Uri.parse("fb://facewebmodal/f?href=$fbUrl")
                }
            } catch (ignored: PackageManager.NameNotFoundException) {
            }
            val intent = Intent(Intent.ACTION_VIEW, uri)
            if (intent.resolveActivity( context.packageManager) != null) {
                context.startActivity(intent)
            }
        }
    }
}
