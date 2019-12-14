package com.radiocore.news.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.radiocore.news.model.News

@Dao
interface NewsDao {
    @get:Query("SELECT * FROM news ORDER BY date DESC")
    val allNews: List<com.radiocore.news.model.News>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(new: News)

    @Query("DELETE FROM news")
    fun clear()
}