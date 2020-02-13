package com.radiocore.news.data.remote

import retrofit2.http.GET

interface NewsApi {
    @GET("/news/politics")
    suspend fun getNewsAsyc(): List<com.radiocore.news.model.News>
}