package com.xzentry.shorten.ui.news.viewholder

import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.GestureDetector
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.util.Consumer
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.integration.webp.decoder.WebpDrawable
import com.bumptech.glide.integration.webp.decoder.WebpDrawableTransformation
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Transformation
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.firebase.analytics.FirebaseAnalytics
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.xzentry.shorten.BR
import com.xzentry.shorten.BuildConfig
import com.xzentry.shorten.R
import com.xzentry.shorten.databinding.ItemCartoonLayoutBinding
import com.xzentry.shorten.di.module.GlideApp
import com.xzentry.shorten.model.NewsData
import com.xzentry.shorten.ui.news.VerticalPagerAdapter
import com.xzentry.shorten.utils.*
import com.xzentry.shorten.utils.network.ConnectivityChecker
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import java.text.SimpleDateFormat

internal class CartoonCardViewHolder(
    private val binding: ItemCartoonLayoutBinding,
    private val firebaseHelper: FirebaseHelper,
    private val connectivityChecker: ConnectivityChecker

) : RecyclerView.ViewHolder(binding.root) {

    private var youTubePlayer: YouTubePlayer? = null
    private var currentVideoId: String? = null

    fun updateView(
        newsList: List<NewsData>,
        position: Int,
        newsCardListener: VerticalPagerAdapter.NewsCardListener?,
        eventTriggerListener: VerticalPagerAdapter.CardEventTriggerListener,
        firebaseAnalytics: FirebaseAnalytics,
        videoId: String?,
        headerTitle: String?
    ) {
        binding.setVariable(BR.model, newsList[position].getPostItem())
        binding.setVariable(BR.position, position)
        binding.setVariable(BR.headerTitle, headerTitle ?: "")

        videoId?.let { cueVideo(videoId) } ?: youTubePlayer?.pause()

        val footerImage = binding.readMoreImage
        val headerBackgroundImage = binding.imgBackground
        val imgNews = binding.imgNews
        val refreshFeed = binding.root.findViewById<ImageView>(R.id.img_refresh_feed)
        val refreshProgress = binding.root.findViewById<MaterialProgressBar>(R.id.feed_progress)
        val screenshotView = binding.card

        //video or not anyway set the image
        setNewsImage(newsList, position, footerImage, headerBackgroundImage, imgNews)

        //show title header
        binding.headerLayout.header.isGone =
            binding.headerTitle?.trim() == binding.root.context.getString(R.string.my_feed)

        //set image or video
        if (!newsList[position].getPostItem()?.videoUrl.isNullOrEmpty()) {
            toggleBetweenImageAndVideo(showImage = false)
            //reset full screens back to normal size when load again after scrolling between cards
        } else {
            toggleBetweenImageAndVideo(showImage = true)
        }

        newsCardListener?.let { listener ->
            refreshFeed.setOnClickListener {
                if (NetworkUtils.isNetworkConnected(binding.root.context)) {
                    refreshProgress.isVisible = true
                    refreshFeed.isVisible = false
                    val refreshComplete: Consumer<Boolean> = Consumer {
                        refreshProgress.isVisible = false
                        refreshFeed.isVisible = true
                    }
                    listener.onClickRefreshFeed(refreshComplete)
                } else {
                    Toast.makeText(
                        binding.root.context,
                        R.string.unable_to_connect,
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
        }

        cardGestureDetection(newsList, position)

        binding.txtSource.setOnClickListener {

            eventTriggerListener.onEventTriggered(Event.OnOpenSourcePage(newsList[position].getPostItem()?.source))
        }

        binding.headerLayout.imgBack.setOnClickListener {
            newsCardListener?.onBackPress()
        }

        binding.footerLayout.like.setOnClickListener {
            newsCardListener?.onClickGotoTopFeed()
        }

        binding.btnShare.setOnClickListener {
            shareCard(newsCardListener, screenshotView, newsList, position, firebaseAnalytics)
        }

        binding.footerLayout.share.setOnClickListener {
            shareCard(newsCardListener, screenshotView, newsList, position, firebaseAnalytics)
        }

        binding.footerLayout.bookmark.setOnClickListener {
            if (BuildConfig.DEBUG) {
                newsCardListener?.onBookmarkClickListener(screenshotView)
            }
        }
    }

    private fun cueVideo(videoId: String?) {
        currentVideoId = videoId
        videoId?.let {
            youTubePlayer?.cueVideo(it, 0f)
        }
    }

    private fun toggleBetweenImageAndVideo(showImage: Boolean) {
        binding.imgNews.isVisible = showImage
        binding.btnShare.isVisible = showImage
    }

    private fun setNewsImage(
        newsList: List<NewsData>,
        position: Int,
        footerImage: ImageView,
        headerBackgroundImage: ImageView,
        imgNews: ImageView
    ) {

        val circleCrop: Transformation<Bitmap> = RoundedCorners(1)
        GlideApp.with(binding.root.context)
            .load(newsList[position].getPostItem()?.imageUrl.toSnImageUrl(firebaseHelper))
            .placeholder(R.drawable.ic_news_placeholder)
            .optionalTransform(WebpDrawable::class.java, WebpDrawableTransformation(circleCrop))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.btnShare.isVisible = false
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    if (resource is BitmapDrawable) {
                        val bitmap = resource.bitmap
                        //setFooterImage(bitmap, footerImage, headerBackgroundImage)
                    }
                    return false
                }
            })
            .into(binding.imgNews)
    }

    private fun setFooterImage(
        bitmap: Bitmap?,
        footerImage: ImageView,
        headerBackgroundImage: ImageView
    ) {
        bitmap?.let {
            val blurredBitmap = BlurBuilder.blur(binding.root.context, bitmap)
            footerImage.setImageBitmap(blurredBitmap)
            footerImage.setColorFilter(0x76AAAAAA, PorterDuff.Mode.MULTIPLY)
            headerBackgroundImage.setImageBitmap(blurredBitmap)
            headerBackgroundImage.setColorFilter(
                0x76AAAAAA,
                PorterDuff.Mode.MULTIPLY
            )
            binding.btnShare.isVisible = true
        }
    }

    private fun shareCard(
        newsCardListener: VerticalPagerAdapter.NewsCardListener?,
        screenshotView: LinearLayout,
        newsList: List<NewsData>,
        position: Int,
        firebaseAnalytics: FirebaseAnalytics
    ) {
        newsCardListener?.onShareClickListener(screenshotView, newsList[position])
        firebaseEvenLog(newsList, position, firebaseAnalytics)
    }

    private fun cardGestureDetection(
        newsList: List<NewsData>,
        position: Int
    ) {
        val mGestureDetector = GestureDetector(
            binding.root.context,
            object : CustomGestureListener(binding.root) {
                override fun onAngledSwipeUp(): Boolean {
                    return false
                }

                override fun onAngledSwipeDown(): Boolean {
                    return false
                }

                override fun onTouch(): Boolean {
                    if (binding.footer1.visibility === View.INVISIBLE) {
                        binding.footer1.visibility = View.VISIBLE
                        binding.footer2.visibility = View.INVISIBLE
                        binding.headerLayout.header.isVisible = true
                        //how many stroies are in the stack below
                        showNumberOfStoriesBelow(newsList, position)
                    } else {
                        binding.footer1.visibility = View.INVISIBLE
                        binding.footer2.visibility = View.VISIBLE
                        binding.headerLayout.header.isVisible = false
                    }

                    return false
                }
            })

        binding.newsCard.setOnTouchListener { _, event ->
            !mGestureDetector.onTouchEvent(event)
        }
    }

    private fun getRelativeTimeStamp(updatedAt: String?): CharSequence {
        if (updatedAt == null) return ""
        return DateUtils.timeAgo(
            binding.root.context,
            SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(updatedAt)
        )
    }

    private fun showNumberOfStoriesBelow(
        newsList: List<NewsData>,
        position: Int
    ) {
        val numberOfNewsBelow = newsList.count().minus(1).minus(position)
        Toast.makeText(
            binding.root.context,
            if (numberOfNewsBelow > 0) "$numberOfNewsBelow" + binding.root.context.getString(R.string.news_below) else "Last card below",
            Toast.LENGTH_LONG
        ).show()
    }

    private fun firebaseEvenLog(
        newsList: List<NewsData>,
        position: Int,
        firebaseAnalytics: FirebaseAnalytics
    ) {
        val bundle = Bundle()
        bundle.putString("shared_news_title", newsList[position].getPostItem()?.title)
        firebaseAnalytics.logEvent("news_card_share_page", bundle)
    }
}