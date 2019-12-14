package com.radiocore.news.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.radiocore.news.data.NewsRepository
import com.radiocore.news.data.local.NewsDatabase

import timber.log.Timber

class PersistNewsWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val appContext = applicationContext
        val newsDao = NewsDatabase.getInstance(appContext).newsDao()
        val newsItemsList = NewsRepository.getInstance().radioCoreNews

        newsDao.clear()

        //store news items in localstore using room
        newsItemsList?.forEach { newsItem ->
            newsDao.insert(newsItem)
        }

        Timber.i("Saving ${newsItemsList.size} News Items to Local Storage")

        return Result.success()

    }
}