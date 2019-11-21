package com.foreverrafs.radiocore.data.local

import android.content.Context
import com.foreverrafs.radiocore.data.INewsManager
import com.foreverrafs.radiocore.model.News

class LocalNews(val context: Context) : INewsManager<News> {
    override suspend fun fetchNews(): List<News> {
        val database = NewsDatabase.getInstance(context)
        return database.newsDao().allNews
    }

}