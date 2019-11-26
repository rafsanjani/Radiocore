package com.foreverrafs.radiocore.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.util.*

class HomeSectionsPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    private val mFragmentList = ArrayList<Fragment>()
    private val mFragmentTitleList = ArrayList<String>()

//    override fun getItem(position: Int): Fragment {
//        return mFragmentList[position]
//    }

//    override fun getCount(): Int {
//        return mFragmentList.size
//    }

    fun addFragment(fragment: Fragment, title: String) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }

//    override fun getPageTitle(position: Int): CharSequence? {
//        return mFragmentTitleList[position]
//    }

    override fun getItemCount(): Int {
        return mFragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return mFragmentList[position]
    }
}