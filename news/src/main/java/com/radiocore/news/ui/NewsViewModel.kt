package com.radiocore.news.ui

import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radiocore.news.data.repository.NewsRepository
import com.radiocore.news.state.NewsState
import kotlinx.coroutines.ExperimentalCoroutinesApi

class NewsViewModel
@ViewModelInject
constructor(
        private val repository: NewsRepository
) : ViewModel() {
    private var _newsState = MutableLiveData<NewsState>()

    val newsState: LiveData<NewsState>
        get() = _newsState

    fun setNewsState(state: NewsState) {
        _newsState.postValue(state)
    }

    @ExperimentalCoroutinesApi
    suspend fun getAllNews() = repository.loadAllNews()
}