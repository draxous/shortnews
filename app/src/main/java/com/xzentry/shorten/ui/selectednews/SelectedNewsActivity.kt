package com.xzentry.shorten.ui.selectednews

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.xzentry.shorten.R
import com.xzentry.shorten.data.remote.model.Post
import com.xzentry.shorten.databinding.ActivitySelectedNewsBinding
import com.xzentry.shorten.factory.ViewModelFactory
import com.xzentry.shorten.ui.selectednews.viewpager.NewsByTopicFragment
import com.xzentry.shorten.ui.selectednews.viewpager.NewsByTopicWebViewFragment
import com.xzentry.shorten.utils.NetworkUtils
import com.xzentry.shorten.utils.SwipePanel
import dagger.android.AndroidInjection
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import javax.inject.Inject


class SelectedNewsActivity : AppCompatActivity(), SelectedNewsNavigator {

    private var postList: List<Post>? = null
    private var topicId: Int = -1
    private var headerTitle: String? = "Feed"
    private var sourceId: Int = -1
    private var position: Int = -1

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var binding: ActivitySelectedNewsBinding

    lateinit var selectedNewsViewModel: SelectedNewsViewModel

    companion object {

        private const val INTENT_TOPIC_ID = "topic_id"
        private const val INTENT_FEED_NAME = "feed_name"
        private const val INTENT_SOURCE_ID = "source_id"
        private const val INTENT_POSTS_LIST = "postsList"
        private const val INTENT_POSTS_LIST_CLICKED_POSITION = "postsListClickedPostion"

        fun newIntent(context: Context, sourceId: Int?, name: String?): Intent {
            val intent = Intent(context, SelectedNewsActivity::class.java)
            intent.putExtra(INTENT_SOURCE_ID, sourceId)
            intent.putExtra(INTENT_FEED_NAME, name)
            return intent
        }

        fun newIntent(context: Context, category_id: Int, name: String?): Intent {
            val intent = Intent(context, SelectedNewsActivity::class.java)
            intent.putExtra(INTENT_TOPIC_ID, category_id)
            intent.putExtra(INTENT_FEED_NAME, name)
            return intent
        }

        fun newIntent(context: Context, postsList: ArrayList<Post?>, position: Int, name: String?): Intent {
            val intent = Intent(context, SelectedNewsActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelableArrayList(INTENT_POSTS_LIST, postsList)
            intent.putExtra(INTENT_POSTS_LIST_CLICKED_POSITION, position)
            intent.putExtra(INTENT_FEED_NAME, name)
            intent.putExtras(bundle)
            return intent
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_selected_news)
        selectedNewsViewModel =
            ViewModelProvider(this, viewModelFactory).get(SelectedNewsViewModel::class.java)
        selectedNewsViewModel.navigator = this

        intent.extras?.let {
            headerTitle = intent.getStringExtra(INTENT_FEED_NAME)
            topicId = intent.getIntExtra(INTENT_TOPIC_ID, -1)
            sourceId = intent.getIntExtra(INTENT_SOURCE_ID, -1)
            postList = intent.getParcelableArrayListExtra(INTENT_POSTS_LIST)
            position = intent.getIntExtra(INTENT_POSTS_LIST_CLICKED_POSITION, -1)
        }

        binding.swipePanel.setOnFullSwipeListener(object : SwipePanel.OnFullSwipeListener {
            override fun onFullSwipe(direction: Int) {
                finish()
                binding.swipePanel.close(direction)
            }
        })

        setViewPager()
        loadData()
        observeLiveData()
    }


    private fun observeLiveData() {

        selectedNewsViewModel.getErrorLoadingLiveData().observe(this, Observer { isLoadingError ->

            if (isLoadingError) {
                Toast.makeText(
                    this@SelectedNewsActivity,
                    getString(R.string.error_retry),
                    Toast.LENGTH_LONG
                ).show()
            }
        })
    }

    private fun loadData() {

        /* Fetch posts list  */
        when {
            sourceId != -1 -> {
                selectedNewsViewModel.loadNewsBySourceId(sourceId)
            }
            topicId != -1 -> {
                selectedNewsViewModel.loadNewsByCategoryId(topicId)
            }
            !postList.isNullOrEmpty() -> {
                selectedNewsViewModel.getPostsByTopicLiveData()
                    .postValue(postList!!.toList())
                selectedNewsViewModel.updateClickedPosition(position)
            }
        }
    }

    private fun setViewPager() {
        val pageOneFragment = NewsByTopicFragment.newInstance(headerTitle).apply {
            setViewModel(this@SelectedNewsActivity.selectedNewsViewModel)
            setPageTitle(headerTitle)
            setNewsCardChangedListener(object : NewsByTopicFragment.OnNewsCardChangeListener {
                override fun onCardChanged(position: Int) {
                    activity?.let {
                        if (NetworkUtils.isNetworkConnected(requireActivity())) {
                            val posts: List<Post?>? =
                                selectedNewsViewModel.getPostsByTopicLiveData().value
                            posts?.let {
                                if (it.size > position) {
                                    selectedNewsViewModel.getSelectedTopicLiveData()
                                        .postValue(it[position])
                                }
                            }
                        }
                    }
                }
            })
        }

        val pageTwoFragment = NewsByTopicWebViewFragment.newInstance().apply {
            setViewModel(this@SelectedNewsActivity.selectedNewsViewModel)
        }

        binding.pager.adapter = SelectedNewsPagerAdapter(
            supportFragmentManager,
            listOf(pageOneFragment, pageTwoFragment)
        )
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}