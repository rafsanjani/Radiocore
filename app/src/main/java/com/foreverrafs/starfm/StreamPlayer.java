package com.foreverrafs.starfm;

import android.content.Context;

import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;

public class StreamPlayer {
    private static SimpleExoPlayer mediaPlayer;

    public static SimpleExoPlayer getPlayer(Context context) {
        if (mediaPlayer == null)
            mediaPlayer = ExoPlayerFactory.newSimpleInstance(context);

        return mediaPlayer;
    }
}
