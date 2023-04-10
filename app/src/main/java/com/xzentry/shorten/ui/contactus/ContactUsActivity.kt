package com.xzentry.shorten.ui.contactus

import android.content.Context
import android.content.Intent
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
import com.xzentry.shorten.databinding.ActivityContactusBinding
import com.xzentry.shorten.factory.ViewModelFactory
import com.xzentry.shorten.ui.webview.WebViewActivity
import com.xzentry.shorten.utils.SwipePanel
import dagger.android.AndroidInjection
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import javax.inject.Inject

class ContactUsActivity : AppCompatActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var sharedPrefs: PreferencesHelper


    private lateinit var binding: ActivityContactusBinding

    private lateinit var contactUsViewModel: ContactUsViewModel

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, ContactUsActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_contactus)
        contactUsViewModel =
            ViewModelProvider(this, viewModelFactory).get(ContactUsViewModel::class.java)

        setToolbar()
        showTitleOnlyCollapsed()

        binding.txtShare.setOnClickListener {
            openShortNewsWeb()
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
                        this@ContactUsActivity,
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

    fun openShortNewsWeb() {
        val intent = WebViewActivity.newIntent(this)
        intent.putExtra(WebViewActivity.SOURCE_URL, "http://shortnews.work")
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