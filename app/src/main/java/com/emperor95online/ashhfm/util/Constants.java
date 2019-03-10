package com.emperor95online.ashhfm.util;

// Created by Emperor95 on 3/5/2019.

public class Constants {
    public static final String DEBUG_TAG = "com.emperor95online";
    public static final String RESULT = "com.emperor95online.ashhfm.service.AudioStreamingService.REQUEST_PROCESSED";
    public static final String MESSAGE = "com.emperor95online.ashhfm.service.AudioStreamingService.AUDIO_STREAMING_STATUS";

    public static final String ACTION_PLAY = "com.emperor95Online.ashhfm.PLAY";
    public static final String ACTION_PAUSE = "com.emperor95Online.ashhfm.PAUSE";

    //these values will be stored to persistent storage so their values are mapped to AudioStates
    //for backward conversion
    public static final String PLAYING = "STATUS_PLAYING";
    public static final String PAUSED = "STATUS_PAUSED";
    public static final String LOADING = "STATUS_LOADING";

    //PLAYER STATES
    public static final String STATUS_PLAYING = "STATUS_PLAYING";
    public static final String STATUS_PAUSED = "STATUS_PAUSED";
    public static final String STATUS_LOADING = "STATUS_LOADING";
    public static final String STATUS_DESTROYED = "STATUS_DESTROYED";
}
