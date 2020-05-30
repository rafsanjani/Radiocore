package com.radiocore.news.data.remote

import com.radiocore.core.util.RadioPreferences
import com.radiocore.news.data.NewsDataSource
import com.radiocore.news.model.News
import org.joda.time.DateTime
import timber.log.Timber
import javax.inject.Inject

/**
 * Remote news repository will always fetch from online no matter what
 */
class RemoteDataSource @Inject constructor(
        private val mPreferences: RadioPreferences,
        private val newsApi: NewsApi) : NewsDataSource {

//    private val mPreferences: RadioPreferences = RadioPreferences(context)

//    @Inject
//    lateinit var mPreferences: RadioPreferences


    override suspend fun getNews(): List<News> {
//        val newsApi = ApiServiceGenerator.createService(NewsApi::class.java)

        return try {
            val list = newsApi.getNewsAsyc()
            mPreferences.cacheStorageTime = DateTime.now()
            list
        } catch (e: Exception) {
            //Log the error and return an empty list
            Timber.e(e)
            listOf()
        }
    }
}