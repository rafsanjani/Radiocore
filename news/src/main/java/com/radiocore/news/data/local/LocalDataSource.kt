package com.radiocore.news.data.local

import android.content.Context
import com.radiocore.news.data.NewsDataSource
import com.radiocore.news.model.News

class LocalDataSource(val context: Context) : NewsDataSource {
    override suspend fun getNews(): List<News> {
        val database = NewsDatabase.getInstance(context)
        return database.newsDao().allNews
    }
}