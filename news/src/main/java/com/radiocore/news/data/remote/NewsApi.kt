package com.radiocore.news.data.remote

import com.radiocore.news.model.News
import retrofit2.http.GET

interface NewsApi {
    @GET("/news/")
    suspend fun getNewsAsyc(): List<News>
}