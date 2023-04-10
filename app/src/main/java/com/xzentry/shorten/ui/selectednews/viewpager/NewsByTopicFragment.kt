package com.xzentry.shorten.ui.selectednews.viewpager

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Consumer
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.analytics.FirebaseAnalytics
import com.xzentry.shorten.R
import com.xzentry.shorten.data.local.perfs.PreferencesHelper
import com.xzentry.shorten.databinding.FragmentNewsByTopicBinding
import com.xzentry.shorten.model.CardType
import com.xzentry.shorten.model.NewsData
import com.xzentry.shorten.ui.news.VerticalPagerAdapter
import com.xzentry.shorten.ui.news.storage.StorageHelper
import com.xzentry.shorten.ui.news.viewholder.Event
import com.xzentry.shorten.ui.selectednews.SelectedNewsViewModel
import com.xzentry.shorten.utils.FirebaseHelper
import com.xzentry.shorten.utils.ScreenshotUtil
import com.xzentry.shorten.utils.network.ConnectivityChecker
import com.xzentry.shorten.utils.pagerTransformer.VerticalPageTransformer
import com.xzentry.shorten.utils.toPostWithCorrectCardType
import dagger.android.support.AndroidSupportInjection
import java.io.File
import javax.inject.Inject

class NewsByTopicFragment : Fragment() {
    //flag to say this fragment triggered by searchActivity
    ///todo:Bad design re-factor later
    private var isFromSearchActivity: Boolean = false
    private var verticalViewPager: ViewPager2? = null
    private lateinit var viewPager2PageChangeCallback: ViewPager2PageChangeCallback
    private lateinit var cardChangedListener: OnNewsCardChangeListener
    private lateinit var verticalPagerAdapter: VerticalPagerAdapter
    private var viewModel: SelectedNewsViewModel? = null
    private var pageTitle: String? = "Feed"
    private lateinit var binding: FragmentNewsByTopicBinding
    private var bitmap: Bitmap? = null
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    @Inject
    lateinit var firebaseHelper: FirebaseHelper

    @Inject
    lateinit var sharedPrefs: PreferencesHelper

    @Inject
    lateinit var storageHelper: StorageHelper

    @Inject
    lateinit var connectivityChecker: ConnectivityChecker

    interface OnNewsCardChangeListener {
        fun onCardChanged(position: Int)
    }

    fun setNewsCardChangedListener(listener: OnNewsCardChangeListener) {
        cardChangedListener = listener
    }

    fun setViewModel(viewModel: SelectedNewsViewModel) {
        this.viewModel = viewModel
    }


    fun setPageTitle(title: String?) {
        this.pageTitle = title
    }

    companion object {
        private const val HEADER_TITLE = "HEADER_TITLE"

        fun newInstance(feedTitle: String?): NewsByTopicFragment {
            val newsByTopicFragment = NewsByTopicFragment()
            val args = Bundle().apply {
                putString(HEADER_TITLE, feedTitle)
            }
            newsByTopicFragment.arguments = args
            return newsByTopicFragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        AndroidSupportInjection.inject(this)
        binding = FragmentNewsByTopicBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@NewsByTopicFragment.viewModel
        }

        firebaseAnalytics = FirebaseAnalytics.getInstance(binding.root.context)

        initSwipePager()
        observeData()
        return binding.root
    }

