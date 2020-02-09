package com.radiocore.news.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.radiocore.core.util.Constants
import com.radiocore.news.R
import com.radiocore.news.data.NewsRepository
import com.radiocore.news.model.News
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
        val datePretty = mNewsItem!!.date.toString(fmt)

        tvHeadline.text = HtmlCompat.fromHtml(mNewsItem?.headline!!, HtmlCompat.FROM_HTML_MODE_LEGACY)
        tvDate.text = datePretty
        tvContent.text = HtmlCompat.fromHtml(mNewsItem?.content!!, HtmlCompat.FROM_HTML_MODE_LEGACY)
        tvCategory.text = mNewsItem?.category

        Glide.with(this)
                .load(mNewsItem?.imageUrl)
                .into(image!!)
    }

    companion object {
        fun getInstance(position: Int): NewsItemFragment {
            val newsItemAtPosition = NewsRepository.getInstance().radioCoreNews[position]

            val fragmentNewsItem = NewsItemFragment()
            val argument = Bundle()
            argument.putParcelable(Constants.KEY_NEWS_ITEM, newsItemAtPosition)
            fragmentNewsItem.arguments = argument
            return fragmentNewsItem
        }
    }

}