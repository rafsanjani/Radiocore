package com.emperor95online.ashhfm.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;

import com.emperor95online.ashhfm.util.PrefManager;

import java.io.IOException;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static com.emperor95online.ashhfm.util.Constants.ACTION_PAUSE;
import static com.emperor95online.ashhfm.util.Constants.ACTION_PLAY;
import static com.emperor95online.ashhfm.util.Constants.LOADING;
import static com.emperor95online.ashhfm.util.Constants.MESSAGE;
import static com.emperor95online.ashhfm.util.Constants.PAUSED;
import static com.emperor95online.ashhfm.util.Constants.PLAYING;
import static com.emperor95online.ashhfm.util.Constants.RESULT;
import static com.emperor95online.ashhfm.util.Constants.STATUS_DESTROYED;
import static com.emperor95online.ashhfm.util.Constants.STATUS_PAUSED;

/***
 * Handle Audio playback
 */
public class AudioStreamingService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    LocalBroadcastManager broadcastManager;

    private final String audioStreamUrl = "http://stream.zenolive.com/urp3bkvway5tv.aac?15474";
    MediaPlayer mediaPlayer = null;

    private PrefManager prefManager;

    public AudioStreamingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        prefManager = new PrefManager(AudioStreamingService.this);
        broadcastManager = LocalBroadcastManager.getInstance(this);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(audioStreamUrl);
        } catch (IOException e) {

        }
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_PLAY)) {
            mediaPlayer.prepareAsync(); // prepare async to not block main thread
            prefManager.setStatus(LOADING);
            sendResult(AudioStreamingState.STATUS_LOADING);
        } else if (intent.getAction().equals(ACTION_PAUSE)) {
            mediaPlayer.stop();
            prefManager.setStatus(PAUSED);
            prefManager.setStatus(STATUS_PAUSED);
            sendResult(AudioStreamingState.STATUS_PAUSED);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            prefManager.setStatus(STATUS_DESTROYED);
        }
    }


    /**
     * Called when MediaPlayer is ready
     */
    public void onPrepared(MediaPlayer player) {
        player.start();
        if (player.isPlaying()) {
            sendResult(AudioStreamingState.STATUS_PLAYING);
            prefManager.setStatus(PLAYING);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // ... react appropriately ...
        // The MediaPlayer has moved to the Error state, must be reset!
        return true;
    }

    public void sendResult(AudioStreamingState message) {
        Intent intent = new Intent(RESULT);
        if (message != null)
            //Perform backwards convertion to AudioStreamingState in it's receivers
            intent.putExtra(MESSAGE, message.toString());

        broadcastManager.sendBroadcast(intent);
    }

    public enum AudioStreamingState {
        STATUS_PLAYING,
        STATUS_PAUSED,
        STATUS_LOADING,
        STATUS_DESTROYED
    }
}
