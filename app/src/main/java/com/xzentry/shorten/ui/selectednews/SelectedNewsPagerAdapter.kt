package com.xzentry.shorten.ui.selectednews

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter


class SelectedNewsPagerAdapter(
    fm: FragmentManager,
    private val pages: List<Fragment>
) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment = pages[position]

    override fun getCount(): Int = pages.size
}