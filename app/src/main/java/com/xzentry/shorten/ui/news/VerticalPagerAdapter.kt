package com.xzentry.shorten.ui.news

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Consumer
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.xzentry.shorten.R
import com.xzentry.shorten.data.local.perfs.PreferencesHelper
import com.xzentry.shorten.databinding.*
import com.xzentry.shorten.model.CardType
import com.xzentry.shorten.model.NewsData
import com.xzentry.shorten.ui.news.viewholder.*
import com.xzentry.shorten.utils.FirebaseHelper
import com.xzentry.shorten.utils.NetworkUtils
import com.xzentry.shorten.utils.YouTubeHelper
import com.xzentry.shorten.utils.network.ConnectivityChecker
import javax.inject.Inject

class VerticalPagerAdapter @Inject constructor(
    private val context: Context?,
    private val firebaseHelper: FirebaseHelper,
    private val firebaseAnalytics: FirebaseAnalytics,
    private val sharedPrefs: PreferencesHelper,
    private val childFragmentManager: FragmentManager,
    private val lifecycle: Lifecycle?,
    private val connectivityChecker: ConnectivityChecker,
    private val headerTitle: String?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val newsList: ArrayList<NewsData> = ArrayList()
    internal lateinit var eventTriggerListener: CardEventTriggerListener
    private var previousPosition: Int = -1

    interface NewsCardListener {
        fun onNewsCardShow(newsEntity: NewsData)
        fun onClickRefreshFeed(refreshComplete: Consumer<Boolean>)
        fun onClickGotoTopFeed()
        fun onShareClickListener(view: View, post: NewsData?)
        fun onBackPress()
        fun onBookmarkClickListener(view: View)
        fun onCardSwipeUp(position: Int)
        fun onLoadMoreOlderNews(position: Int)
        fun onCardSwipeDown(position: Int)
    }

    private var newsCardListener: NewsCardListener? = null

    fun clearItems() {
        newsList.clear()
        notifyDataSetChanged()
    }

    fun setItems(
        posts: List<NewsData> = emptyList(),
        isLoadMore: Boolean = false
    ) {

        context?.let {

            // endless loading after card list go to end of the stack
            if (posts.isNotEmpty()) {
                if (isLoadMore) {
                    newsList.addAll(posts)
                    notifyDataSetChanged()
                } else {
                    newsList.addAll(0, posts)
                    notifyItemRangeInserted(0, posts.size)
                }
            }

            if (NetworkUtils.isNetworkConnected(context)) {
                // check @NoConnection card is in the newsList. if it is remove that item.
                ///Todo: optimize algo here
                if (isCardExists(CardType.NoConnection)) {
                    removeCardIfExists(CardType.NoConnection)
                }

                if (posts.isEmpty()) {
                    // add @Last card at the end of stack
                    if (!isCardExists(CardType.Last)) {
                        insertCard(newsList.size, NewsData(null, CardType.Last))
                    }
                } else {
                    // check no @Last card is in the newsList. if it is remove that item.
                    removeCardIfExists(CardType.Last)
                }

            } else {
                // check @NoConnection card is in the newsList. if it is remove that item.
                if (!isCardExists(CardType.NoConnection)) {
                    //if no internet available on the time of card update
                    // add @NoConnection card at the end of stack
                    insertCard(newsList.size, NewsData(null, CardType.NoConnection))
                }
            }
        }
        if (firebaseHelper.getAuth().currentUser == null) {
            // check @NoConnection card is in the newsList. if it is remove that item.
            if (!isCardExists(CardType.GoogleSignIn)) {
                //if no internet available on the time of card update
                // add @NoConnection card at the end of stack
                insertCard(0, NewsData(null, CardType.GoogleSignIn))
            }
        }
    }

    fun getItems(): ArrayList<NewsData>? {
        return newsList
    }

    // Remove a RecyclerView item containing a specified Data object
    fun remove(data: NewsData) {
        val position: Int = newsList.indexOf(data)
        newsList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun setNewsCardListener(listener: NewsCardListener) {
        newsCardListener = listener
    }

    override fun getItemViewType(position: Int): Int {
        return when (newsList[position].getCardType()) {
            CardType.News -> {
                CardType.News.value
            }
            CardType.AdNews -> {
                CardType.AdNews.value
            }
            CardType.FullAd -> {
                CardType.FullAd.value
            }
            CardType.MultiPageAd -> {
                CardType.MultiPageAd.value
            }
            CardType.NoConnection -> {
                CardType.NoConnection.value
            }
            CardType.Last -> {
                CardType.Last.value
            }
            CardType.Intro -> {
                CardType.Intro.value
            }
            CardType.IntroFb -> {
                CardType.IntroFb.value
            }
            CardType.Pinned -> {
                CardType.Pinned.value
            }
            CardType.MultiPageParallaxAd -> {
                CardType.MultiPageParallaxAd.value
            }
            CardType.VersionUpdate -> {
                CardType.VersionUpdate.value
            }
            CardType.Cartoon -> {
                CardType.Cartoon.value
            }
            CardType.GoogleSignIn -> {
                CardType.GoogleSignIn.value
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (CardType.parse(viewType)) {
            CardType.News -> {
                val binding = DataBindingUtil.inflate<ItemNewsLayoutBinding>(
                    inflater,
                    R.layout.item_news_layout,
                    parent,
                    false
                )
                lifecycle?.addObserver(binding.youtubePlayerView)
                NewsCardViewHolder(
                    binding,
                    firebaseHelper,
                    connectivityChecker,
                    binding.youtubePlayerView
                )
            }
            /*CardType.AdNews -> {
                val binding = DataBindingUtil.inflate<ItemNewsLayoutBinding>(
                    inflater,
                    R.layout.item_news_layout,
                    parent,
                    false
                )
                NewsCardViewHolder(binding, firebaseHelper)
            }
            CardType.FullAd -> {
                val binding = DataBindingUtil.inflate<ItemNewsLayoutBinding>(
                    inflater,
                    R.layout.item_news_layout,
                    parent,
                    false
                )
                NewsCardViewHolder(binding, firebaseHelper)
            }*/
            CardType.MultiPageAd -> {
                val binding = DataBindingUtil.inflate<MultipageAdCardBinding>(
                    inflater,
                    R.layout.multipage_ad_card,
                    parent,
                    false
                )

                MultiPagedAdCardViewHolder(binding, firebaseHelper, firebaseAnalytics)
            }
            CardType.MultiPageParallaxAd -> {
                val binding = DataBindingUtil.inflate<MultipageParralaxAdCardBinding>(
                    inflater,
                    R.layout.multipage_parralax_ad_card,
                    parent,
                    false
                )

                MultiPagedParallaxAdCardViewHolder(binding, firebaseHelper, firebaseAnalytics)
            }
            CardType.NoConnection -> {
                val binding = DataBindingUtil.inflate<NoInternetCardBinding>(
                    inflater,
                    R.layout.no_internet_card,
                    parent,
                    false
                )
                NoInternetCardViewHolder(binding)
            }
            CardType.Last -> {
                val binding = DataBindingUtil.inflate<LastCardBinding>(
                    inflater,
                    R.layout.last_card,
                    parent,
                    false
                )
                LastCardViewHolder(binding)
            }
            CardType.Intro -> {
                val binding = DataBindingUtil.inflate<IntroCardBinding>(
                    inflater,
                    R.layout.intro_card,
                    parent,
                    false
                )
                IntroCardViewHolder(binding)
            }
            CardType.IntroFb -> {
                val binding = DataBindingUtil.inflate<IntroFbCardBinding>(
                    inflater,
                    R.layout.intro_fb_card,
                    parent,
                    false
                )
                IntroFbCardViewHolder(binding)
            }
            CardType.Pinned -> {
                val binding = DataBindingUtil.inflate<PinnedCardBinding>(
                    inflater,
                    R.layout.pinned_card,
                    parent,
                    false
                )
                PinnedCardViewHolder(binding, firebaseHelper)
            }
            CardType.VersionUpdate -> {
                val binding = DataBindingUtil.inflate<VersionUpdateCardBinding>(
                    inflater,
                    R.layout.version_update_card,
                    parent,
                    false
                )
                VersionUpdateCardViewHolder(binding)
            }
            CardType.Cartoon -> {
                val binding = DataBindingUtil.inflate<ItemCartoonLayoutBinding>(
                    inflater,
                    R.layout.item_cartoon_layout,
                    parent,
                    false
                )
                CartoonCardViewHolder(
                    binding,
                    firebaseHelper,
                    connectivityChecker
                )
            }
            CardType.GoogleSignIn -> {
                val binding = DataBindingUtil.inflate<ItemGoogleSigninLayoutBinding>(
                    inflater,
                    R.layout.item_google_signin_layout,
                    parent,
                    false
                )
                GoogleSignInCardViewHolder(
                    binding,
                    firebaseHelper,
                    connectivityChecker,
                    this
                )
            }
            else -> {
                val binding = DataBindingUtil.inflate<ItemNewsLayoutBinding>(
                    inflater,
                    R.layout.item_news_layout,
                    parent,
                    false
                )
                NewsCardViewHolder(
                    binding,
                    firebaseHelper,
                    connectivityChecker,
                    binding.youtubePlayerView
                )
            }
        }
    }

    override fun getItemCount() = newsList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        detectCardSwipingDirection(position)
        detectLastCardForLoadMore(position)

        when (holder) {
            is NewsCardViewHolder -> {
                holder.updateView(
                    newsList,
                    position,
                    newsCardListener,
                    eventTriggerListener,
                    firebaseAnalytics,
                    newsList[position].getPostItem()?.videoUrl?.let {
                        YouTubeHelper.extractVideoIdFromUrl(it)
                    },
                    headerTitle
                )
            }
            is NoInternetCardViewHolder -> {
                holder.updateView(eventTriggerListener)
            }
            is LastCardViewHolder -> {
                holder.updateView(position, eventTriggerListener)
            }
            is IntroCardViewHolder -> {
                holder.updateView(position, eventTriggerListener)
            }
            is IntroFbCardViewHolder -> {
                holder.updateView(position, eventTriggerListener)
            }
            is PinnedCardViewHolder -> {
                holder.updateView(newsList, position, eventTriggerListener)
            }
            is MultiPagedAdCardViewHolder -> {
                holder.updateView(
                    newsList,
                    position,
                    eventTriggerListener,
                    childFragmentManager
                )
            }
            is MultiPagedParallaxAdCardViewHolder -> {
                holder.updateView(
                    newsList,
                    position,
                    eventTriggerListener,
                    childFragmentManager
                )
            }
            is VersionUpdateCardViewHolder -> {
                holder.updateView(
                    position,
                    eventTriggerListener,
                    firebaseAnalytics
                )
            }
            is CartoonCardViewHolder -> {
                holder.updateView(
                    newsList,
                    position,
                    newsCardListener,
                    eventTriggerListener,
                    firebaseAnalytics,
                    newsList[position].getPostItem()?.videoUrl?.let {
                        YouTubeHelper.extractVideoIdFromUrl(it)
                    },
                    headerTitle
                )
            }
            is GoogleSignInCardViewHolder -> {
                holder.updateView("Sign Up", newsCardListener, position)
            }
        }
    }

    private fun detectLastCardForLoadMore(position: Int) {
        if (position == newsList.size.minus(1) && newsList[position].getCardType() == CardType.News) {
            newsCardListener?.onLoadMoreOlderNews(position)
        }
    }

    private fun detectCardSwipingDirection(position: Int) {
        if (position != previousPosition && position > previousPosition) {
            newsCardListener?.onCardSwipeUp(position)
        } else {
            newsCardListener?.onCardSwipeDown(position)
        }
        previousPosition = position
    }

    interface CardEventTriggerListener {
        fun onEventTriggered(event: Event)
    }

    private fun removeCardIfExists(cardType: CardType) {
        newsList.indexOfFirst { it.getCardType() == cardType }.let { position ->
            if (position != -1) { //-1 if the list does not contain such element.
                newsList.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    private fun changeCardTypeByCategory(news: NewsData) {
        newsList.indexOfFirst { it.getPostItem()?.category ?: "" == "Cartoon" }.let { position ->
            if (position != -1) { //-1 if the list does not contain such element.
                newsList.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }

    // Insert a new item to the RecyclerView on a predefined position
    // one type of card can be only add once
    fun insertCard(position: Int, data: NewsData) {
        newsList.add(position, data)
        notifyItemInserted(position)
    }

    internal fun isCardExists(cardType: CardType): Boolean {
        return newsList.find { it.getCardType() == cardType }?.getCardType() != null
    }
}

