package com.emperor95online.ashhfm.pojo;

// Created by Emperor95 on 1/13/2019.

public class News {
    private String headline;
    private String date;
    private String image;
    private String content;

    public News() {
    }

    public News(String headline, String date, String image) {
        this.headline = headline;
        this.date = date;
        this.image = image;
    }

    public News(String headline, String date, String image, String content) {
        this.headline = headline;
        this.date = date;
        this.image = image;
        this.content = content;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
