package com.foreverrafs.starfm.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.foreverrafs.starfm.R;
import com.foreverrafs.starfm.StreamPlayer;
import com.foreverrafs.starfm.activity.HomeActivity;
import com.foreverrafs.starfm.util.Constants;
import com.foreverrafs.starfm.util.RadioPreferences;

import static com.foreverrafs.starfm.util.Constants.ACTION_PAUSE;
import static com.foreverrafs.starfm.util.Constants.ACTION_PLAY;
import static com.foreverrafs.starfm.util.Constants.ACTION_STOP;
import static com.foreverrafs.starfm.util.Constants.DEBUG_TAG;
import static com.foreverrafs.starfm.util.Constants.MESSAGE;
import static com.foreverrafs.starfm.util.Constants.RESULT;
import static com.foreverrafs.starfm.util.Constants.STATUS_LOADING;
import static com.foreverrafs.starfm.util.Constants.STATUS_PLAYING;
import static com.foreverrafs.starfm.util.Constants.STATUS_STOPPED;
import static com.foreverrafs.starfm.util.Constants.STREAM_URL;

/***
 * Handle Audio playback
 */
public class AudioStreamingService extends Service implements AudioManager.OnAudioFocusChangeListener {

    private final Object mFocusLock = new Object();
    private LocalBroadcastManager broadcastManager;
    private StreamPlayer mediaPlayer;
    private RadioPreferences radioPreferences;
    private String notificationText = "Empty"; //this will be set when context is created
    private Notification streamNotification;
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
        radioPreferences = new RadioPreferences(AudioStreamingService.this);
        broadcastManager = LocalBroadcastManager.getInstance(this);

        if (!getAudioFocus()) {
            Log.i(DEBUG_TAG, "Couldn't gain audio focus:::::Exiting");
            return;
        }


        try {
            mediaPlayer = StreamPlayer.getInstance(this);
            mediaPlayer.setStreamSource(Uri.parse(STREAM_URL));

            mediaPlayer.setPlayerStateChangesListener(new StreamPlayer.StreamStateChangesListener() {
                @Override
                public void onPlay() {
                    sendResult(AudioStreamingState.STATUS_PLAYING);
                    radioPreferences.setStatus(STATUS_PLAYING);
                    startForeground(5, streamNotification);
                }

                @Override
                public void onBuffering() {
                    sendResult(AudioStreamingState.STATUS_LOADING);
                    radioPreferences.setStatus(STATUS_LOADING);
                }

                @Override
                public void onStop() {
                    sendResult(AudioStreamingState.STATUS_STOPPED);
                    radioPreferences.setStatus(STATUS_STOPPED);
                    stopForeground(true);
                }

                @Override
                public void onPause() {
                    sendResult(AudioStreamingState.STATUS_STOPPED);
                    radioPreferences.setStatus(STATUS_STOPPED);
                    stopForeground(true);
                }

                @Override
                public void onError(Exception exception) {
                    sendResult(AudioStreamingState.STATUS_STOPPED);
                    radioPreferences.setStatus(STATUS_STOPPED);
                    stopForeground(true);
                }
            });

        } catch (Exception e) {
            Log.e(DEBUG_TAG, e.getMessage());
        }
    }

    private boolean getAudioFocus() {
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        synchronized (mFocusLock) {
            int audioFocusReqCode = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            if (audioFocusReqCode == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                return false;
            }
        }

        return true;
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

        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 5,
                contentIntent, 0);


        PendingIntent pausePendingIntent = PendingIntent.getService(this, 0, pauseIntent, 0);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Online Radio")
                .addAction(R.drawable.ic_pause_notification, "Pause", pausePendingIntent)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0))
                .setContentText(notificationText)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(contentPendingIntent);

        return builder.build();
    }

    /**
     * Start playback and set playback status in SharedPreferences.
     */
    private void startPlayback() {
        mediaPlayer.play();
    }

    /**
     * Stop playback and set playback status in SharedPreferences
     */
    private void stopPlayback() {
        mediaPlayer.stop();
    }

    private void pausePlayback() {
        mediaPlayer.pause();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        createNotificationChannel();

        //start foreground audio service right away instead of waiting for onPrepared to complete
        // to beat android 0 5sec limit
        startForeground(5, streamNotification);


        if (intent.getAction().equals(ACTION_PLAY)) {
            startPlayback();

        } else if (intent.getAction().equals(ACTION_STOP)) {
            stopPlayback();

            //stop the foreground audio service and take away the notification from the user's screen
            // stopForeground(true);
        } else if (intent.getAction().equals(ACTION_PAUSE)) {
            pausePlayback();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            radioPreferences.setStatus(STATUS_STOPPED);
            audioManager.abandonAudioFocus(this);
            StreamPlayer.getInstance(this).release();
        }

        Log.i(DEBUG_TAG, "Service is destroyed");
    }

    /**
     * Send a result back to the Broadcast receiver of the calling activity, in this case (HomeActivity.java)
     * The result is basically the state of the stream audio and is usually one of STATUS_LOADING, STATUS_STOPPED or STATUS_PLAYING
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

    /***
     * Called when a different application interrupts the audio on the target device. This can
     * be triggered by the phone ringing as a result of an incoming call or the user opening and accessing
     * an app which produces a sound such as a media player or a game
     * @param focusChange
     */
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
                    pausePlayback();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                synchronized (mFocusLock) {
                    mResumeOnFocusGain = StreamPlayer.getInstance(this).isPlaying();
                }
                pausePlayback();
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
