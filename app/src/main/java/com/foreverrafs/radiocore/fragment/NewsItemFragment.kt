package com.foreverrafs.radiocore.fragment

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.foreverrafs.radiocore.R
import com.foreverrafs.radiocore.data.NewsRepository
import com.foreverrafs.radiocore.model.News
import com.foreverrafs.radiocore.util.Constants
import kotlinx.android.synthetic.main.fragment_news_item_detail.*
import kotlinx.android.synthetic.main.news_detail_content.*
import org.joda.time.format.DateTimeFormat

class NewsItemFragment : Fragment() {

    private var mNewsItem: News? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments

        if (bundle != null) {
            mNewsItem = bundle.getParcelable(Constants.KEY_NEWS_ITEM)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_news_item_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        image.transitionName = mNewsItem?.imageUrl
        val fmt = DateTimeFormat.forPattern("MMMM d, yyyy")
        val datePretty = mNewsItem!!.date!!.toString(fmt)

        tvHeadline.text = Html.fromHtml(mNewsItem?.headline)
        tvDate.text = datePretty
        tvContent.text = Html.fromHtml(mNewsItem?.content)
        tvCategory.text = mNewsItem?.category

        Glide.with(this)
                .load(mNewsItem?.imageUrl)
                .into(image!!)

    }

    companion object {

        fun getInstance(position: Int): NewsItemFragment {
            val newsItemAtPosition = NewsRepository.getNewsItems()!![position]

            val fragmentNewsItem = NewsItemFragment()
            val argument = Bundle()
            argument.putParcelable(Constants.KEY_NEWS_ITEM, newsItemAtPosition)
            fragmentNewsItem.arguments = argument
            return fragmentNewsItem
        }
    }

}