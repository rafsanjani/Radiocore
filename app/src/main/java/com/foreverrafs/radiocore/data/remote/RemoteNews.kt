package com.foreverrafs.radiocore.data.remote

import com.foreverrafs.radiocore.api.ServiceGenerator
import com.foreverrafs.radiocore.data.INewsManager
import com.foreverrafs.radiocore.data.local.NewsDatabase
import com.foreverrafs.radiocore.model.News
import timber.log.Timber

/**
 * Remote news repository will always fetch from online no matter what
 */
class RemoteNews : INewsManager<News> {
    override suspend fun fetchNews(): List<News> {
        val newsApi = ServiceGenerator.createService(NewsApi::class.java)

        return try {
            newsApi.getNewsAsyc()
        } catch (e: Exception) {
            //return an empty list
            ArrayList()
        }
    }

    fun syncWithLocal(items: List<News>, database: NewsDatabase) {
        //clear local storage
        database.newsDao().clear()

        //store news items in localstore using room
        for (news: News in items) {
            database.newsDao().insert(news)
        }
        Timber.i("Adding ${items.size} News Items to Local Storage")
    }
}