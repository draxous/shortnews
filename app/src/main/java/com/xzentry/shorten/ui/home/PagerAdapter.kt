package com.xzentry.shorten.ui.home

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.xzentry.shorten.R


class PagerAdapter(
    private val context: Context, private val fm: FragmentManager,
    private val pages: List<Fragment>
) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment = pages[position]

    override fun getCount(): Int = pages.size

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            0 -> {
                context.resources.getString(R.string.discover)
            }
            else -> {
                ""
            }
        }
    }

}