package com.radiocore.news.data.entities

// Created by Emperor95 on 1/13/2019.

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime

@Parcelize
@Entity(tableName = "news")
data class NewsEntity(
        @ColumnInfo(name = "headline") var headline: String,
        @ColumnInfo(name = "content") var content: String,
        @ColumnInfo(name = "date") var date: DateTime,
        @ColumnInfo(name = "category") var category: String,
        @ColumnInfo(name = "imageUrl") var imageUrl: String

) : Parcelable {
    //our primary key definition
    @PrimaryKey(autoGenerate = true)
    @IgnoredOnParcel
    @ColumnInfo(name = "id")
    var id: Int = 0
}


//YYYY-MM-DDThh:mm:ssTZD