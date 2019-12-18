package com.radiocore.news.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.radiocore.news.data.NewsRepository
import com.radiocore.news.ui.NewsItemFragment

/**
 * Created by Rafsanjani on 7/24/2019
 */
class NewsPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int {
        return NewsRepository.getInstance().radioCoreNews.size
    }

    override fun createFragment(position: Int): Fragment {
        return NewsItemFragment.getInstance(position)
    }
}
