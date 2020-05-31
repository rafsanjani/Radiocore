package com.radiocore.news.ui

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.radiocore.core.util.Constants
import com.radiocore.core.util.RadioPreferences
import com.radiocore.news.data.NewsDataSource
import com.radiocore.news.data.NewsRepository
import com.radiocore.news.data.local.LocalDataSource
import com.radiocore.news.data.remote.RemoteDataSource
import com.radiocore.news.model.News
import com.radiocore.news.util.NewsState
import com.radiocore.news.util.NewsState.ErrorState
import com.radiocore.news.workers.PersistNewsWorker
import kotlinx.coroutines.Dispatchers
import org.joda.time.DateTime
import org.joda.time.Hours
import timber.log.Timber
import javax.inject.Inject

class NewsViewModel @Inject constructor(
        mPreferences: RadioPreferences,
        private val appContext: Context,
        private val remoteDataSource: RemoteDataSource) : ViewModel() {

    private var hoursBeforeExpire: Int = mPreferences.cacheExpiryHours!!.toInt()
    private var lastFetchedTime: DateTime = mPreferences.cacheStorageTime!!

    private var _newsState = MutableLiveData<NewsState>()

    val newsState: LiveData<NewsState>
        get() = _newsState

    fun setNewsState(state: NewsState) {
        _newsState.postValue(state)
    }

    //The ViewModel will decide whether we are fetching the news from online or local storage based on the cacheExpiryHours
    fun getAllNews(): LiveData<List<News>> {
        val elapsedHours = Hours.hoursBetween(lastFetchedTime, DateTime.now())

        val isCacheValid = (hoursBeforeExpire - elapsedHours.hours) >= 0

        val repository = if (isCacheValid) {
            Timber.i("Cache Valid for ${hoursBeforeExpire - elapsedHours.hours} Hours: Loading from local...")
            LocalDataSource(appContext)
        } else {
            Timber.i("Cache Expired: Loading from Remote...")
            remoteDataSource
        }

        return emitNewsItems(repository)
    }

    private fun emitNewsItems(repository: NewsDataSource): LiveData<List<News>> {
        return liveData(Dispatchers.IO) {
            try {
                val data = repository.getNews()
                //keep this inside our repository if it's not empty
                if (data.isNotEmpty()) {
                    NewsRepository.newsItems = data

                    //save to localstorage if we fetched from online
                    if (repository is RemoteDataSource)
                        saveNewsToLocalStorage()
                } else {
                    emit(listOf<News>())
                }

                NewsRepository.newsItems = data
                emit(data)
            } catch (e: Exception) {
                setNewsState(ErrorState(e))
            }
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