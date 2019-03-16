package com.emperor95online.ashhfm.util;

// Created by Emperor95 on 3/5/2019.

public class Constants {
    public static final String DEBUG_TAG = "com.emperor95online";
    public static final String RESULT = "com.emperor95online.ashhfm.service.AudioStreamingService.REQUEST_PROCESSED";
    public static final String MESSAGE = "com.emperor95online.ashhfm.service.AudioStreamingService.AUDIO_STREAMING_STATUS";
    //public static final String STREAM_URL = "http://stream.zenolive.com/urp3bkvway5tv.aac?15474";
    public static final String STREAM_URL = "http://ashhfm.atunwadigital.streamguys1.com/ashhfm";

    public static final String ACTION_PLAY = "com.emperor95Online.ashhfm.PLAY";
    public static final String ACTION_STOP = "com.emperor95Online.ashhfm.PAUSE";

    //these values will be stored to persistent storage so their values are mapped to AudioStates
    //for backward conversion
    public static final String PLAYING = "STATUS_PLAYING";
    public static final String STOPPED = "STATUS_STOPPED";
    public static final String LOADING = "STATUS_LOADING";

    //PLAYER STATES
    public static final String STATUS_PLAYING = "STATUS_PLAYING";
    public static final String STATUS_STOPPED = "STATUS_STOPPED";
    public static final String STATUS_LOADING = "STATUS_LOADING";
    //public static final String STATUS_DESTROYED = "STATUS_DESTROYED";

    //Notification channel for Android 0
    public static final String NOTIFICATION_CHANNEL_ID = "com.emperor95Online.ashhfm";

    //SETTINGS
    public static final String AUTOPLAY_ON_START = "com.emperor95online.AUTOPLAY";
}
