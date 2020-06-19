package com.radiocore.news.adapter

// Created by Emperor95 on 1/13/2019.

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.radiocore.news.databinding.ItemNewsBinding
import com.radiocore.news.model.News
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.*


class NewsAdapter(val listener: (news: News, adapterPosition: Int) -> Unit) : AnimationAdapter(AnimationType.BOTTOM_UP, 150) {

    private val asyncDifferCallback = object : DiffUtil.ItemCallback<News>() {
        override fun areItemsTheSame(oldItem: News, newItem: News): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: News, newItem: News): Boolean {
            return oldItem.id == newItem.id
        }

    }
    private val listDiffer = AsyncListDiffer(this, asyncDifferCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemBinding = ItemNewsBinding.inflate(inflater, parent, false)
        return NewsHolder(itemBinding)
    }

    override fun getItemCount(): Int {
        return listDiffer.currentList.size
    }


    /**
     * {@inheritDoc}
     */
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val newsItem = listDiffer.currentList[position]
        (holder as NewsHolder).bind(newsItem)
    }

    fun submitList(items: List<News>) = listDiffer.submitList(items)

    inner class NewsHolder(var binding: ItemNewsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(newsItem: News) = with(binding) {
            this.newsItem = newsItem
            executePendingBindings()

            image.transitionName = newsItem.imageUrl

            itemView.rootView.setOnClickListener {
                listener(newsItem, adapterPosition)
            }

            setAnimation(itemView, adapterPosition)
        }
    }
}
