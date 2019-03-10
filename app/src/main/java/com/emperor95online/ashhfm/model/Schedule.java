package com.emperor95online.ashhfm.model;

// Created by Emperor95 on 1/13/2019.

public class Schedule {
    private String headline;
    private String date;
    private String remainingTime;

    public Schedule() {
    }

    public Schedule(String headline, String date, String remainingTime) {
        this.headline = headline;
        this.date = date;
        this.remainingTime = remainingTime;
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

    public String getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(String image) {
        this.remainingTime = remainingTime;
    }
}
