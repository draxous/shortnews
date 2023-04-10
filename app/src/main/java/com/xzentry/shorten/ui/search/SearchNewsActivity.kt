package com.xzentry.shorten.ui.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.analytics.FirebaseAnalytics
import com.xzentry.shorten.R
import com.xzentry.shorten.databinding.ActivitySearchNewsBinding
import com.xzentry.shorten.factory.ViewModelFactory
import com.xzentry.shorten.utils.FirebaseHelper
import com.xzentry.shorten.utils.NetworkUtils
import com.xzentry.shorten.utils.SwipePanel.OnFullSwipeListener
import com.xzentry.shorten.utils.closeKeyboard
import dagger.android.AndroidInjection
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import javax.inject.Inject

class SearchNewsActivity : AppCompatActivity(), SearchNewsNavigator {

    private lateinit var searchResultsAdapter: SearchResultsAdapter
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var firebaseHelper: FirebaseHelper

    private lateinit var binding: ActivitySearchNewsBinding

    lateinit var searchNewsViewModel: SearchNewsViewModel

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context, SearchNewsActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_search_news)
        searchNewsViewModel =
            ViewModelProvider(this, viewModelFactory).get(SearchNewsViewModel::class.java)
        searchNewsViewModel.navigator = this
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        searchResultsAdapter = SearchResultsAdapter(this, firebaseHelper)
        binding.recSearchResults.adapter = searchResultsAdapter

        binding.btnSearch.setOnClickListener {
            if (NetworkUtils.isNetworkConnected(this)) {
                searchPosts()
            } else {
                Toast.makeText(this, R.string.unable_to_connect, Toast.LENGTH_LONG).show()
            }
        }

        binding.swipePanel.setOnFullSwipeListener(object : OnFullSwipeListener {
            override fun onFullSwipe(direction: Int) {
                finish()
                binding.swipePanel.close(direction)
            }
        })

        binding.recSearchResults.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(
                recyclerView: RecyclerView,
                motionEvent: MotionEvent
            ): Boolean {
                binding.recSearchResults.closeKeyboard()
                return false
            }
        })

        binding.txtSearch.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (NetworkUtils.isNetworkConnected(binding.root?.context)) {
                    changeLoaderVisibility(true)
                    searchPosts()
                } else {
                    Toast.makeText(
                        binding.root.context,
                        R.string.unable_to_connect,
                        Toast.LENGTH_LONG
                    ).show()
                }
                true
            } else false
        }

        setToolbar()
        observeLiveData()
    }

    private fun setToolbar() {
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }


    private fun observeLiveData() {
        searchNewsViewModel.getSearchResultsLiveData().observe(this, Observer { list ->
            setVisibilityWithResult(false)
            changeLoaderVisibility(false)
            list?.let {
                searchResultsAdapter.setItems(it)
            }

        })

        searchNewsViewModel.getSearchErrorLiveData().observe(this, Observer { list ->
            changeLoaderVisibility(false)
            Toast.makeText(
                binding.root.context,
                getString(R.string.no_search_result),
                Toast.LENGTH_LONG
            ).show()
            searchResultsAdapter.setItems(ArrayList())
            setVisibilityWithResult(true)
        })
    }

    private fun setVisibilityWithResult(isShow: Boolean) {
        binding.noResults.isVisible = isShow
        binding.recSearchResults.isVisible = !isShow
    }

    private fun changeLoaderVisibility(isShow: Boolean) {
        binding.recSearchResults.isVisible = !isShow
        binding.loaderLayout.rootView.isVisible = isShow
    }

    private fun searchPosts() {

        /* Fetch movies list  */
        searchNewsViewModel.loadNewsByCategoryId(binding.txtSearch.text.toString())
        firebaseEvenLog()
    }

    private fun firebaseEvenLog() {
        val bundle = Bundle()
        bundle.putString("search_query", binding.txtSearch.text.toString())
        firebaseAnalytics.logEvent("search_page", bundle)
    }


    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return false
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}