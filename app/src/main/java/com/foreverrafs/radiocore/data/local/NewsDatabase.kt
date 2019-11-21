package com.foreverrafs.radiocore.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.foreverrafs.radiocore.model.News
import com.foreverrafs.radiocore.util.Converters

@Database(entities = [News::class], version = 1)
@TypeConverters(Converters::class)
abstract class NewsDatabase : RoomDatabase() {
    abstract fun newsDao(): NewsDao

    companion object {
        private var instance: NewsDatabase? = null

        @Synchronized
        fun getInstance(context: Context): NewsDatabase {
            if (instance == null) {
                instance = Room.databaseBuilder(context, NewsDatabase::class.java, "RadioCoreDB")
                        .fallbackToDestructiveMigration().build()
            }

            return instance as NewsDatabase
        }
    }
}