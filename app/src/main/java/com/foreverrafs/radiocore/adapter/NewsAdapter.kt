package com.foreverrafs.radiocore.adapter

// Created by Emperor95 on 1/13/2019.

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.foreverrafs.radiocore.R
import com.foreverrafs.radiocore.model.News
import kotlinx.android.synthetic.main.item_news__.view.*
import org.joda.time.format.DateTimeFormat
import java.util.*


class NewsAdapter : AnimationAdapter {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_news__, parent, false)

        return NewsHolder(view)
    }

    private var newsList: List<News>? = null
    private var listener: NewsItemClickListener? = null

    constructor(list: List<News>?, type: AnimationType, duration: Int) : super(type, duration) {
        this.newsList = list
    }

    constructor() : super(AnimationType.LEFT_RIGHT, 200) {
        this.newsList = ArrayList()
    }

    override fun getItemCount(): Int {
        return newsList!!.size
    }


    fun setOnNewsItemClickListener(listener: NewsItemClickListener) {
        this.listener = listener
    }

    /**
     * {@inheritDoc}
     */
    override fun onBindViewHolder(newsViewHolder: RecyclerView.ViewHolder, position: Int) {
        val newsItem = newsList!![position]
        (newsViewHolder as NewsHolder).bind(newsItem)
    }


    /**
     * Propagate click events to the RecyclervView to which this adapter is attached.
     */
    interface NewsItemClickListener {
        fun onNewsItemClicked(position: Int)
    }


    internal inner class NewsHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(newsItem: News) {
            val formatter = DateTimeFormat.forPattern("MMMM d, yyyy")

            itemView.tvDate.text = newsItem.date!!.toString(formatter)
            itemView.tvHeadline.text = newsItem.headline
            itemView.tvCategory.text = newsItem.category


            Glide.with(itemView)
                    .load(newsItem.image)
                    .error(R.drawable.newsimage)
                    .placeholder(R.drawable.newsimage)
                    .centerCrop()
                    .into(itemView.image)

            itemView.setOnClickListener { listener?.onNewsItemClicked(adapterPosition) }
            setAnimation(itemView, adapterPosition)
        }
    }
}
