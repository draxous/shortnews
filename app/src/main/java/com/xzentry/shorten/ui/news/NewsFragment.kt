package com.xzentry.shorten.ui.news

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.util.Consumer
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.analytics.FirebaseAnalytics
import com.xzentry.shorten.BuildConfig
import com.xzentry.shorten.R
import com.xzentry.shorten.data.local.perfs.PreferencesHelper
import com.xzentry.shorten.databinding.FragmentNewsBinding
import com.xzentry.shorten.factory.ViewModelFactory
import com.xzentry.shorten.model.CardType
import com.xzentry.shorten.model.NewsData
import com.xzentry.shorten.ui.home.HomeActivity
import com.xzentry.shorten.ui.news.storage.StorageHelper
import com.xzentry.shorten.ui.news.viewholder.Event
import com.xzentry.shorten.ui.selectednews.SelectedNewsActivity
import com.xzentry.shorten.utils.FirebaseHelper
import com.xzentry.shorten.utils.NetworkUtils
import com.xzentry.shorten.utils.ScreenshotUtil
import com.xzentry.shorten.utils.network.ConnectivityChecker
import com.xzentry.shorten.utils.pagerTransformer.VerticalPageTransformer
import com.xzentry.shorten.utils.toPostWithCorrectCardType
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.DaggerFragment
import java.io.File
import javax.inject.Inject

private const val EXTRA_NOTIFICATION_POST_ID = "EXTRA_NOTIFICATION_POST_ID"

class NewsFragment : DaggerFragment() {


    private var refreshCompleted: Consumer<Boolean>? = null
    private lateinit var viewPager2PageChangeCallback: ViewPager2PageChangeCallback
    private var bitmap: Bitmap? = null

    @Inject
    lateinit var sharedPrefs: PreferencesHelper

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var firebaseHelper: FirebaseHelper

    @Inject
    lateinit var storageHelper: StorageHelper

    @Inject
    lateinit var connectivityChecker: ConnectivityChecker

    private var cardChangedListener: OnNewsCardChangeListener? = null
    private lateinit var verticalPagerAdapter: VerticalPagerAdapter
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var binding: FragmentNewsBinding

    //to avoid news refresh() scroll to position of push notification  (only after app opens from push notification)
    private var isShownNotificationPosition: Boolean = false
    private lateinit var newsViewModel: NewsViewModel

    interface OnNewsCardChangeListener {
        fun onCardChanged(selectedNews: NewsData)
        fun onFooterClicked(selectedNews: NewsData?)
    }

    fun setNewsCardChangedListener(listener: OnNewsCardChangeListener) {
        cardChangedListener = listener
    }

    companion object {
        fun newInstance(intent: Intent): NewsFragment = NewsFragment().apply {
            arguments = intent.extras
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        AndroidSupportInjection.inject(this)

        binding = FragmentNewsBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }
        newsViewModel = ViewModelProvider(this, viewModelFactory).get(NewsViewModel::class.java)

        firebaseAnalytics = FirebaseAnalytics.getInstance(binding.root.context)

        binding.swiperefresh.setOnRefreshListener {
            refresh()
        }
        (activity as HomeActivity).setOnGoogleSignUpCompleteListener(googleSignInListener)
        loadDataRemote()
        initSwipePager()
        observeData()
        checkForNewVersionIsAvailable()
        return binding.root
    }

    private fun loadAds() {
        if (NetworkUtils.isNetworkConnected(binding.root.context) && !isAppStartedByPushNotification) {  // don't load ads when app starts via push notification
            newsViewModel.loadAds()
        }
    }

    private fun loadDataRemote() {
        newsViewModel.loadPosts(NetworkUtils.isNetworkConnected(binding.root.context))
    }

    // don't load ads when app starts via push notification
    private val isAppStartedByPushNotification
        get() = arguments?.getString(EXTRA_NOTIFICATION_POST_ID) != null


