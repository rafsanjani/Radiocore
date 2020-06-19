package com.radiocore.news.model

// Created by Emperor95 on 1/13/2019.

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class News(
        val id: String,
        val headline: String,
        val content: String,
        val date: String,
        val category: String,
        val imageUrl: String) : Parcelable