package com.foreverrafs.radiocore.data.remote

import com.foreverrafs.radiocore.model.News
import io.reactivex.Observable
import retrofit2.http.GET

interface NewsApi {
    @get:GET("/news")
    val allNews: Observable<List<News?>?>?

    @GET("/news")
    suspend fun getNewsAsyc(): List<News>
}