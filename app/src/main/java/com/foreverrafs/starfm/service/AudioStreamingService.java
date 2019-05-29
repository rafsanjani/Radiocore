package com.foreverrafs.starfm.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.foreverrafs.starfm.HomeActivity;
import com.foreverrafs.starfm.R;
import com.foreverrafs.starfm.StreamPlayer;
import com.foreverrafs.starfm.util.Constants;
import com.foreverrafs.starfm.util.Preference;

import java.io.IOException;

import static com.foreverrafs.starfm.util.Constants.ACTION_PLAY;
import static com.foreverrafs.starfm.util.Constants.ACTION_STOP;
import static com.foreverrafs.starfm.util.Constants.DEBUG_TAG;
import static com.foreverrafs.starfm.util.Constants.LOADING;
import static com.foreverrafs.starfm.util.Constants.MESSAGE;
import static com.foreverrafs.starfm.util.Constants.PLAYING;
import static com.foreverrafs.starfm.util.Constants.RESULT;
import static com.foreverrafs.starfm.util.Constants.STATUS_STOPPED;
import static com.foreverrafs.starfm.util.Constants.STOPPED;
import static com.foreverrafs.starfm.util.Constants.STREAM_URL;

/***
 * Handle Audio playback
 */
public class AudioStreamingService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        AudioManager.OnAudioFocusChangeListener {

    LocalBroadcastManager broadcastManager;

    MediaPlayer mediaPlayer = StreamPlayer.getPlayer();

    private Preference preference;
    private String notificationText = "Empty"; //this will be set when context is created
    private NotificationCompat.Builder builder;
    private Notification streamNotification;

    private final Object mFocusLock = new Object();
    private AudioManager audioManager;
    private boolean mResumeOnFocusGain;

    public AudioStreamingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onCreate() {
        super.onCreate();

        notificationText = this.getString(R.string.live_radio_freq);

        streamNotification = createNotification();
        preference = new Preference(AudioStreamingService.this);
        broadcastManager = LocalBroadcastManager.getInstance(this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        synchronized (mFocusLock) {
            int audioFocusReqCode = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (audioFocusReqCode == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                Log.i(DEBUG_TAG, "Couldn't gain audio focus:::::Exiting");
                return;
            }
        }


        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        try {
            mediaPlayer.setDataSource(STREAM_URL);
        } catch (IOException e) {
            Log.e(DEBUG_TAG, e.getMessage());
        }
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);
    }

    /**
     * Create notification channel on Android O+
     *
     * @return
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_ID,
                    "Music Streaming Channel",
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notificationChannel);
        }
    }

    /**
     * Create notification using the channel created by createNotificationChannel
     * and also instantiate the field variable (builder)
     *
     * @return a Notification object which c
     */
    private Notification createNotification() {
        Intent contentIntent = new Intent(this, HomeActivity.class);
        Intent pauseIntent = new Intent(this, AudioStreamingService.class);
        pauseIntent.setAction(Constants.ACTION_STOP);

        PendingIntent p_contentIntent = PendingIntent.getActivity(this, 0,
                contentIntent, 0);
        PendingIntent p_pauseIntent = PendingIntent.getService(this, 0, pauseIntent, 0);


        builder = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Online Radio")
                .addAction(R.drawable.ic_pause_notification, "Pause", p_pauseIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0))
                .setContentText(notificationText)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(p_contentIntent);

        return builder.build();
    }

    /**
     * Start playback and set playback status in SharedPreferences.
     */
    private void startPlayback() {
        preference.setStatus(LOADING);
        sendResult(AudioStreamingState.STATUS_LOADING);
        mediaPlayer.prepareAsync();
    }

    /**
     * Stop playback and set playback status in SharedPreferences
     */
    private void stopPlayback() {
        mediaPlayer.stop();
        preference.setStatus(STOPPED);
        sendResult(AudioStreamingState.STATUS_STOPPED);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();

        if (intent.getAction().equals(ACTION_PLAY) && !mediaPlayer.isPlaying()) {
            startPlayback();

            //start foreground audio service right away instead of waiting for onPrepared to complete
            // to beat android 0 5sec limit
            startForeground(1, streamNotification);
        } else if (intent.getAction().equals(ACTION_STOP)) {
            stopPlayback();

            //stop the foreground audio service and take away the notification from the user's screen
            stopForeground(true);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
            preference.setStatus(STATUS_STOPPED);
            audioManager.abandonAudioFocus(this);
        }
    }


    /**
     * Called when MediaPlayer is ready
     */
    public void onPrepared(MediaPlayer player) {
        player.start();
        if (player.isPlaying()) {

            sendResult(AudioStreamingState.STATUS_PLAYING);
            preference.setStatus(PLAYING);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Toast.makeText(this, "Error Loading Stream::::Are you Online?", Toast.LENGTH_LONG).show();
        Log.e(DEBUG_TAG, "Error Loading Stream::::Possibly no network on target device");
        sendResult(AudioStreamingState.STATUS_STOPPED);

        stopForeground(true);
        stopSelf();
        stopPlayback();
        return true;
    }

    /**
     * Send a result back to the Broadcast receiver of the calling activity, in this case (HomeActivity.java)
     *
     * @param message
     */
    public void sendResult(AudioStreamingState message) {
        Intent intent = new Intent(RESULT);
        if (message != null)
            //Perform backwards convertion to AudioStreamingState in it's receivers
            intent.putExtra(MESSAGE, message.toString());

        broadcastManager.sendBroadcast(intent);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (mResumeOnFocusGain) {
                    synchronized (mFocusLock) {
                        mResumeOnFocusGain = false;
                        startPlayback();
                    }
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                synchronized (mFocusLock) {
                    mResumeOnFocusGain = false;
                    stopPlayback();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                synchronized (mFocusLock) {
                    mResumeOnFocusGain = mediaPlayer.isPlaying();
                }
                stopPlayback();
                break;
        }
    }

    /**
     * Discrete states of the Audio Streaming Service
     */
    public enum AudioStreamingState {
        STATUS_PLAYING,
        STATUS_STOPPED,
        STATUS_LOADING
    }
}
