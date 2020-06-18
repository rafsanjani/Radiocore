package com.radiocore.news.model

// Created by Emperor95 on 1/13/2019.

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.joda.time.DateTime

//@Parcelize
//@Entity(tableName = "news")
//data class News(
//        @ColumnInfo(name = "headline") var headline: String,
//        @ColumnInfo(name = "content") var content: String,
//        @ColumnInfo(name = "date") var date: DateTime,
//        @ColumnInfo(name = "category") var category: String,
//        @ColumnInfo(name = "imageUrl") var imageUrl: String
//
//) : Parcelable {
//        //our primary key definition
//        @PrimaryKey(autoGenerate = true)
//        @IgnoredOnParcel
//        @ColumnInfo(name = "id")
//        var id: Int = 0
//}
@Parcelize
data class News(
        val id: Int = 0,
        val headline: String,
        val content: String,
        val date: DateTime,
        val category: String,
        val imageUrl: String) : Parcelable