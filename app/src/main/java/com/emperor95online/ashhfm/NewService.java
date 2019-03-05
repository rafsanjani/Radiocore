package com.emperor95online.ashhfm;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import java.io.IOException;

import static com.emperor95online.ashhfm.Constants.ACTION_PAUSE;
import static com.emperor95online.ashhfm.Constants.ACTION_PLAY;
import static com.emperor95online.ashhfm.Constants.LOADING;
import static com.emperor95online.ashhfm.Constants.MESSAGE;
import static com.emperor95online.ashhfm.Constants.PAUSED;
import static com.emperor95online.ashhfm.Constants.PLAYING;
import static com.emperor95online.ashhfm.Constants.RESULT;
import static com.emperor95online.ashhfm.Constants.STATUS_DESTROYED;
import static com.emperor95online.ashhfm.Constants.STATUS_LOADING;
import static com.emperor95online.ashhfm.Constants.STATUS_PAUSED;
import static com.emperor95online.ashhfm.Constants.STATUS_PLAYING;

public class NewService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    LocalBroadcastManager broadcastManager;

    private final String audioStreamUrl = "http://stream.zenolive.com/urp3bkvway5tv.aac?15474";
    MediaPlayer mediaPlayer = null;

    private PrefManager prefManager;

    public NewService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        prefManager = new PrefManager(NewService.this);
        broadcastManager = LocalBroadcastManager.getInstance(this);

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try{
            mediaPlayer.setDataSource(audioStreamUrl);
        }catch (IOException e){

        }
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getAction().equals(ACTION_PLAY)) {
            mediaPlayer.prepareAsync(); // prepare async to not block main thread
            prefManager.setStatus(STATUS_LOADING);
            sendResult(LOADING);
        } else if (intent.getAction().equals(ACTION_PAUSE)) {
            mediaPlayer.stop();
            prefManager.setStatus(STATUS_PAUSED);
            sendResult(PAUSED);
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null){
            mediaPlayer.release();
            mediaPlayer = null;

            prefManager.setStatus(STATUS_DESTROYED);
        }
    }


    /** Called when MediaPlayer is ready */
    public void onPrepared(MediaPlayer player) {
        player.start();
        if (player.isPlaying()) {
            sendResult(PLAYING);
            prefManager.setStatus(STATUS_PLAYING);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        // ... react appropriately ...
        // The MediaPlayer has moved to the Error state, must be reset!
        return true;
    }

    public void sendResult(String message) {
        Intent intent = new Intent(RESULT);
        if(message != null)
            intent.putExtra(MESSAGE, message);

        broadcastManager.sendBroadcast(intent);
    }
}