    override fun onResume() {
        super.onResume()

        if (!NetworkUtils.isNetworkConnected(binding.root.context)) {
            Toast.makeText(binding.root.context, R.string.unable_to_connect, Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun refresh() {
        setPullToRefreshState(true)
        if (NetworkUtils.isNetworkConnected(binding.root.context)) {
            newsViewModel.loadPosts(NetworkUtils.isNetworkConnected(binding.root.context))
            resetSharedPerfs()
            binding.vPager.currentItem = 0
        } else {
            binding.swiperefresh.isRefreshing = false
            Toast.makeText(binding.root.context, R.string.unable_to_connect, Toast.LENGTH_LONG)
                .show()
        }
    }

    private fun setPullToRefreshState(state: Boolean) {
        binding.swiperefresh.isEnabled = state
        binding.swiperefresh.isRefreshing = state
    }

    private fun observeData() {

        newsViewModel.getPostsListLoadingCompleteLiveData()
            .observe(viewLifecycleOwner, Observer { loadingComplete ->
                if (loadingComplete) {
                    loadAds()
                }
            })

        newsViewModel.getPositionForPushNotificationPostIdsLiveData()
            .observe(viewLifecycleOwner, Observer { postPosition ->
                binding.vPager.currentItem = postPosition
            })

        newsViewModel.getPostsLiveData().observe(viewLifecycleOwner, Observer { resource ->
            binding.swiperefresh.isRefreshing = false

            if (resource.data != null && resource.data.isNotEmpty()) {
                //convert post entities to news data
                val newsData = resource.data.toPostWithCorrectCardType()
                //callback to notify newsViewHolder when refresh is completed
                refreshCompleted?.accept(null)
                //Viewpager loads current+ next page at same time
                //Due to this for the first page there is no way to get card data when it's visible.
                //Need to update live data (manually trigger) for this every time new data loads specifically then after
                //card change event will update that data properly
                val initialNewsData = if (!sharedPrefs.isUserOnBoard()) {
                    NewsData(null, CardType.Intro, null)
                } else {
                    newsData[0]
                }
                cardChangedListener?.onCardChanged(initialNewsData)

                verticalPagerAdapter.clearItems()
                verticalPagerAdapter.setItems(newsData)
                //update livedata with all loaded cards at the moment
                newsViewModel.getAllShownPostsLiveData()
                    .postValue(verticalPagerAdapter.getItems() ?: emptyList())

                //scroll to the post after push notification is clicked
                // flow: user click notification -> app loads as a new instance -> loads posts
                // -> after finished loading live data updated -> then viewpager scrolls to correct post
                arguments?.getString(EXTRA_NOTIFICATION_POST_ID)?.let { postId ->
                    if (postId.isNotEmpty() && !isShownNotificationPosition) {
                        isShownNotificationPosition = true
                        newsViewModel.getCardPositionForPostId(
                            newsData, postId.toInt()
                        )
                    }
                }

                //Intro cards gets added only if user haven't seen it
                if (!sharedPrefs.isUserOnBoard() && !isAppStartedByPushNotification) {
                    verticalPagerAdapter.insertCard(0, NewsData(null, CardType.IntroFb))
                    verticalPagerAdapter.insertCard(0, NewsData(null, CardType.Intro))
                }

                if (firebaseHelper.isNewVersionAvailable && !isAppStartedByPushNotification) {
                    verticalPagerAdapter.insertCard(2, NewsData(null, CardType.VersionUpdate))
                }
            }
        })

        newsViewModel.getLoadMorePostsLiveData().observe(viewLifecycleOwner, Observer { resource ->
            if (resource.data != null && resource.data.isNotEmpty()) {
                val newsData = resource.data.toPostWithCorrectCardType()
                Toast.makeText(
                    activity,
                    resource.data.size.toString() + getString(R.string.news_below),
                    Toast.LENGTH_LONG
                ).show()

                verticalPagerAdapter.setItems(newsData, true)

                //update livedata with all loaded cards at the moment
                newsViewModel.getAllShownPostsLiveData()
                    .postValue(verticalPagerAdapter.getItems() ?: emptyList())
            } else {
                verticalPagerAdapter.setItems(emptyList())
            }
        })

        newsViewModel.getAds()
            .observe(viewLifecycleOwner, Observer { adsList ->
                val newsList = verticalPagerAdapter.getItems()
                newsList?.run {
                    if (adsList?.isNotEmpty() == true) {
                        for (ad in adsList) {
                            if (newsList.size > ad?.position ?: 0) {
                                ad?.let { adv ->
                                    // show ads card only if user is on board
                                    if (sharedPrefs.isUserOnBoard() && !isAppStartedByPushNotification) {
                                        //avoid adding the same ad twice
                                        if (this.none { newsData -> newsData.getAd()?.id == adv.id }) {
                                            verticalPagerAdapter.insertCard(
                                                adv.position ?: 1,
                                                when (adv.type) {
                                                    1 -> NewsData(null, CardType.MultiPageAd, adv)
                                                    2 -> NewsData(
                                                        null,
                                                        CardType.MultiPageParallaxAd,
                                                        adv
                                                    )
                                                    else -> NewsData(
                                                        null,
                                                        CardType.MultiPageAd,
                                                        adv
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            })
    }

    private fun initSwipePager() {
        binding.vPager.let { viewPager ->
            viewPager.setPageTransformer(VerticalPageTransformer())
            verticalPagerAdapter =
                VerticalPagerAdapter(
                    activity,
                    firebaseHelper,
                    firebaseAnalytics,
                    sharedPrefs,
                    childFragmentManager,
                    activity?.lifecycle,
                    connectivityChecker,
                    binding.root.context.getString(R.string.my_feed)
                )
            viewPager.adapter = verticalPagerAdapter

            verticalPagerAdapter.setNewsCardListener(object :
                VerticalPagerAdapter.NewsCardListener {
                override fun onNewsCardShow(newsEntity: NewsData) {

                }

                override fun onClickRefreshFeed(refreshComplete: Consumer<Boolean>) {
                    refreshCompleted = refreshComplete
                    refresh()
                }

                override fun onClickGotoTopFeed() {
                    binding.vPager.currentItem = 0
                }

                override fun onShareClickListener(view: View, post: NewsData?) {
                    shareNewsImage(view, post)
                }

                override fun onBackPress() {
                }

                override fun onBookmarkClickListener(view: View) {
                    binding.vPager.currentItem = verticalPagerAdapter.itemCount - 1
                }

                override fun onCardSwipeUp(position: Int) {
                }

                override fun onLoadMoreOlderNews(position: Int) {
                    if (BuildConfig.DEBUG) {
                        Toast.makeText(context, "Load more started", Toast.LENGTH_LONG).show()
                    }

                    newsViewModel.loadMorePosts(NetworkUtils.isNetworkConnected(requireActivity()))
                }

                override fun onCardSwipeDown(position: Int) {

                }
            })

            verticalPagerAdapter.eventTriggerListener =
                object : VerticalPagerAdapter.CardEventTriggerListener {
                    override fun onEventTriggered(event: Event) {
                        when (event) {
                            is Event.OnLoadMoreItems -> {
                                newsViewModel.loadMorePosts(
                                    NetworkUtils.isNetworkConnected(
                                        requireActivity()
                                    )
                                )
                            }
                            is Event.OnOpenNetworkSettingsScreen -> {
                                activity?.startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS))
                            }
                            is Event.OnBackToTop -> {
                                binding.vPager.currentItem = 0
                            }
                            is Event.OnCardSkip -> {
                                binding.vPager.currentItem = binding.vPager.currentItem.plus(1)
                            }
                            is Event.OnPinnedImageClicked -> {
                                cardChangedListener?.onFooterClicked(event.selectedNewsData)
                            }
                            is Event.OnShareCard -> {
                                shareNewsImage(event.shareView, event.post)
                            }
                            is Event.OnOpenSourcePage -> {
                                requireActivity().startActivity(
                                    SelectedNewsActivity.newIntent(
                                        binding.root.context,
                                        event.source?.sourceId,
                                        event.source?.source
                                    )
                                )
                            }
                            is Event.OnUpdateVersion -> {
                                activity?.let {
                                    try {
                                        val uri =
                                            Uri.parse(BuildConfig.PLAYSTORE_URI + it.packageName)
                                        startActivity(Intent(Intent.ACTION_VIEW, uri))
                                    } catch (e: ActivityNotFoundException) {
                                        startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(BuildConfig.PLAYSTORE_URL + it.packageName)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

            viewPager2PageChangeCallback = ViewPager2PageChangeCallback {}
            viewPager.registerOnPageChangeCallback(viewPager2PageChangeCallback)
        }
    }

    private fun onCardChanged(position: Int) {
        activity?.let {
            if (NetworkUtils.isNetworkConnected(binding.root.context)) {
                //save last viewed news card position.
                sharedPrefs.setLastViewedCardPosition(position)
            }
            val posts: List<NewsData>? =
                newsViewModel.getAllShownPostsLiveData().value

            posts?.let {
                newsViewModel.getCurrentCardInfo().postValue(it[position])
                cardChangedListener?.onCardChanged(it[position])

            }
        }
    }

    private fun shareNewsImage(
        shareView: View,
        post: NewsData?
    ) {
        bitmap = ScreenshotUtil.instance?.takeScreenshotForView(shareView)
        bitmap?.let {
            it.run {
                storageHelper.saveBitmapToFile("shortnews_app", this)
            }
            openScreenshot(storageHelper.getStorageFile("shortnews_app"), post)
        }
    }

    private fun checkForNewVersionIsAvailable() {

        //Check new version before saving the item details
        val consumer: Consumer<String> = Consumer {
        }

        val consumerError: Consumer<String> = Consumer {

        }
        val consumerPinnedCardAvailable: Consumer<Boolean> = Consumer {
            if (firebaseHelper.pinnedImageUrlRemote?.isNotEmpty() == true &&
                !verticalPagerAdapter.getItems()
                    .isNullOrEmpty() && !verticalPagerAdapter.isCardExists(CardType.Pinned)
            ) {
                // show pinned card only if user is onboard and app not opend by push notifiation
                if (sharedPrefs.isUserOnBoard() && !isAppStartedByPushNotification) {
                    verticalPagerAdapter.insertCard(1, NewsData(null, CardType.Pinned))
                }
            }
        }

        firebaseHelper.remoteConfigUpdatesCheck(
            consumer,
            consumerError,
            isPinnedCardAvailable = consumerPinnedCardAvailable
        )
    }

    private fun openScreenshot(imageFile: File?, post: NewsData?) {
        imageFile?.let {
            val intent = Intent(Intent.ACTION_SEND)
            val uri = storageHelper.getFileUri(binding.root.context, it)
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message_text))
            intent.setDataAndType(uri, "image/png")
            activity?.packageManager?.let { pm ->
                if (intent.resolveActivity(pm) != null) {
                    startActivity(intent)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.vPager.adapter = null
        binding.vPager.unregisterOnPageChangeCallback(viewPager2PageChangeCallback)
        resetSharedPerfs()

    }

    private fun resetSharedPerfs() {
        sharedPrefs.setOldestLoadedNewsPostUpdatedAtTime("-1")
    }

    inner class ViewPager2PageChangeCallback(private val listener: (Int) -> Unit) :
        ViewPager2.OnPageChangeCallback() {
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }


        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
        }

        override fun onPageSelected(position: Int) {
            onCardChanged(position)
            when (position) {
                0 -> {
                    binding.swiperefresh.isEnabled = true
                    binding.swiperefresh.isRefreshing = false
                }
                2 -> {
                    //Happens only when user is just joined and doing if for the first time
                    // Swiping the first card
                    if (!sharedPrefs.isUserOnBoard()) {
                        verticalPagerAdapter.let { adapter ->
                            val newsList = adapter.getItems()
                            newsList?.let {
                                //Remove first and second intro cards
                                if (!newsList.isNullOrEmpty() && newsList.size > 1) {
                                    val firstCard = newsList[0]
                                    val secondCard = newsList[1]
                                    if (secondCard.getCardType() == CardType.IntroFb) {
                                        adapter.apply {
                                            remove(firstCard)
                                            remove(secondCard)
                                            binding.vPager.currentItem = 0
                                        }
                                        sharedPrefs.regsiterUserOnBoard(true)
                                    }
                                }
                            }
                        }

                    }
                    setPullToRefreshState(false)
                }
                else -> {
                    setPullToRefreshState(false)
                }
            }
        }
    }

    private val googleSignInListener = object : OnGoogleSignUpCompleteListener {
        override fun onGoogleSignUpSuccess() {
            refresh()
        }

        override fun onGoogleSignUpFail() {
            TODO("Not yet implemented")
        }

    }
}