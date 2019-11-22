package com.foreverrafs.radiocore.data.remote

import com.foreverrafs.radiocore.model.News
import retrofit2.http.GET

interface NewsApi {
    @GET("/news")
    suspend fun getNewsAsyc(): List<News>
}