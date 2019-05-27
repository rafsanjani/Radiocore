package com.foreverrafs.starfm.util;

// Created by Emperor95 on 11/19/2018.

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preference {
    ///////////////SETTINGS VARIABLES
    private final String AUTOPLAY = "autoplay";
    private SharedPreferences settings;
    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String STATUS = "status_set";

    ////////////////END OF SETTINGS VARIABLES


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
     * @return
     */
    public boolean isStatusSet() {
        return settings.contains(STATUS);
    }

    /**
     * Get the application's playing state. This is either PLAYING, STOPPED or LOADING depending on what the
     * user is doing
     * @return
     */
    public String getStatus() {
        return settings.getString(STATUS, "");
    }

    /**
     * Set the application's playing status, this can either be loading, playing or stopped depending on
     * the user's actions or the network status
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
}