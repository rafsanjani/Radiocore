package com.radiocore.news.state

import com.radiocore.news.model.News


/* Created by Rafsanjani on 31/05/2020. */

sealed class NewsState {
    object LoadingState : NewsState()
    class LoadedState(val news: List<News>) : NewsState()
    class ErrorState(val error: Throwable) : NewsState()
}