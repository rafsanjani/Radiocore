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
import kotlinx.android.synthetic.main.news_detail_content.*
import org.joda.time.format.DateTimeFormat

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
        with(binding) {
            image.transitionName = mNewsItem?.imageUrl
            val formatter = DateTimeFormat.forPattern("MMMM d, yyyy")

            mNewsItem?.let { news ->
                val datePretty = news.date.toString(formatter)

                tvHeadline.text = HtmlCompat.fromHtml(news.headline, HtmlCompat.FROM_HTML_MODE_LEGACY)
                tvDate.text = datePretty
                tvContent.text = HtmlCompat.fromHtml(news.content, HtmlCompat.FROM_HTML_MODE_LEGACY)
                tvCategory.text = news.category

                Glide.with(requireContext())
                        .load(news.imageUrl)
                        .into(image)
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