package com.radiocore.news.data.repository

import com.radiocore.news.model.News
import kotlinx.coroutines.flow.Flow


/* Created by Rafsanjani on 18/06/2020. */

interface NewsRepository {
    suspend fun loadAllNews(): Flow<List<News>>
}