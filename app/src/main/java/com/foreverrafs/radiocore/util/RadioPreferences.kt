package com.foreverrafs.radiocore.util

// Created by Emperor95 on 11/19/2018.

import android.content.Context
import android.content.SharedPreferences
import org.joda.time.DateTime

private const val TAG = "RadioPreferences"
class RadioPreferences(context: Context) {
    private val settings: SharedPreferences = context.getSharedPreferences("RadioCore", Context.MODE_PRIVATE)

    /**
     * Checks if we are starting the application for the first time.
     *
     * @return
     */
    /**
     * Store a value which tells the context that the application is starting for the first time. This is necessary
     * for performing operations that are only necessary when the application is running for the first time
     *
     */
    var firstTimeLaunch: Boolean
        get() = settings.getBoolean(IS_FIRST_TIME_LAUNCH, true)
        set(isFirstTime) = settings.edit().putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime).apply()


    /**
     * Checks whether user wants to start audio stream when app is launched without explicitly clicking on the play button
     *
     * @return a boolean (true/false) indicating whether user has decided to enable autoplay or not
     */
    /**
     * User can decide to start the audio stream immediately the app is launched or not
     * This will be set mostly from the settings screen automatically
     *
     */
    val isAutoPlayOnStart: Boolean
        get() = settings.getBoolean(AUTOPLAY_ON_START, true)

    /**
     * Get the location of the cache file. This is only used internally by the app for storing cached
     * news items to facilitate their easy retrieval.
     *
     * @return
     */


    /**
     * Gets and sets the time when the last news cache was saved.
     */
    var cacheStorageTime: DateTime?
        get() {
            //time will be null when there is no cache. we return a date in the past which will force us to fetch new content from online
            val time: String? = settings.getString(CACHE_STORAGE_TIME, null)
                    ?: return DateTime.parse("2019-01-01")

            return DateTime.parse(time)
        }
        set(storageTime) {
            settings.edit().putString(CACHE_STORAGE_TIME, storageTime.toString()).apply()
        }

    /**
     * Get the hours it takes for the news cache to expire.
     *
     * @return
     */
    val cacheExpiryHours: String?
        get() = settings.getString(CACHE_EXPIRY_HOURS, "5")

    /**
     * gets the number of hours or minutes a stream should play before automatically shutting
     * down. The default is set to 99 Hours
     */
    val streamingTimer: String?
        get() = settings.getString(STREAMING_TIMER, "1")

    var cleanShutdown: Boolean
        get() = settings.getBoolean(CLEAN_SHUT_DOWN, false)
        set(value) = settings.edit().putBoolean(CLEAN_SHUT_DOWN, value).apply()


    companion object {
        const val CACHE_STORAGE_TIME = "com.foreverrafs.radiocore.cache_storage_time"
        private const val AUTOPLAY_ON_START = "com.foreverrafs.radiocore.autoplay_on_start"
        private const val IS_FIRST_TIME_LAUNCH = "com.foreverrafs.radiocore.is_first_time_launch"
        private const val CACHE_EXPIRY_HOURS = "com.foreverrafs.radiocore.cache_expiry_hours"
        private const val STREAMING_TIMER = "com.foreverrafs.radiocore.streaming_timer"
        private const val CLEAN_SHUT_DOWN = "com.foreverrafs.radiocore.clean_shut_down"
    }
}
