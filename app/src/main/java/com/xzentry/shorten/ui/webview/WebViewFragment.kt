package com.xzentry.shorten.ui.webview

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.google.firebase.analytics.FirebaseAnalytics
import com.xzentry.shorten.BuildConfig
import com.xzentry.shorten.R
import com.xzentry.shorten.common.error
import com.xzentry.shorten.data.remote.model.Post
import com.xzentry.shorten.databinding.FragmentWebviewBinding
import com.xzentry.shorten.factory.ViewModelFactory
import com.xzentry.shorten.model.CardType
import com.xzentry.shorten.model.NewsData
import com.xzentry.shorten.ui.home.HomeActivity
import com.xzentry.shorten.utils.FirebaseHelper
import com.xzentry.shorten.utils.NetworkUtils
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class WebViewFragment : Fragment() {
    private lateinit var adView: AdView
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var viewModel: WebViewModel? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var firebaseHelper: FirebaseHelper

    var binding: FragmentWebviewBinding? = null

    companion object {
        fun newInstance(): WebViewFragment = WebViewFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        AndroidSupportInjection.inject(this)
        binding = FragmentWebviewBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
        }

        firebaseAnalytics = FirebaseAnalytics.getInstance(requireActivity())
        viewModel = ViewModelProvider(this, viewModelFactory).get(WebViewModel::class.java)

        activity?.let {
            firebaseAnalytics = FirebaseAnalytics.getInstance(it)
            (it as HomeActivity).setUpdateWebViewListener(object :
                HomeActivity.OnUpdateWebViewListener {
                override fun onUpdateWebView(selectedNews: NewsData) {
                    viewModel?.updateChangedNewsWebView(selectedNews)
                }

                override fun onFooterClicked(selectedNews: NewsData) {

                }
            })
        }

        loadWebView()

        return binding?.root
    }

    override fun onResume() {
        super.onResume()
        loadAd()
    }

    private fun loadWebView() {

        binding?.webView?.apply {

            settings?.javaScriptEnabled = true // enable javascript
            settings?.loadWithOverviewMode = true
            settings?.useWideViewPort = true
            settings?.builtInZoomControls = false

            webViewClient = object : WebViewClient() {
                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    if (NetworkUtils.isNetworkConnected(activity!!)) {
                        binding?.mainProgress?.isVisible = false
                        Toast.makeText(activity, R.string.unable_to_connect, Toast.LENGTH_LONG)
                            .show()
                    } else {
                        error("webview load failed :$description", null)
                    }
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    binding?.mainProgress?.isVisible = true
                }

                override fun onPageCommitVisible(view: WebView?, url: String?) {
                    super.onPageCommitVisible(view, url)
                    binding?.mainProgress?.isVisible = false
                }

                override fun onPageFinished(view: WebView?, url: String?) {

                }
            }
        }
    }

    private fun loadAd() {
        adView = AdView(activity, getString(R.string.ad_id), AdSize.BANNER_HEIGHT_50)
        val adContainer = binding?.adView
        adContainer?.addView(adView)
        adView.loadAd()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    private fun firebaseEvenLog(it: Post) {
        val bundle = Bundle()
        bundle.putString("post_id", it.id.toString())
        bundle.putString("post_url", it.sourceUrl)
        firebaseAnalytics.logEvent("loaded_webview_page", bundle)
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)

        if (menuVisible) {
            activity?.let {

                if (!NetworkUtils.isNetworkConnected(it)) {
                    Toast.makeText(
                        binding?.root?.context,
                        R.string.unable_to_connect,
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
            val post = viewModel?.getCurrentNewsLiveData()?.value
            when (post?.getCardType()) {
                CardType.News -> {
                    viewModel?.getCurrentNewsLiveData()?.value?.getPostItem()?.let {
                        loadWebViewForUrl(it.source?.source, it.sourceUrl)
                        firebaseEvenLog(it)
                    }
                }
                CardType.Pinned -> {
                    val pageUrl = firebaseHelper.pinnedWebUrlRemote
                    loadWebViewForUrl(pageUrl, pageUrl)
                }
                CardType.IntroFb -> {
                    loadWebViewForUrl(
                        BuildConfig.SN_FACEBOOK_PAGE_URL,
                        BuildConfig.SN_FACEBOOK_PAGE_URL
                    )
                }
                CardType.Intro -> {
                    loadWebViewForUrl(
                        BuildConfig.SN_INTRO_PAGE_URL,
                        BuildConfig.SN_INTRO_PAGE_URL
                    )
                }
                CardType.MultiPageAd -> {
                    viewModel?.getCurrentNewsLiveData()?.value?.getAd()?.let {
                        loadWebViewForUrl(it.redirectUrl, it.redirectUrl)
                    }
                }
                CardType.MultiPageParallaxAd -> {
                    viewModel?.getCurrentNewsLiveData()?.value?.getAd()?.let {
                        loadWebViewForUrl(it.redirectUrl, it.redirectUrl)
                        }
                }
                CardType.VersionUpdate -> {
                    loadWebViewForUrl(
                        getString(R.string.app_name),
                        BuildConfig.PLAYSTORE_URL + BuildConfig.APPLICATION_ID
                    )
                }
                CardType.GoogleSignIn -> {
                    loadWebViewForUrl(
                        getString(R.string.app_name),
                        BuildConfig.PLAYSTORE_URL + BuildConfig.APPLICATION_ID
                    )
                }
                else -> {
                }
            }
        } else { //clean the browser before next lcad
            binding?.webView?.loadUrl("about:blank")
        }
    }

    private fun loadWebViewForUrl(source: String?, sourceUrl: String?) {
        if (!binding?.root?.context?.let { NetworkUtils.isNetworkConnected(it) }!!) {
            Toast.makeText(binding?.root?.context, R.string.unable_to_connect, Toast.LENGTH_LONG)
                .show()
            binding?.mainProgress?.isVisible = false
        }
        sourceUrl?.let {
            if ("https://play.google.com/" in it) run {
                try {
                    activity?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it)))
                } catch (e: ActivityNotFoundException) {
                }
            } else {
                binding?.webView?.loadUrl(it)
            }
        }
        source?.let {
            binding?.url?.text = it
        }
    }
}
