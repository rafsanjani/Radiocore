package com.foreverrafs.starfm.util;

// Created by Emperor95 on 11/19/2018.

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import static com.foreverrafs.starfm.util.Constants.DEBUG_TAG;
import static com.foreverrafs.starfm.util.Constants.STATUS_PLAYING;

public class RadioPreferences {
    private static final String AUTOPLAY_ON_START = "com.foreverrafs.radiocore.autoplay_on_start";
    private static final String IS_FIRST_TIME_LAUNCH = "com.foreverrafs.radiocore.is_first_time_launch";
    private static final String STREAMING_STATUS = "com.foreverrafs.radiocore.streaming_status";
    private static final String CACHE_FILE_NAME = "com.foreverrafs.radiocore.cache_file_name";
    private static final String CACHE_EXPIRY_HOURS = "com.foreverrafs.radiocore.cache_expiry_hours";
    private static final String STREAMING_TIMER = "com.foreverrafs.radiocore.streaming_timer";

    ///////////////SETTINGS VARIABLES
    private SharedPreferences settings;
    private Context context;

    ////////////////END OF SETTINGS VARIABLES


    public RadioPreferences(Context context) {
        this.context = context;
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
     * Store a value which tells the context that the application is starting for the first time. This is necessary
     * for performing operations that are only necessary when the application is running for the first time
     *
     * @param isFirstTime
     */
    public void setFirstTimeLaunch(boolean isFirstTime) {
        settings.edit().putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime).apply();
    }

    /**
     * Checks whether the application's status has been set. Status is one of STATUS_PLAYING, STATUS_PAUSE or STATUS_STOPPED
     *
     * @return
     */
    public boolean isStatusSet() {
        return settings.contains(STREAMING_STATUS);
    }

    /**
     * Get the application's playing state. This is either PLAYING, STOPPED or LOADING depending on what the
     * user is doing
     *
     * @return
     */
    public String getStatus() {
        return settings.getString(STREAMING_STATUS, STATUS_PLAYING);
    }

    /**
     * Set the application's playing status, this can either be loading, playing or stopped depending on
     * the user's actions or the network status
     *
     * @param status
     */
    public void setStatus(String status) {
        settings.edit().putString(STREAMING_STATUS, status).apply();
    }

    /**
     * Checks whether user wants to start audio stream when app is launched without explicitly clicking on the play button
     *
     * @return a boolean (true/false) indicating whether user has decided to enable autoplay or not
     */
    public boolean isAutoPlayOnStart() {
        return settings.getBoolean(AUTOPLAY_ON_START, true);
    }

    /**
     * User can decide to start the audio stream immediately the app is launched or not
     * This will be set mostly from the settings screen automatically
     *
     * @param value True or false, indicating whether we want to auto start or not
     */
    public void setAutoPlayOnStart(boolean value) {
        settings.edit().putBoolean(AUTOPLAY_ON_START, value).apply();
    }


    /**
     * Removes the cache file. This usually happens when the cache has expired and needs
     * to be rebuilt anew
     */
    public void removeCacheFileEntry() {
        settings.edit().remove(CACHE_FILE_NAME).apply();
    }

    /**
     * Get the location of the cache file. This is only used internally by the app for storing cached
     * news items to facilitate their easy retrieval.
     *
     * @return
     */
    public String getCacheFileName() {
        return settings.getString(CACHE_FILE_NAME, null);
    }

    /**
     * Set's the name with which the cache file will be stored. This is usually the temporary
     * storage location of the app with json extension appended to it.
     *
     * @param fileName The name to be used in storing the cache
     */
    public void setCacheFileName(String fileName) {
        settings.edit().putString(CACHE_FILE_NAME, fileName).apply();
        Log.i(DEBUG_TAG, "Cache path saved. Path: " + fileName);
    }

    /**
     * Get the hours it takes for the news cache to expire.
     *
     * @return
     */
    public int getCacheExpiryHours() {
        return settings.getInt(CACHE_EXPIRY_HOURS, 5);
    }

    public void setCacheExpiryHours(int hours) {
        settings.edit().putInt(CACHE_EXPIRY_HOURS, hours).apply();
    }

    public String getStreamingTimer() {
        return settings.getString(STREAMING_TIMER, "1");
    }

    /**
     * Sets the number of hours or minutes a stream should play before automatically shutting
     * down. The default is set to 99 Hours
     */

    public void setStreamingTimer(int hours) {
        settings.edit().putInt(STREAMING_TIMER, hours).apply();
    }
}
