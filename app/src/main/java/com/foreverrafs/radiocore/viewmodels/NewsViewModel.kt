package com.foreverrafs.radiocore.viewmodels

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.foreverrafs.radiocore.data.NewsRepository
import com.foreverrafs.radiocore.data.local.LocalNews
import com.foreverrafs.radiocore.data.remote.RemoteNews
import com.foreverrafs.radiocore.model.News
import com.foreverrafs.radiocore.util.Constants
import com.foreverrafs.radiocore.util.RadioPreferences
import com.foreverrafs.radiocore.workers.PersistNewsWorker
import kotlinx.coroutines.Dispatchers
import org.joda.time.DateTime
import org.joda.time.Hours
import timber.log.Timber

class NewsViewModel(application: Application) : AndroidViewModel(application) {

    private val mPreferences: RadioPreferences
    private var hoursBeforeExpire: Int
    private var lastFetchedTime: DateTime
    private val appContext: Context = application.applicationContext
    private lateinit var news: LiveData<List<News>>

    init {
        mPreferences = RadioPreferences(appContext)
        hoursBeforeExpire = mPreferences.cacheExpiryHours!!.toInt()
        lastFetchedTime = mPreferences.cacheStorageTime!!
    }

    //The viewmodel will decide whether we are fetching the news from online or local storage based on the cacheExpiryHours
    fun getAllNews(): LiveData<List<News>> {
        val elapsedHours = Hours.hoursBetween(lastFetchedTime, DateTime.now())

        val isCacheValid = (hoursBeforeExpire - elapsedHours.hours) >= 0

        val repository = if (isCacheValid) {
            Timber.i("Cache Valid for ${hoursBeforeExpire - elapsedHours.hours} Hours: Loading from local...")
            LocalNews(appContext)
        } else {
            Timber.i("Cache Expired: Loading from Remote...")
            mPreferences.cacheStorageTime = DateTime.now()
            RemoteNews()
        }

        return liveData(Dispatchers.IO) {
            val data = repository.fetchNews()

            //keep this inside our repository if it's not empty
            if (!data.isNullOrEmpty()) {
                NewsRepository.getInstance().radioCoreNews = data

                //save to localstorage if we fetched from online
                if (repository is RemoteNews)
                    saveNewsToLocalStorage()
            }
            emit(data)
        }
    }

    private fun saveNewsToLocalStorage() {
        val workManager = WorkManager.getInstance(appContext)
        val persistNewsRequest = OneTimeWorkRequestBuilder<PersistNewsWorker>().build()

        workManager.enqueueUniqueWork(Constants.PERSIST_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                persistNewsRequest)
    }


}