package com.foreverrafs.starfm;

import android.media.MediaPlayer;

public class StreamPlayer {
    private static MediaPlayer mediaPlayer;

    public static MediaPlayer getPlayer() {
        if (mediaPlayer == null)
            mediaPlayer = new MediaPlayer();

        return mediaPlayer;
    }
}
