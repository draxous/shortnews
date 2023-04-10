package com.xzentry.shorten.ui.profile

import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.FirebaseUser
import com.xzentry.shorten.BuildConfig
import com.xzentry.shorten.R
import com.xzentry.shorten.data.local.perfs.PreferencesHelper
import com.xzentry.shorten.databinding.ActivityProfileBinding
import com.xzentry.shorten.di.module.GlideApp
import com.xzentry.shorten.factory.ViewModelFactory
import com.xzentry.shorten.utils.FirebaseHelper
import com.xzentry.shorten.utils.SwipePanel
import dagger.android.AndroidInjection
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import javax.inject.Inject

class ProfileActivity : AppCompatActivity() {

    private var currentUser: FirebaseUser? = null

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var sharedPrefs: PreferencesHelper

    @Inject
    lateinit var firebaseHelper: FirebaseHelper

    private lateinit var binding: ActivityProfileBinding

    private lateinit var profileViewModel: ProfileViewModel

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, ProfileActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile)
        profileViewModel =
            ViewModelProvider(this, viewModelFactory).get(ProfileViewModel::class.java)

        currentUser = firebaseHelper.getAuth().currentUser
        setToolbar()
        showTitleOnlyCollapsed()
        updateProfile()
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
                        this@ProfileActivity,
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

    private fun updateProfile() {
        currentUser?.let {
            binding.txtName.text = it.displayName
            binding.txtPhone.text = it.phoneNumber
            binding.txtEmail.text = it.email
            
            var requestOptions = RequestOptions()
            requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(100))
            binding.account.let { account ->
                GlideApp.with(this)
                    .load(currentUser?.photoUrl)
                    .placeholder(R.drawable.ic_account)
                    .apply( RequestOptions().override(300, 300))
                    .apply(requestOptions)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            // binding.btnShare.isVisible = false
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            if (resource is BitmapDrawable) {
                                val bitmap = resource.bitmap
                                //setFooterImage(bitmap, footerImage, headerBackgroundImage)
                            }
                            return false
                        }
                    })
                    .into(account)
            }
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