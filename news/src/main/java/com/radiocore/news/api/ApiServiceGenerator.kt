package com.radiocore.news.api

import com.radiocore.core.util.Constants
import com.radiocore.news.util.GsonConverters
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiServiceGenerator {
    companion object {
//        private const val BASE_URL = "https://newscentral.herokuapp.com/"

        private val builder: Retrofit.Builder = Retrofit.Builder()
                .baseUrl(Constants.NEWS_URL)
                .addConverterFactory(GsonConverterFactory.create(GsonConverters.instance!!))

        fun <T> createService(serviceClass: Class<T>): T {
            return builder.build().create(serviceClass)
        }
    }
}