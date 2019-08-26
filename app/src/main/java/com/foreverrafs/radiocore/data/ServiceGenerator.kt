package com.foreverrafs.radiocore.data

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class ServiceGenerator {
    companion object {
        const val BASE_URL = "https://newscentral.herokuapp.com/"

        private val builder: Retrofit.Builder = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(NewsJson.getInstance()))

        private val retrofit: Retrofit = builder.build()

        fun <T> createService(serviceClass: Class<T>): T {
            return retrofit.create(serviceClass)
        }
    }
}