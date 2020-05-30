package com.radiocore.news.data.remote

import com.radiocore.news.model.News
import retrofit2.http.GET

interface NewsApi {
    @GET("/news/politics")
    suspend fun getNewsAsyc(): List<News>
}