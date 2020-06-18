package com.radiocore.news.data.api

import com.radiocore.news.data.entities.NewsEntity
import retrofit2.http.GET

interface NewsApi {
    @GET("/news/")
    suspend fun getAllNews(): List<NewsEntity>
}