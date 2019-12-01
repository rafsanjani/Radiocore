package com.foreverrafs.radiocore.adapter

// Created by Emperor95 on 1/13/2019.

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.foreverrafs.radiocore.R
import com.foreverrafs.radiocore.model.News
import kotlinx.android.synthetic.main.item_news__.view.*
import kotlinx.android.synthetic.main.item_news_header__.view.*
import org.joda.time.format.DateTimeFormat
import java.util.*


class NewsAdapter : AnimationAdapter {
    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_NEWS_ITEM = 1
    }

    private var newsList: List<News>
    private var listener: NewsItemClickListener? = null

    constructor(list: List<News>, type: AnimationType, duration: Int) : super(type, duration) {
        this.newsList = list
    }

    constructor() : super(AnimationType.LEFT_RIGHT, 200) {
        this.newsList = ArrayList()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_HEADER) {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_news_header__, parent, false)
            return NewsHeaderHolder(view)
        } else if (viewType == VIEW_TYPE_NEWS_ITEM) {
            val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_news__, parent, false)
            return NewsHolder(view)
        }

        return NewsHolder(parent)
    }

    override fun getItemCount(): Int {
        return newsList.size
    }


    fun setOnNewsItemClickListener(listener: NewsItemClickListener) {
        this.listener = listener
    }

    /**
     * {@inheritDoc}
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val newsItem = newsList[position]

        when (holder.itemViewType) {
            VIEW_TYPE_NEWS_ITEM -> (holder as NewsHolder).bind(newsItem)
            VIEW_TYPE_HEADER -> (holder as NewsHeaderHolder).bind(newsItem)
        }
    }

    override fun getItemViewType(position: Int): Int {
        super.getItemViewType(position)
        if (position == 0) {
            return VIEW_TYPE_HEADER
        }

        val currentItemDate = newsList[position].date
        val pastItemDate = newsList[position - 1].date

        return if (pastItemDate.year == currentItemDate.year &&
                pastItemDate.monthOfYear == currentItemDate.monthOfYear &&
                pastItemDate.dayOfMonth == currentItemDate.dayOfMonth) {
            VIEW_TYPE_NEWS_ITEM
        } else {
            VIEW_TYPE_HEADER
        }
    }

    /**
     * Propagate click events to the RecyclervView to which this adapter is attached.
     */
    interface NewsItemClickListener {
        fun onNewsItemClicked(position: Int, image: ImageView)
    }

    internal inner class NewsHeaderHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(newsItem: News) {
            itemView.tvHeaderDate.text = newsItem.date.dayOfWeek().asText
        }
    }


    internal inner class NewsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(newsItem: News) {
            val formatter = DateTimeFormat.forPattern("MMMM d, yyyy")

            itemView.tvDate.text = newsItem.date.toString(formatter)
            itemView.tvHeadline.text = newsItem.headline
            itemView.tvCategory.text = newsItem.category


            Glide.with(itemView)
                    .load(newsItem.imageUrl)
                    .error(R.drawable.newsimage)
                    .placeholder(R.drawable.newsimage)
                    .centerCrop()
                    .into(itemView.image)

            itemView.image.transitionName = newsItem.imageUrl

            itemView.setOnClickListener { listener?.onNewsItemClicked(adapterPosition, itemView.image) }
            setAnimation(itemView, adapterPosition)
        }
    }
}
