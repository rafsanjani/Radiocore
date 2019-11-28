package com.foreverrafs.radiocore.api

import com.foreverrafs.radiocore.util.NewsGson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiServiceGenerator {
    companion object {
        private const val BASE_URL = "https://newscentral.herokuapp.com/"

        private val builder: Retrofit.Builder = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(NewsGson.instance!!))


        fun <T> createService(serviceClass: Class<T>): T {
            return builder.build().create(serviceClass)
        }
    }
}