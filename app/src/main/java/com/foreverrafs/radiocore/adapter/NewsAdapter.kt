package com.foreverrafs.radiocore.adapter

// Created by Emperor95 on 1/13/2019.

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.foreverrafs.radiocore.R
import com.foreverrafs.radiocore.databinding.ItemNewsBinding
import com.foreverrafs.radiocore.model.News
import kotlinx.android.synthetic.main.item_news_header__.view.*


class NewsAdapter(val list: List<News>, type: AnimationType, duration: Int) : AnimationAdapter(type, duration) {
    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_NEWS_ITEM = 1
    }

    private var listener: NewsItemClickListener? = null
    private var mHeaders: Int = 0
    private var isPreviousHeader: Boolean = false


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view: View = View(parent.context)

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
        return list.size
    }


    fun setOnNewsItemClickListener(listener: NewsItemClickListener) {
        this.listener = listener
    }

    /**
     * {@inheritDoc}
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val newsItem = list[if (isPreviousHeader) position - 1 else position]

        when (holder.itemViewType) {
            VIEW_TYPE_NEWS_ITEM -> {
                isPreviousHeader = false
                (holder as NewsHolder).bind(newsItem)
            }
            VIEW_TYPE_HEADER -> {
                isPreviousHeader = true
                (holder as NewsHeaderHolder).bind(newsItem)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        super.getItemViewType(position)
        if (position == 0) {
            return VIEW_TYPE_HEADER
        }

        val currentItemDate = list[position].date
        val pastItemDate = list[position - 1].date

        if (pastItemDate.year == currentItemDate.year &&
                pastItemDate.monthOfYear == currentItemDate.monthOfYear &&
                pastItemDate.dayOfMonth == currentItemDate.dayOfMonth) {
            return VIEW_TYPE_NEWS_ITEM
        } else if (!isPreviousHeader) {
            isPreviousHeader = true
            return VIEW_TYPE_HEADER
        }
        return VIEW_TYPE_NEWS_ITEM
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


    internal inner class NewsHolder(var binding: ItemNewsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(newsItem: News) {
            binding.newsItem = newsItem
            binding.executePendingBindings()

            Glide.with(binding.image)
                    .load(newsItem.imageUrl)
                    .error(R.drawable.newsimage)
                    .placeholder(R.drawable.newsimage)
                    .centerCrop()
                    .into(binding.image)

            binding.image.transitionName = newsItem.imageUrl

            itemView.rootView.setOnClickListener { listener?.onNewsItemClicked(adapterPosition, binding.image) }
            setAnimation(itemView, adapterPosition)
        }
    }

}
