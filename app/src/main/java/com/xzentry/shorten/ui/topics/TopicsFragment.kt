package com.xzentry.shorten.ui.topics

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Consumer
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseUser
import com.xzentry.shorten.BuildConfig
import com.xzentry.shorten.R
import com.xzentry.shorten.data.local.perfs.PreferencesHelper
import com.xzentry.shorten.data.remote.model.Topic
import com.xzentry.shorten.databinding.FragmentTopicsBinding
import com.xzentry.shorten.di.module.GlideApp
import com.xzentry.shorten.factory.ViewModelFactory
import com.xzentry.shorten.ui.home.HomeActivity
import com.xzentry.shorten.ui.news.OnGoogleSignUpCompleteListener
import com.xzentry.shorten.ui.profile.ProfileActivity
import com.xzentry.shorten.ui.search.SearchNewsActivity
import com.xzentry.shorten.ui.settings.SettingsActivity
import com.xzentry.shorten.utils.FirebaseHelper
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject


class TopicsFragment : Fragment() {

    private var currentUser: FirebaseUser? = null

    @Inject
    lateinit var sharedPrefs: PreferencesHelper

    @Inject
    lateinit var firebaseHelper: FirebaseHelper

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var topicsListAdapter: TopicsListAdapter
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private var topicsViewModel: TopicsViewModel? = null

    private var binding: FragmentTopicsBinding? = null

    companion object {
        fun newInstance(): TopicsFragment = TopicsFragment()
        private const val REFRESH_DELAY: Long = 1000
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        AndroidSupportInjection.inject(this)
        val root = inflater.inflate(R.layout.fragment_topics, container, false)
        if (binding == null) {
            binding = FragmentTopicsBinding.bind(root)
        }
        topicsViewModel =
            ViewModelProvider(this, viewModelFactory).get(TopicsViewModel::class.java)

        firebaseAnalytics = FirebaseAnalytics.getInstance(requireActivity())

        topicsListAdapter = TopicsListAdapter(requireActivity(), firebaseAnalytics, firebaseHelper)
        binding?.recCategoryList?.layoutManager = GridLayoutManager(context, 3)
        binding?.recCategoryList?.adapter = topicsListAdapter

        binding?.txtSearch?.setOnClickListener {
            startActivity(SearchNewsActivity.newIntent(requireActivity()))
        }

        binding?.settings?.setOnClickListener {
            activity?.let {
                startActivity(SettingsActivity.newIntent(it))
            }
        }
        (activity as HomeActivity).setOnGoogleSignUpCompleteListener(googleSignInListener)
        checkForNewVersionIsAvailable()
        initialiseViewModel()

        return binding!!.root
    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = firebaseHelper.getUser()
        updateProfile(currentUser)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            currentUser = firebaseHelper.getUser()
            updateProfile(currentUser)
        }
    }

    private fun updateProfile(currentUser: FirebaseUser?) {
        currentUser?.let {
            var requestOptions = RequestOptions()
            requestOptions = requestOptions.transforms(CenterCrop(), RoundedCorners(50))
            binding?.account?.let { account ->
                GlideApp.with(this)
                    .load(currentUser?.photoUrl)
                    .placeholder(R.drawable.ic_account)
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

        binding?.account?.setOnClickListener {
            currentUser?.let { user ->
                startActivity(Intent(activity, ProfileActivity::class.java))
            } ?: run {
                val signInIntent =
                    activity?.let { context -> firebaseHelper.configGoogleSignIn(context).signInIntent }
                val activity = activity as HomeActivity
                activity.startActivityForResult(signInIntent, HomeActivity.RC_SIGN_IN)
            }
        }
    }


    private fun checkForNewVersionIsAvailable() {

        //Check new version to show force update message
        val consumer: Consumer<String> = Consumer {
        }

        val consumerError: Consumer<String> = Consumer {
            binding?.newUpdate?.isVisible = firebaseHelper.isNewVersionAvailable
            activity?.let {
                binding?.newUpdate?.text =
                    it.getString(R.string.new_update)
            }
            binding?.newUpdate?.setOnClickListener {

                activity?.let {
                    try {
                        val uri = Uri.parse(BuildConfig.PLAYSTORE_URI + it.packageName)
                        startActivity(Intent(Intent.ACTION_VIEW, uri))
                    } catch (e: ActivityNotFoundException) {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(BuildConfig.PLAYSTORE_URL + it.packageName)
                            )
                        )
                    }
                }
            }
        }

        firebaseHelper.remoteConfigUpdatesCheck(consumer, consumerError)
    }

    override fun onResume() {
        super.onResume()
        topicsViewModel?.loadTopics()
    }

    private fun initialiseViewModel() {
        topicsViewModel?.getTopicsLiveData()?.observe(viewLifecycleOwner, Observer { resource ->
            if (resource!!.isLoading) {
                // displayLoader()

            } else if (resource.data != null && resource.data.isNotEmpty()) {
                updateTopicsList(resource.data)
            }
        })
    }

    private fun updateTopicsList(topics: List<Topic>) {
        topicsListAdapter.setItems(topics)
    }

    private val googleSignInListener = object : OnGoogleSignUpCompleteListener {
        override fun onGoogleSignUpSuccess() {
            if (currentUser == null) {
                currentUser = firebaseHelper.getAuth().currentUser
            }
            updateProfile(currentUser)
        }

        override fun onGoogleSignUpFail() {
            TODO("Not yet implemented")
        }

    }
}
