package com.foreverrafs.starfm.util;

// Created by Emperor95 on 3/5/2019.

public class Constants {
    public static final String DEBUG_TAG = "com.starfm";

    public static final String RESULT = "com.emperor95online.ashhfm.service.AudioStreamingService.REQUEST_PROCESSED";
    public static final String MESSAGE = "com.emperor95online.ashhfm.service.AudioStreamingService.AUDIO_STREAMING_STATUS";
    //    public static final String STREAM_URL = "http://node-21.zeno.fm/sm3w0cp642quv?rj-ttl=5&rj-token=AAABauNwQ3G4lfvWvkyhRuTJ0_mgLB2oK-fWV41JG42CMMK1VQPH8Q";
    public static final String STREAM_URL = "http://media-ice.musicradio.com/CapitalGlasgowMP3";
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
    public static final String STATUS_DESTROYED = "STATUS_DESTROYED";


    //APPLICATION SETTINGS
    public static final String AUTOPLAY = "com.foreverrafs.starFM.autoplay";

    //Notification channel for Android 0
    public static final String NOTIFICATION_CHANNEL_ID = "com.emperor95Online.ashhfm";

    //SETTINGS
    static final String AUTOPLAY_ON_START = "com.emperor95online.AUTOPLAY";
}
