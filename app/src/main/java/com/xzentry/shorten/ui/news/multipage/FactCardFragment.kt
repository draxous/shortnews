package com.xzentry.shorten.ui.news.multipage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.xzentry.shorten.data.remote.model.AdsModel
import com.xzentry.shorten.databinding.FragmentMultiPagerBinding
import dagger.android.support.AndroidSupportInjection

class FactCardFragment: Fragment() {

    companion object {
        const val EXTRA_MULTI_PAGER_AD_TYPE_ID = "EXTRA_MULTI_PAGER_AD_TYPE_ID"
        fun newInstance(adModel: AdsModel?): FactCardFragment = FactCardFragment().apply {
            adModel?.let {ad->
                arguments = Bundle().apply {
                    putParcelable(EXTRA_MULTI_PAGER_AD_TYPE_ID, ad)
                }
            }
        }
    }
    private var binding: FragmentMultiPagerBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        AndroidSupportInjection.inject(this)
        binding = FragmentMultiPagerBinding.inflate(inflater, container, false)

       /* arguments?.let {
           val ads = it.getParcelable<AdsModel>(EXTRA_MULTI_PAGER_AD_TYPE_ID)
            binding?.viewpager?.adapter = MultiPagerAdapter(requireActivity(), ads, eventTriggerListener)
            binding?.viewpager?.setPageTransformer(ParallaxTransformer())
        }*/
        
        return binding!!.root
    }
}