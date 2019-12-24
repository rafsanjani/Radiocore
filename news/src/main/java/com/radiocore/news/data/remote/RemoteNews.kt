package com.radiocore.news.data.remote

import android.content.Context
import com.radiocore.core.util.RadioPreferences
import com.radiocore.news.api.ApiServiceGenerator
import com.radiocore.news.data.INewsManager
import com.radiocore.news.model.News
import org.joda.time.DateTime

/**
 * Remote news repository will always fetch from online no matter what
 */
class RemoteNews(private val context: Context) : INewsManager<News> {

    private val mPreferences: RadioPreferences = RadioPreferences(context)

    override suspend fun fetchNews(): List<News> {
        val newsApi = ApiServiceGenerator.createService(NewsApi::class.java)

        return try {
            val list = newsApi.getNewsAsyc()
            mPreferences.cacheStorageTime = DateTime.now()
            list
        } catch (e: Exception) {
            //return an empty list
            ArrayList()
        }
    }

}