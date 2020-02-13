package com.radiocore.news.adapter

// Created by Emperor95 on 1/13/2019.

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.radiocore.news.R
import com.radiocore.news.databinding.ItemNewsBinding
import com.radiocore.news.model.News
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.format.DateTimeFormat
import timber.log.Timber


class NewsAdapter(val listItems: List<News>, val fragment: Fragment) : AnimationAdapter(AnimationType.BOTTOM_UP, 150) {

    private lateinit var listener: NewsItemClickListener


    init {
        if (fragment is NewsItemClickListener) {
            listener = fragment
        } else {
            Timber.e("Fragment must implement NewsItemClickListener")
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemBinding = ItemNewsBinding.inflate(inflater, parent, false)
        return NewsHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return listItems.size
    }


    /**
     * {@inheritDoc}
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val newsItem = listItems[position]
        (holder as NewsHolder).bind(newsItem)
    }


    /**
     * Propagate click events to the RecyclervView to which this adapter is attached.
     */
    interface NewsItemClickListener {
        fun onNewsItemClicked(position: Int, image: ImageView)
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

            itemView.rootView.setOnClickListener {
                listener.onNewsItemClicked(adapterPosition, binding.image)
            }
            setAnimation(itemView, adapterPosition)
        }
    }
}
