package com.xzentry.shorten.ui.preview

import android.animation.ValueAnimator
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.databinding.DataBindingUtil
import com.xzentry.shorten.R
import com.xzentry.shorten.data.remote.model.Post
import com.xzentry.shorten.databinding.ActivityImagePreviewBinding
import com.xzentry.shorten.factory.ViewModelFactory
import com.xzentry.shorten.utils.FirebaseHelper
import com.xzentry.shorten.utils.SwipePanel
import com.xzentry.shorten.utils.toSnImageUrl
import dagger.android.AndroidInjection
import javax.inject.Inject

class ImagePreviewActivity : AppCompatActivity() {
    @Inject
    lateinit var firebaseHelper: FirebaseHelper

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    companion object {
        private const val KEY_IMAGE_TITLE = "image_title"
        private const val KEY_IMAGE_URL = "image_url"

        fun Context.imagePreviewIntent(post: Post?): Intent {
            return Intent(this, ImagePreviewActivity::class.java)
                .putExtra(KEY_IMAGE_TITLE, post?.title)
                .putExtra(KEY_IMAGE_URL, post?.imageUrl)
        }
    }

    private val isFullScreen
        get() = window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_FULLSCREEN == View.SYSTEM_UI_FLAG_FULLSCREEN

    private val url
        get() = intent.getStringExtra(KEY_IMAGE_URL)

    private val newsTitle
        get() = intent.getStringExtra(KEY_IMAGE_TITLE)

    private lateinit var binding: ActivityImagePreviewBinding
    private var navigationBarColor: Int = 0
    private var navigationBarColorTransparent: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidInjection.inject(this)
        window.decorView.apply {
            systemUiVisibility =
                systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }

        // Since navigation bar color doesn't contain an alpha channel, add it ourselves
        window.navigationBarColor.let {
            navigationBarColor = Color.argb(255, Color.red(it), Color.green(it), Color.blue(it))
            navigationBarColorTransparent =
                Color.argb(0, Color.red(it), Color.green(it), Color.blue(it))
        }

        binding = DataBindingUtil.setContentView<ActivityImagePreviewBinding>(
            this,
            R.layout.activity_image_preview
        ).apply {
            image.apply {
                setUrl(url.toSnImageUrl(firebaseHelper))
                setOnClickListener { toggleFullScreen() }
            }
            title.text = newsTitle

            swipePanel.setOnFullSwipeListener(object : SwipePanel.OnFullSwipeListener {
                override fun onFullSwipe(direction: Int) {
                    finish()
                    binding.swipePanel.close(direction)
                }
            })
        }
    }

    private fun toggleFullScreen() {
        val navBarInitialColor: Int
        val navBarFinalColor: Int

        val black = getColor(R.color.black)

        if (isFullScreen) {
            supportActionBar?.show()
            //binding.options.isGone = false
            navBarInitialColor = navigationBarColorTransparent
            navBarFinalColor = navigationBarColor
        } else {
            supportActionBar?.hide()
            //binding.options.isGone = true
            navBarInitialColor = navigationBarColor
            navBarFinalColor = navigationBarColorTransparent
        }

        // Animate the navigation bar color along with our bottom bar
        // because it looks nice
        ValueAnimator.ofArgb(navBarInitialColor, navBarFinalColor).run {
            addUpdateListener {
                window.navigationBarColor =
                    ColorUtils.compositeColors(it.animatedValue as Int, black)
            }

            start()
        }

        window.decorView.systemUiVisibility =
            window.decorView.systemUiVisibility xor View.SYSTEM_UI_FLAG_FULLSCREEN
    }

    /* fun shareImage(v: View) {
         fileName?.let { fileName ->
             getFileMimeType(File(fileName).extension.toLowerCase(Locale.getDefault()))?.let {
                 openShareActivity(fileName, it)
             }
         }
     }

     private fun openShareActivity(fileName: String, mimeType: String) {
         val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
             addCategory(Intent.CATEGORY_OPENABLE)
             type = mimeType
             putExtra(Intent.EXTRA_TITLE, fileName)
         }

         startActivityForResult(intent, REQUEST_SHARE)
     }*/
}