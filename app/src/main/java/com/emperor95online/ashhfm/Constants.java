package com.emperor95online.ashhfm;

// Created by Emperor95 on 3/5/2019.

public class Constants {
    static final String DEBUG_TAG = "com.emperor95online";
    static final String RESULT = "com.emperor95online.ashhfm.AudioStreamingService.REQUEST_PROCESSED";
    static final String MESSAGE = "com.emperor95online.ashhfm.AudioStreamingService.AUDIO_STREAMING_STATUS";

    static final String ACTION_PLAY = "com.emperor95Online.ashhfm.PLAY";
    static final String ACTION_PAUSE = "com.emperor95Online.ashhfm.PAUSE";

    //these values will be stored to persistent storage so their values are mapped to AudioStates
    //for backward conversion
    static final String PLAYING = "STATUS_PLAYING";
    static final String PAUSED = "STATUS_PAUSED";
    static final String LOADING = "STATUS_LOADING";

    //PLAYER STATES
    static final String STATUS_PLAYING = "STATUS_PLAYING";
    static final String STATUS_PAUSED = "STATUS_PAUSED";
    static final String STATUS_LOADING = "STATUS_LOADING";
    static final String STATUS_DESTROYED = "STATUS_DESTROYED";
}
