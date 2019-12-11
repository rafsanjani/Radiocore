package com.foreverrafs.radiocore.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.foreverrafs.radiocore.data.NewsRepository
import com.foreverrafs.radiocore.data.local.NewsDatabase
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

        Timber.i("Persisting ${newsItemsList.size} News Items to Local Storage")

        return Result.success()

    }
}