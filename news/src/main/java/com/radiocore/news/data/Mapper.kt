package com.radiocore.news.data

import com.radiocore.news.data.entities.NewsEntity
import com.radiocore.news.model.News


/* Created by Rafsanjani on 17/06/2020. */

typealias ObjectMapper<F, T> = ((from: F) -> T)

internal val newsMapper: ObjectMapper<NewsEntity, News> = {
    News(
            id = it.id,
            headline = it.headline,
            content = it.content,
            date = it.date,
            category = it.category,
            imageUrl = it.imageUrl
    )
}