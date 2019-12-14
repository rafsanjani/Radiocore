package com.radiocore.news.data.remote

import com.radiocore.news.api.ApiServiceGenerator
import com.radiocore.news.data.INewsManager
import com.radiocore.news.model.News

/**
 * Remote news repository will always fetch from online no matter what
 */
class RemoteNews : INewsManager<News> {
    override suspend fun fetchNews(): List<com.radiocore.news.model.News> {
        val newsApi = ApiServiceGenerator.createService(NewsApi::class.java)

        return try {
            newsApi.getNewsAsyc()
        } catch (e: Exception) {
            //return an empty list
            ArrayList()
        }
    }

}