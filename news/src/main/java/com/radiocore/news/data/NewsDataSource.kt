package com.radiocore.news.data

import com.radiocore.news.model.News

interface NewsDataSource {
    suspend fun getNews(): List<News>
}