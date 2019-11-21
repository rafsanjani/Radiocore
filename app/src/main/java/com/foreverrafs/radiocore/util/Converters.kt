package com.foreverrafs.radiocore.util

import androidx.room.TypeConverter
import org.joda.time.DateTime

class Converters {
    @TypeConverter
    fun fromTimeStamp(value: Long): DateTime {
        return (DateTime(value))
    }

    @TypeConverter
    fun toTimeStamp(date: DateTime): Long {
        return date.millis
    }
}