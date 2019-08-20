package com.foreverrafs.radiocore.util

// Created by Emperor95 on 11/19/2018.

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.util.Log

class RadioPreferences(context: Context) {
    ///////////////SETTINGS VARIABLES
    private val settings: SharedPreferences

    init {

        settings = PreferenceManager.getDefaultSharedPreferences(context)
    }

    /**
     * Checks if we are starting the application for the first time.
     *
     * @return
     */
    /**
     * Store a value which tells the context that the application is starting for the first time. This is necessary
     * for performing operations that are only necessary when the application is running for the first time
     *
     * @param isFirstTime
     */
    var isFirstTimeLaunch: Boolean
        get() = settings.getBoolean(IS_FIRST_TIME_LAUNCH, true)
        set(isFirstTime) = settings.edit().putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime).apply()

    /**
     * Checks whether the application's status has been set. Status is one of STATUS_PLAYING, STATUS_PAUSE or STATUS_STOPPED
     *
     * @return
     */
    val isStatusSet: Boolean
        get() = settings.contains(STREAMING_STATUS)

    /**
     * Get the application's playing state. This is either PLAYING, STOPPED or LOADING depending on what the
     * user is doing
     *
     * @return
     */
    /**
     * Set the application's playing status, this can either be loading, playing or stopped depending on
     * the user's actions or the network status
     *
     * @param status
     */
    var status: String?
        get() = settings.getString(STREAMING_STATUS, Constants.STATUS_PLAYING)
        set(status) = settings.edit().putString(STREAMING_STATUS, status).apply()

    /**
     * Checks whether user wants to start audio stream when app is launched without explicitly clicking on the play button
     *
     * @return a boolean (true/false) indicating whether user has decided to enable autoplay or not
     */
    /**
     * User can decide to start the audio stream immediately the app is launched or not
     * This will be set mostly from the settings screen automatically
     *
     * @param value True or false, indicating whether we want to auto start or not
     */
    var isAutoPlayOnStart: Boolean
        get() = settings.getBoolean(AUTOPLAY_ON_START, true)
        set(value) = settings.edit().putBoolean(AUTOPLAY_ON_START, value).apply()

    /**
     * Get the location of the cache file. This is only used internally by the app for storing cached
     * news items to facilitate their easy retrieval.
     *
     * @return
     */
    /**
     * Set's the name with which the cache file will be stored. This is usually the temporary
     * storage location of the app with json extension appended to it.
     *
     * @param fileName The name to be used in storing the cache
     */
    var cacheFileName: String?
        get() = settings.getString(CACHE_FILE_NAME, CACHE_NOT_FOUND)
        set(fileName) {
            settings.edit().putString(CACHE_FILE_NAME, fileName).apply()
            Log.i(TAG, "Cache path saved. Path: $fileName")
        }

    /**
     * Get the hours it takes for the news cache to expire.
     *
     * @return
     */
    val cacheExpiryHours: String?
        get() = settings.getString(CACHE_EXPIRY_HOURS, "5")

    val streamingTimer: String?
        get() = settings.getString(STREAMING_TIMER, "1")

    var cleanShutdown: Boolean
        get() = settings.getBoolean(CLEAN_SHUT_DOWN, false)
        set(value) = settings.edit().putBoolean(CLEAN_SHUT_DOWN, value).apply()


    /**
     * Removes the cache file. This usually happens when the cache has expired and needs
     * to be rebuilt anew
     */
    fun removeCacheFileEntry() {
        settings.edit().remove(CACHE_FILE_NAME).apply()
    }

    fun setCacheExpiryHours(hours: Int) {
        settings.edit().putInt(CACHE_EXPIRY_HOURS, hours).apply()
    }

    /**
     * Sets the number of hours or minutes a stream should play before automatically shutting
     * down. The default is set to 99 Hours
     */

    fun setStreamingTimer(hours: Int) {
        settings.edit().putInt(STREAMING_TIMER, hours).apply()
    }

    companion object {
        private val TAG = "RadioPreferences"
        val CACHE_NOT_FOUND = "com.foreverrafs.radiocore.not_found"
        private val AUTOPLAY_ON_START = "com.foreverrafs.radiocore.autoplay_on_start"
        private val IS_FIRST_TIME_LAUNCH = "com.foreverrafs.radiocore.is_first_time_launch"
        private val STREAMING_STATUS = "com.foreverrafs.radiocore.streaming_status"
        private val CACHE_FILE_NAME = "com.foreverrafs.radiocore.cache_file_name"
        private val CACHE_EXPIRY_HOURS = "com.foreverrafs.radiocore.cache_expiry_hours"
        private val STREAMING_TIMER = "com.foreverrafs.radiocore.streaming_timer"
        private val CLEAN_SHUT_DOWN = "com.foreverrafs.radiocore.clean_shut_down"
    }
}
