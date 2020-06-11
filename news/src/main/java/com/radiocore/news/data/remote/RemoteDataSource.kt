package com.radiocore.news.data.remote

import com.radiocore.RadioPreferences
import com.radiocore.news.data.NewsDataSource
import com.radiocore.news.model.News
import org.joda.time.DateTime
import javax.inject.Inject

/**
 * Remote news repository will always fetch from online no matter what
 */
class RemoteDataSource @Inject constructor(
        private val mPreferences: RadioPreferences,
        private val newsApi: NewsApi) : NewsDataSource {


    override suspend fun getNews(): List<News> {
        val list = newsApi.getNewsAsyc()
        mPreferences.cacheStorageTime = DateTime.now()
        return list
    }
}