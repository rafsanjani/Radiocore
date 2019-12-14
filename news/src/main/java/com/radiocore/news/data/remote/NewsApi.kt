package com.radiocore.news.data.remote

import retrofit2.http.GET

interface NewsApi {
    @GET("/news")
    suspend fun getNewsAsyc(): List<com.radiocore.news.model.News>
}