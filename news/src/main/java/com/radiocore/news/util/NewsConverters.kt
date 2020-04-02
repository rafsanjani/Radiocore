package com.radiocore.news.util

import androidx.room.TypeConverter
import org.joda.time.DateTime


class NewsConverters {
    @TypeConverter
    fun fromTimeStamp(value: Long): DateTime {
        return (DateTime(value))
    }

    @TypeConverter
    fun toTimeStamp(date: DateTime): Long {
        return date.millis
    }
}