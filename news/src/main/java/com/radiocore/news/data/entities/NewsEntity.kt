package com.radiocore.news.data.entities

// Created by Emperor95 on 1/13/2019.

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "news")
data class NewsEntity(
        @SerializedName("_id")
        @ColumnInfo(name = "id")
        @PrimaryKey(autoGenerate = false) val id: String,
        @ColumnInfo(name = "headline") val headline: String,
        @ColumnInfo(name = "content") val content: String,
        @ColumnInfo(name = "date") val date: String,
        @ColumnInfo(name = "category") val category: String,
        @ColumnInfo(name = "imageUrl") val imageUrl: String

) : Parcelable