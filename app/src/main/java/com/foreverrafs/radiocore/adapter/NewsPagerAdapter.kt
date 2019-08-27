package com.foreverrafs.radiocore.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.foreverrafs.radiocore.data.NewsDataManager
import com.foreverrafs.radiocore.fragment.NewsItemFragment

/**
 * Created by Rafsanjani on 7/24/2019
 */
class NewsPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return NewsItemFragment.getInstance(position)
    }

    override fun getCount(): Int {
        return NewsDataManager.RadioCoreNews.size
    }
}
