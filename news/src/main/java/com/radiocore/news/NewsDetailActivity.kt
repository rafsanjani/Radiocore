package com.radiocore.news

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.radiocore.core.util.KEY_SELECTED_NEWS_ITEM_POSITION
import com.radiocore.news.adapter.NewsPagerAdapter
import kotlinx.android.synthetic.main.news_detail_viewpager.*


class NewsDetailActivity : AppCompatActivity() {
    private var mNewsPosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.news_detail_viewpager)

        getIncomingIntent()

        val newsPagerAdapter = NewsPagerAdapter(this)
        viewPager.offscreenPageLimit = 1
        viewPager.adapter = newsPagerAdapter
        viewPager.currentItem = mNewsPosition

        tvFooter.text = getString(R.string.footer_copyright, getString(R.string.app_name))
    }

    private fun getIncomingIntent() {
        mNewsPosition = intent?.getIntExtra(KEY_SELECTED_NEWS_ITEM_POSITION, 0)!!
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
        }

        return true
    }

}