package com.radiocore.news.data.repository

import com.radiocore.news.data.database.NewsDao
import com.radiocore.news.data.networkBoundedFlow
import com.radiocore.news.data.newsMapper
import com.radiocore.news.data.api.NewsApi
import com.radiocore.news.model.News
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map


/* Created by Rafsanjani on 18/06/2020. */

class NewsRepositoryImpl
constructor(
        private val newsDao: NewsDao,
        private val newsApi: NewsApi
) : NewsRepository {
    @ExperimentalCoroutinesApi
    override suspend fun loadAllNews(): Flow<List<News>> {
        return networkBoundedFlow(
                newsDao.getAllNews().map { newsEntity ->
                    newsEntity.map { newsMapper(it) }
                },
                { newsDao.insert(it) },
                {
                    val news = newsApi.getAllNews()
                    news
                }
        ).flowOn(Dispatchers.IO)
    }
}