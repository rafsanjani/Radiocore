package com.emperor95online.ashhfm.fragment;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.emperor95online.ashhfm.R;

import java.io.IOException;


// Created by Emperor95 on 1/13/2019.

public class Home extends Fragment implements View.OnClickListener {

    MediaPlayer mediaPlayer;
    final Uri audioStreamUri = Uri.parse("http://stream.zenolive.com/urp3bkvway5tv.aac?15474");

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        view.findViewById(R.id.button_play).setOnClickListener(this);
        view.findViewById(R.id.button_pause).setOnClickListener(this);
        mediaPlayer = new MediaPlayer();

        try {
            mediaPlayer.setDataSource(getContext(), audioStreamUri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_play:
                //lets begin an audio stream here
                //TODO: replace with Google's exoplayer library for better control and performance


                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {

                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            case R.id.button_pause:
                mediaPlayer.stop();
                break;
        }
    }
}
