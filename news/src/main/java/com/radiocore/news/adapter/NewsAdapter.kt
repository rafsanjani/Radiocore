package com.radiocore.news.adapter

// Created by Emperor95 on 1/13/2019.

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.radiocore.news.R
import com.radiocore.news.databinding.ItemNewsBinding
import com.radiocore.news.model.SectionedNews
import com.radiocore.stickyheaders.StickyHeaders
import kotlinx.android.synthetic.main.item_news_header__.view.*
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.format.DateTimeFormat
import timber.log.Timber


class NewsAdapter(val news: SectionedNews, val fragment: Fragment) : AnimationAdapter(AnimationType.BOTTOM_UP, 150), StickyHeaders {
    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_NEWS_ITEM = 1
    }

    private lateinit var listener: NewsItemClickListener


    private var listItems: MutableList<Any> = mutableListOf()


    init {
        if (fragment is NewsItemClickListener) {
            listener = fragment
        } else {
            Timber.e("Fragment must implement NewsItemClickListener")
        }

        listItems.addAll(news.list)

        var offset = 0
        news.headerPositions.forEach { headerPosition ->
            try {
                if (headerPosition == 0) {
                    listItems.add(0, news.list[0].date.toString())

                } else if (listItems[headerPosition - 1] is com.radiocore.news.model.News) {
                    listItems.add(++offset + headerPosition, news.list[headerPosition].date.toString())
                }

            } catch (ex: Exception) {

            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view = View(parent.context)

        if (viewType == VIEW_TYPE_HEADER) {
            view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_news_header__, parent, false)
            return NewsHeaderHolder(view)

        } else if (viewType == VIEW_TYPE_NEWS_ITEM) {
            val inflater = LayoutInflater.from(parent.context)
            val itemBinding = ItemNewsBinding.inflate(inflater, parent, false)
            return NewsHolder(itemBinding)
        }
        //let's hope that it doesn't get to this
        return NewsHeaderHolder(view)
    }

    override fun getItemCount(): Int {
        return listItems.size
    }


    /**
     * {@inheritDoc}
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val newsItem = listItems[position]

        when (holder.itemViewType) {
            VIEW_TYPE_NEWS_ITEM -> {
                (holder as NewsHolder).bind(newsItem as com.radiocore.news.model.News)
            }
            VIEW_TYPE_HEADER -> {
                (holder as NewsHeaderHolder).bind(newsItem as String)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (listItems[position] is String) {
            VIEW_TYPE_HEADER
        } else
            VIEW_TYPE_NEWS_ITEM
    }

    /**
     * Propagate click events to the RecyclervView to which this adapter is attached.
     */
    interface NewsItemClickListener {
        fun onNewsItemClicked(position: Int, image: ImageView)
    }

    internal inner class NewsHeaderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(headerDate: String) {
            val header = getPeriod(DateTime.parse(headerDate))

            itemView.tvHeaderDate.text = header
        }
    }

    private fun getPeriod(date: DateTime): String {
        val today = DateTime.now()

        val days = Days.daysBetween(date, today).days

        return when (days) {
            0 -> "Today"
            1 -> "Yesterday"
            in 2..5 -> date.dayOfWeek().asText
            in 6..10 -> "$days days ago"
            else -> date.toString(DateTimeFormat.forPattern("MMMM d, yyyy"))
        }
    }

    internal inner class NewsHolder(var binding: ItemNewsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(newsItem: com.radiocore.news.model.News) {
            binding.newsItem = newsItem
            binding.executePendingBindings()

            Glide.with(binding.image)
                    .load(newsItem.imageUrl)
                    .error(R.drawable.newsimage)
                    .placeholder(R.drawable.newsimage)
                    .centerCrop()
                    .into(binding.image)

            binding.image.transitionName = newsItem.imageUrl

            itemView.rootView.setOnClickListener { listener.onNewsItemClicked(adapterPosition, binding.image) }
            setAnimation(itemView, adapterPosition)
        }
    }

    override fun isStickyHeader(position: Int): Boolean {
        //the headers are strings, the news items are objects of type News
        return listItems[position] is String
    }

}
