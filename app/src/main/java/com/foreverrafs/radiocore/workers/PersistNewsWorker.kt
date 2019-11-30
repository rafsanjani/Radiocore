package com.foreverrafs.radiocore.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.foreverrafs.radiocore.data.NewsRepository
import com.foreverrafs.radiocore.data.local.NewsDatabase
import com.foreverrafs.radiocore.model.News
import timber.log.Timber

class PersistNewsWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        val appContext = applicationContext
        val newsDao = NewsDatabase.getInstance(appContext).newsDao()
        val newsItemsList = NewsRepository.getInstance().radioCoreNews

        newsDao.clear()

        //store news items in localstore using room
        for (news: News in newsItemsList) {
            newsDao.insert(news)
        }

        Timber.i("Persisting ${newsItemsList.size} News Items to Local Storage")

        return Result.success()
    }
}