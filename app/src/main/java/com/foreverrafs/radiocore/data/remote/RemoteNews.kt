package com.foreverrafs.radiocore.data.remote

import com.foreverrafs.radiocore.api.ApiServiceGenerator
import com.foreverrafs.radiocore.data.INewsManager
import com.foreverrafs.radiocore.model.News

/**
 * Remote news repository will always fetch from online no matter what
 */
class RemoteNews : INewsManager<News> {
    override suspend fun fetchNews(): List<News> {
        val newsApi = ApiServiceGenerator.createService(NewsApi::class.java)

        return try {
            newsApi.getNewsAsyc()
        } catch (e: Exception) {
            //return an empty list
            ArrayList()
        }
    }

}