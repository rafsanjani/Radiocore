package com.foreverrafs.radiocore.model;

// Created by Emperor95 on 1/13/2019.

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;

public class News implements Parcelable {
    public static final Creator<News> CREATOR = new Creator<News>() {
        @Override
        public News createFromParcel(Parcel in) {
            return new News(in);
        }

        @Override
        public News[] newArray(int size) {
            return new News[size];
        }
    };
    private String headline;
    private String image;
    private String content;
    private DateTime date;
    private String category;

    public News(String headline, DateTime date, String image, String content, String category) {
        this.headline = headline;
        this.date = date;
        this.image = image;
        this.content = content;
        this.category = category;
    }


    public News(Parcel in) {
        headline = in.readString();
        content = in.readString();
        image = in.readString();
        String dateStr = in.readString();
        category = in.readString();

        try {
            date = DateTime.parse(dateStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getHeadline() {
        return headline;
    }

    public DateTime getDate() {
        return date;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    public String getImage() {
        return image;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(headline);
        dest.writeString(content);
        dest.writeString(image);
        dest.writeString(date.toString());
        dest.writeString(category);
    }
}
