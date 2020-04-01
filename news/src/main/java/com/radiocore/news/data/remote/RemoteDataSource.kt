package com.radiocore.news.data.remote

import android.content.Context
import com.radiocore.core.util.RadioPreferences
import com.radiocore.news.api.ApiServiceGenerator
import com.radiocore.news.data.NewsDataSource
import com.radiocore.news.model.News
import org.joda.time.DateTime
import timber.log.Timber

/**
 * Remote news repository will always fetch from online no matter what
 */
class RemoteDataSource(context: Context) : NewsDataSource {

    private val mPreferences: RadioPreferences = RadioPreferences(context)

    override suspend fun getNews(): List<News> {
        val newsApi = ApiServiceGenerator.createService(NewsApi::class.java)

        return try {
            val list = newsApi.getNewsAsyc()
            mPreferences.cacheStorageTime = DateTime.now()
            list
        } catch (e: Exception) {
            //Log the error and return an empty list
            Timber.e(e)
            listOf()
        }
    }
}