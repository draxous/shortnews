package com.xzentry.shorten.ui.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.AppBarLayout
import com.xzentry.shorten.BuildConfig
import com.xzentry.shorten.R
import com.xzentry.shorten.data.local.perfs.PreferencesHelper
import com.xzentry.shorten.databinding.ActivitySettingsBinding
import com.xzentry.shorten.factory.ViewModelFactory
import com.xzentry.shorten.ui.contactus.ContactUsActivity
import com.xzentry.shorten.ui.webview.WebViewActivity
import com.xzentry.shorten.utils.SwipePanel
import dagger.android.AndroidInjection
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import java.util.*
import javax.inject.Inject


class SettingsActivity : AppCompatActivity(), SettingsHelper {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var sharedPrefs: PreferencesHelper

    private lateinit var binding: ActivitySettingsBinding

    private lateinit var settingsViewModel: SettingsViewModel

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, SettingsActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings)
        settingsViewModel =
            ViewModelProvider(this, viewModelFactory).get(SettingsViewModel::class.java)

        setToolbar()
        showTitleOnlyCollapsed()

        binding.txtShare.setOnClickListener {
            shareApp()
        }
        binding.txtFeedback.setOnClickListener {
            sendFeedback()
        }
        binding.txtRate.setOnClickListener {
            rateApp()
        }
        binding.txtTermsConditions.setOnClickListener {
            readTermsAndConditions()
        }
        binding.txtPrivacyPolicy.setOnClickListener {
            readPrivacyPolicy()
        }
        binding.txtAboutUs.setOnClickListener {
            openContactUs()
        }

        binding.swipePanel.setOnFullSwipeListener(object : SwipePanel.OnFullSwipeListener {
            override fun onFullSwipe(direction: Int) {
                finish()
                binding.swipePanel.close(direction)
            }
        })

        binding.appVersion.text = getString(R.string.version, BuildConfig.VERSION_NAME)
    }


    private fun setToolbar() {
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    private fun showTitleOnlyCollapsed() {
        var isShow = true
        var scrollRange = -1
        binding.appBar.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { barLayout, verticalOffset ->
            if (scrollRange == -1) {
                scrollRange = barLayout?.totalScrollRange!!
            }
            if (scrollRange + verticalOffset == 0) {
                binding.collapsingToolbar.title = resources.getString(R.string.settings)
                binding.collapsingToolbar.setExpandedTitleColor(
                    ContextCompat.getColor(
                        this@SettingsActivity,
                        R.color.contentColor
                    )
                )
                isShow = true
            } else if (isShow) {
                binding.collapsingToolbar.title =
                    " " //careful there should a space between double quote otherwise it wont work
                isShow = false
            }
        })
    }

    override fun openContactUs() {
        val intent = ContactUsActivity.newIntent(this)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    override fun shareApp() {
        var sendIntent = Intent()
        sendIntent.action = Intent.ACTION_SEND
        sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Best app for reading News")
        sendIntent.putExtra(
            Intent.EXTRA_TEXT,
            getString(R.string.share_app_text)
        )
        sendIntent.type = "text/plain"
        if (sendIntent.resolveActivity(packageManager) != null) {
            startActivity(sendIntent)
        }
    }

    override fun rateApp() {
        try {
            val uri = Uri.parse(BuildConfig.PLAYSTORE_URI + packageName)
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(BuildConfig.PLAYSTORE_URL + packageName)
                )
            )
        }
    }

    override fun sendFeedback() {
        val mailto = "mailto:shortnews4u@gmail.com" +
                "?cc=" + "" +
                "&subject=" + Uri.encode("Feedback") +
                "&body=" + Uri.encode(getDeviceInfoOnFeedback())

        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.data = Uri.parse(mailto)

        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        }
    }

    private fun getDeviceInfoOnFeedback(): String {
        val builder = StringBuilder().apply {
            append(Date().time.toString() + ": ShortNews - Android feedback")
            append("\n")
            append("DeviceId${sharedPrefs.getDeviceId()}")
            append(System.getProperty("line.separator"))
            append("Device Name: " + Build.MODEL)
            append(System.getProperty("line.separator"))
            append("Android OS Version: " + Build.VERSION.RELEASE)
            append(System.getProperty("line.separator"))
            append(getString(R.string.feedback_info)) // append all before this line
        }
        return builder.toString()
    }

    override fun readTermsAndConditions() {
        val intent = WebViewActivity.newIntent(this)
        intent.putExtra(WebViewActivity.SOURCE_URL, "http://shortnews.work/sn_terms_and_conditions")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    override fun readPrivacyPolicy() {
        val intent = WebViewActivity.newIntent(this)
        intent.putExtra(WebViewActivity.SOURCE_URL, "http://shortnews.work/sn_privacy_policy")
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
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