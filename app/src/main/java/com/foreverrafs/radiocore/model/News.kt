package com.foreverrafs.radiocore.model

// Created by Emperor95 on 1/13/2019.

import android.os.Parcel
import android.os.Parcelable

import org.joda.time.DateTime

class News : Parcelable {
    var headline: String? = null
        private set
    var image: String? = null
        private set
    var content: String? = null
    var date: DateTime? = null
        private set
    var category: String? = null

    constructor(headline: String, date: DateTime?, image: String, content: String, category: String) {
        this.headline = headline
        this.date = date
        this.image = image
        this.content = content
        this.category = category
    }


    constructor(`in`: Parcel) {
        headline = `in`.readString()
        content = `in`.readString()
        image = `in`.readString()
        val dateStr = `in`.readString()
        category = `in`.readString()

        try {
            date = DateTime.parse(dateStr)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun describeContents(): Int {
        return hashCode()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(headline)
        dest.writeString(content)
        dest.writeString(image)
        dest.writeString(date!!.toString())
        dest.writeString(category)
    }


    companion object CREATOR : Parcelable.Creator<News> {
        override fun createFromParcel(parcel: Parcel): News {
            return News(parcel)
        }

        override fun newArray(size: Int): Array<News?> {
            return arrayOfNulls(size)
        }
    }
}