    private fun observeData() {
        viewModel?.getPostsByTopicLiveData()?.observe(viewLifecycleOwner, Observer { posts ->

            cardChangedListener.onCardChanged(0)
            val newsData = posts.toPostWithCorrectCardType()
            verticalPagerAdapter.setItems(newsData)
        })

        viewModel?.getLoadMorePostsLiveData()?.observe(viewLifecycleOwner, Observer { posts ->
            if (posts != null && !isFromSearchActivity) {
                Toast.makeText(
                    activity,
                    if (posts.isEmpty()) "Last card below" else posts.size.toString() + getString(R.string.news_below),
                    Toast.LENGTH_LONG
                ).show()
                val newsData = posts.toPostWithCorrectCardType()
                verticalPagerAdapter.setItems(newsData, true)
            } else {
                verticalPagerAdapter.setItems(emptyList())
            }
        })

        viewModel?.getSearchPostClickedPositionLiveData()
            ?.observe(viewLifecycleOwner, Observer { position ->
                isFromSearchActivity = position != null
                binding.vPager.currentItem = position
            })
    }

    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).apply {
            (activity as AppCompatActivity).supportActionBar?.hide()
        }
    }

    private fun initSwipePager() {
        verticalViewPager = binding.vPager

        verticalViewPager?.let { viewPager ->
            viewPager.setPageTransformer(VerticalPageTransformer())

            verticalPagerAdapter = VerticalPagerAdapter(
                activity,
                firebaseHelper,
                firebaseAnalytics,
                sharedPrefs,
                childFragmentManager,
                activity?.lifecycle,
                connectivityChecker,
                arguments?.getString(HEADER_TITLE) ?: ""
            )
            viewPager.adapter = verticalPagerAdapter

            verticalPagerAdapter.setNewsCardListener(object :
                VerticalPagerAdapter.NewsCardListener {
                override fun onNewsCardShow(newsEntity: NewsData) {
                }

                override fun onClickRefreshFeed(refreshComplete: Consumer<Boolean>) {
                    binding.vPager.currentItem = 0
                    refreshComplete.accept(null)
                }

                override fun onClickGotoTopFeed() {
                    binding.vPager.currentItem = 0
                }

                override fun onShareClickListener(shareView: View, post: NewsData?) {
                    shareNewsImage(shareView)
                }

                override fun onBackPress() {
                    activity?.let {
                        it.onBackPressed()
                    }
                }

                override fun onBookmarkClickListener(view: View) {
                    binding.vPager.currentItem = verticalPagerAdapter.itemCount - 1
                }

                override fun onCardSwipeUp(position: Int) {
                }

                override fun onLoadMoreOlderNews(position: Int) {
                    val lastNewsItem = verticalPagerAdapter.getItems()
                        ?.first { item -> item.getCardType() == CardType.News }
                    lastNewsItem?.getPostItem()?.let { lastPostItem ->
                        viewModel?.loadMorePosts(
                            lastPostItem.category!!.catId,
                            lastPostItem.updatedAt!!
                        )
                    }
                }

                override fun onCardSwipeDown(position: Int) {
                }
            })

            verticalPagerAdapter.eventTriggerListener =
                object : VerticalPagerAdapter.CardEventTriggerListener {
                    override fun onEventTriggered(event: Event) {
                        when (event) {
                            is Event.OnLoadMoreItems -> {

                            }
                            is Event.OnOpenNetworkSettingsScreen -> {

                                activity?.startActivity(Intent(android.provider.Settings.ACTION_WIFI_SETTINGS))
                            }
                            is Event.OnBackToTop -> {
                                binding.vPager.currentItem = 0
                            }
                            is Event.OnCardSkip -> {
                                binding.vPager.currentItem =
                                    (binding.vPager.currentItem ?: 0).plus(1)
                            }
                            is Event.OnPinnedImageClicked -> {
                            }
                        }
                    }
                }

            viewPager2PageChangeCallback = ViewPager2PageChangeCallback {}
            viewPager.registerOnPageChangeCallback(viewPager2PageChangeCallback)

        }
    }

    private fun shareNewsImage(shareView: View) {
        bitmap = ScreenshotUtil.instance?.takeScreenshotForView(shareView)
        bitmap?.let {
            it.run {
                storageHelper.saveBitmapToFile("shortnews_app", this)
            }

            openScreenshot(storageHelper.getStorageFile("shortnews_app"))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        verticalViewPager?.let {
            it.adapter = null
        }
        binding.vPager.unregisterOnPageChangeCallback(viewPager2PageChangeCallback)
    }

    private fun openScreenshot(imageFile: File?) {
        imageFile?.let {
            val intent = Intent(Intent.ACTION_SEND)
            val uri = storageHelper.getFileUri(
                binding.root.context,
                it
            )

            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.putExtra(
                Intent.EXTRA_TEXT,
                getString(R.string.share_message_text)
            )
            intent.setDataAndType(uri, "image/png")
            activity?.packageManager?.let { pm ->
                if (intent.resolveActivity(pm) != null) {
                    startActivity(intent)
                }
            }
        }
    }


    inner class ViewPager2PageChangeCallback(private val listener: (Int) -> Unit) :
        ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            cardChangedListener.onCardChanged(position)
        }
    }
}