package com.foreverrafs.radiocore.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.foreverrafs.radiocore.data.NewsRepository
import com.foreverrafs.radiocore.fragment.NewsItemFragment

/**
 * Created by Rafsanjani on 7/24/2019
 */
class NewsPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

//    override fun getItem(position: Int): Fragment {
//        return NewsItemFragment.getInstance(position)
//    }
//
//    override fun getCount(): Int {
//        return NewsRepository.getInstance().radioCoreNews.size
//    }

    override fun getItemCount(): Int {
        return NewsRepository.getInstance().radioCoreNews.size
    }

    override fun createFragment(position: Int): Fragment {
        return NewsItemFragment.getInstance(position)
    }
}
