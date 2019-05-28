package com.foreverrafs.starfm.util;

// Created by Emperor95 on 11/19/2018.

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import static com.foreverrafs.starfm.util.Constants.PLAYING;

public class Preference {
    private static final String IS_FIRST_TIME_LAUNCH = "is_first_time_launch";
    private static final String STATUS = "streaming_status";
    private static final String LAST_NEWS_FETCHED_DATE = "last_news_fetched_date";
    ///////////////SETTINGS VARIABLES
    private final String AUTOPLAY = "autoplay_on_start";
    private SharedPreferences settings;

    ////////////////END OF SETTINGS VARIABLES


    //call to this constructor already returns a singletone so no need to define our class as one
    public Preference(Context context) {
        settings = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Checks if we are starting the application for the first time.
     *
     * @return
     */
    public boolean isFirstTimeLaunch() {
        return settings.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    /**
     * Store a value which tells the context that the application is starting for the first time
     *
     * @param isFirstTime
     */
    public void setFirstTimeLaunch(boolean isFirstTime) {
        settings.edit().putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime).apply();
    }

    /**
     * Checks whether the application's status has been set
     *
     * @return
     */
    public boolean isStatusSet() {
        return settings.contains(STATUS);
    }

    /**
     * Get the application's playing state. This is either PLAYING, STOPPED or LOADING depending on what the
     * user is doing
     *
     * @return
     */
    public String getStatus() {
        return settings.getString(STATUS, PLAYING);
    }

    /**
     * Set the application's playing status, this can either be loading, playing or stopped depending on
     * the user's actions or the network status
     *
     * @param status
     */
    public void setStatus(String status) {
        settings.edit().putString(STATUS, status).apply();
    }

    /**
     * Checks whether user wants to start audio stream when app is launched without explicitly clicking on the play button
     *
     * @return a boolean (true/false) indicating whether user has decided to enable autoplay or not
     */
    public boolean isAutoPlayOnStart() {
        return settings.getBoolean(AUTOPLAY, false);
    }

    /**
     * User can decide to start the audio stream immediately the app is launched or not
     * This will be set mostly from the settings screen automatically
     *
     * @param value True or false, indicating whether we want to auto start or not
     */
    public void setAutoPlayOnStart(boolean value) {
        settings.edit().putBoolean(Constants.AUTOPLAY_ON_START, value).apply();
    }


    /**
     * Get the last time news items were fetched
     */
    public String getLastNewsFetchedDate() {
        return settings.getString(LAST_NEWS_FETCHED_DATE, "May 27, 2019");
    }

    /**
     * Store the date and time of the last time news items were fetched. This will be used for news items
     * caching
     *
     * @param value
     */
    public void setLastNewsFetchedDate(String value) {
        settings.edit().putString(LAST_NEWS_FETCHED_DATE, value).apply();
    }
}
