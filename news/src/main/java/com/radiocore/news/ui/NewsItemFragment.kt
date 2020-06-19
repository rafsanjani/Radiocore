package com.radiocore.news.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.radiocore.news.data.NewsObjects
import com.radiocore.news.databinding.FragmentNewsItemDetailBinding
import com.radiocore.news.model.News
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.*

class NewsItemFragment : Fragment() {

    private var mNewsItem: News? = null

    lateinit var binding: FragmentNewsItemDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = arguments

        if (bundle != null) {
            mNewsItem = bundle.getParcelable(KEY_NEWS_ITEM)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNewsItemDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding.layoutNewsDetail) {
            binding.image.transitionName = mNewsItem?.imageUrl

            mNewsItem?.let { news ->

                val date = ZonedDateTime.parse(news.date).toLocalDate()
                val month = date.month.getDisplayName(TextStyle.SHORT, Locale.ROOT)
                val day = date.dayOfMonth.toString()
                val year = date.year

                val formattedDate = "$month $day, $year"

                tvHeadline.text = HtmlCompat.fromHtml(news.headline, HtmlCompat.FROM_HTML_MODE_LEGACY)
                tvDate.text = formattedDate
                tvContent.text = HtmlCompat.fromHtml(news.content, HtmlCompat.FROM_HTML_MODE_LEGACY)
                tvCategory.text = news.category

                Glide.with(requireContext())
                        .load(news.imageUrl)
                        .into(binding.image)
            }
        }
    }

    companion object {
        const val KEY_NEWS_ITEM = "com.radiocore.news_item"

        fun getInstance(position: Int): NewsItemFragment {
            val newsItemAtPosition = NewsObjects.newsItems[position]

            val fragmentNewsItem = NewsItemFragment()
            val argument = Bundle()
            argument.putParcelable(KEY_NEWS_ITEM, newsItemAtPosition)
            fragmentNewsItem.arguments = argument
            return fragmentNewsItem
        }
    }

}