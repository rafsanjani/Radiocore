package com.radiocore.news.data.local

import android.content.Context
import com.radiocore.news.data.INewsManager
import com.radiocore.news.model.News

class LocalNews(val context: Context) : INewsManager<News> {
    override suspend fun fetchNews(): List<com.radiocore.news.model.News> {
        val database = NewsDatabase.getInstance(context)
        return database.newsDao().allNews
    }

}