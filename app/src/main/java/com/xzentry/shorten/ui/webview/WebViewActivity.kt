package com.xzentry.shorten.ui.webview

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import com.facebook.ads.AdSize
import com.facebook.ads.AdView
import com.xzentry.shorten.R
import com.xzentry.shorten.databinding.ActivityWebviewBinding
import com.xzentry.shorten.utils.NetworkUtils
import com.xzentry.shorten.utils.SwipePanel
import dagger.android.AndroidInjection

class WebViewActivity : AppCompatActivity() {

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, WebViewActivity::class.java)
        }

        internal const val SOURCE_URL: String = "source_url"
    }

    private lateinit var adView: AdView
    private lateinit var sourceUrl: String
    private lateinit var binding: ActivityWebviewBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)

        binding =
            DataBindingUtil.setContentView(this, com.xzentry.shorten.R.layout.activity_webview)

        intent.extras?.let {
            sourceUrl = it.getString(SOURCE_URL) ?: ""
        }

        binding.webView.settings.javaScriptEnabled = true

        binding.webView.loadUrl(sourceUrl)
        binding.url.text =
            "${getString(com.xzentry.shorten.R.string.app_name)} :${getString(com.xzentry.shorten.R.string.terms_conditions)}"

        binding.webView.webChromeClient =
            ShortNewsWebViewClient(object :
                ShortNewsWebViewClient.ProgressListener {
                override fun onUpdateProgress(progressValue: Int) {
                    binding.progressBar?.progress = progressValue

                }
            })

        binding.webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {

                if (NetworkUtils.isNetworkConnected(view.context)) {
                    view.loadUrl(url)

                } else {
                    binding.mainProgress.isVisible = false
                    Toast.makeText(view.context, R.string.unable_to_connect, Toast.LENGTH_LONG)
                        .show()
                }
                return true
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                binding.mainProgress.isVisible = true
            }

            override fun onPageCommitVisible(view: WebView?, url: String?) {
                super.onPageCommitVisible(view, url)
                binding.mainProgress.isVisible = false
            }

        }

        binding.swipePanel.setOnFullSwipeListener(object : SwipePanel.OnFullSwipeListener {
            override fun onFullSwipe(direction: Int) {
                finish()
                binding.swipePanel.close(direction)
            }
        })

        loadAd()
    }

    private fun loadAd() {
        adView = AdView(this, getString(R.string.privacy_policy_web_view_bottom_ad_id), AdSize.BANNER_HEIGHT_50)
        val adContainer = binding.adView
        adContainer.addView(adView)
        adView.loadAd()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (adView != null) {
            adView.destroy();
        }
        if(!supportFragmentManager.isDestroyed){
            super.onDestroy();
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
