package com.foreverrafs.radiocore.activity

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import butterknife.ButterKnife
import com.foreverrafs.radiocore.R
import com.foreverrafs.radiocore.adapter.NewsPagerAdapter
import com.foreverrafs.radiocore.util.Constants
import kotlinx.android.synthetic.main.activity_home.*

class NewsDetailActivity : AppCompatActivity() {
    private var mNewsPosition: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_news_item_detail_pager)

        ButterKnife.bind(this)

        getIncomingIntent()

        val newsPagerAdapter = NewsPagerAdapter(supportFragmentManager)
        viewPager.offscreenPageLimit = 1
        viewPager.adapter = newsPagerAdapter
        viewPager.currentItem = mNewsPosition
    }


    private fun getIncomingIntent() {
        mNewsPosition = intent!!.getIntExtra(Constants.KEY_SELECTED_NEWS_ITEM_POSITION, 0)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }

        return true
    }

}