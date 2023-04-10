package com.xzentry.shorten.ui.selectednews.viewpager

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.xzentry.shorten.R
import com.xzentry.shorten.databinding.FragmentWebviewBinding
import com.xzentry.shorten.ui.selectednews.SelectedNewsViewModel
import com.xzentry.shorten.ui.webview.IProgressListener
import com.xzentry.shorten.utils.NetworkUtils


class NewsByTopicWebViewFragment : Fragment() {
    private lateinit var adView: AdView
    private var webUrl: String = ""
    private var viewModel: SelectedNewsViewModel? = null

    var binding: FragmentWebviewBinding? = null

    fun setViewModel(viewModel: SelectedNewsViewModel) {
        this.viewModel = viewModel
    }

    companion object {
        fun newInstance(): NewsByTopicWebViewFragment = NewsByTopicWebViewFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentWebviewBinding.inflate(inflater, container, false)

        binding?.root?.context?.let {
            if (!NetworkUtils.isNetworkConnected(it)) {
                Toast.makeText(activity, R.string.unable_to_connect, Toast.LENGTH_LONG).show()
            }
        }

        loadWebView()
        loadAd()
        // observeData()

        return binding?.root
    }

    private fun loadWebView() {


        binding?.webView?.settings?.javaScriptEnabled = true // enable javascript
        binding?.webView?.settings?.loadWithOverviewMode = true
        binding?.webView?.settings?.useWideViewPort = true
        binding?.webView?.settings?.builtInZoomControls = false


        binding?.webView?.webViewClient = object : WebViewClient() {
            override fun onReceivedError(
                view: WebView?,
                errorCode: Int,
                description: String?,
                failingUrl: String?
            ) {
                Toast.makeText(activity, description, Toast.LENGTH_SHORT).show();
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


    private fun loadAd() {
        adView = AdView(
            activity,
            getString(R.string.topic_web_view_bottom_ad_id),
            AdSize.BANNER_HEIGHT_50
        )
        val adContainer = binding?.adView
        adContainer?.addView(adView)
        adView.loadAd()
    }
    /*private fun firebaseEvenLog(it: PostEntity) {
        val bundle = Bundle()
        bundle.putString("post_id", it.id.toString())
        bundle.putString("post_url", it.sourceUrl)
        firebaseAnalytics.logEvent("loaded_webview_page", bundle)
    }*/

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        ///Todo: always load web page if only settings is set to load always
        binding?.progressBar?.isVisible = menuVisible
        if (menuVisible) {
            viewModel?.getSelectedTopicLiveData()?.value?.let {
                binding?.webView?.loadUrl(it.sourceUrl)
                binding?.url?.text = it.source?.source
                //firebaseEvenLog(it)
            }
        } else {
            binding?.webView?.loadUrl("about:blank")
        }
    }

    class ShortNewsWebViewClient(private val mListener: ProgressListener) :
        WebChromeClient() {

        override fun onProgressChanged(view: WebView, newProgress: Int) {
            mListener.onUpdateProgress(newProgress)
            super.onProgressChanged(view, newProgress)
        }

        interface ProgressListener : IProgressListener {
            fun onUpdateProgress(progressValue: Int)
        }
    }

}
