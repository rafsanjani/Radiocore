package com.radiocore.news.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.radiocore.news.data.entities.NewsEntity
import com.radiocore.news.util.NewsConverters


@Database(entities = [NewsEntity::class], version = 1, exportSchema = false)
@TypeConverters(NewsConverters::class)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun newsDao(): NewsDao
}