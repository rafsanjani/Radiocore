package com.emperor95online.ashhfm.pojo;

// Created by Emperor95 on 1/13/2019.

public class NewsObject {
    private String headline;
    private String date;
    private String image;

    public NewsObject() {
    }

    public NewsObject(String headline, String date, String image) {
        this.headline = headline;
        this.date = date;
        this.image = image;
    }

    public String getHeadline() {
        return headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
