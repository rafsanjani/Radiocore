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
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.foreverrafs.starfm.HomeActivity;
import com.foreverrafs.starfm.R;
import com.foreverrafs.starfm.StreamPlayer;
import com.foreverrafs.starfm.util.Constants;
import com.foreverrafs.starfm.util.Preference;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import static com.foreverrafs.starfm.util.Constants.ACTION_PLAY;
import static com.foreverrafs.starfm.util.Constants.ACTION_STOP;
import static com.foreverrafs.starfm.util.Constants.DEBUG_TAG;
import static com.foreverrafs.starfm.util.Constants.MESSAGE;
import static com.foreverrafs.starfm.util.Constants.RESULT;
import static com.foreverrafs.starfm.util.Constants.STATUS_PLAYING;
import static com.foreverrafs.starfm.util.Constants.STATUS_STOPPED;
import static com.foreverrafs.starfm.util.Constants.STREAM_URL;

/***
 * Handle Audio playback
 */
public class AudioStreamingService extends Service implements AudioManager.OnAudioFocusChangeListener {

    private final Object mFocusLock = new Object();
    LocalBroadcastManager broadcastManager;
    SimpleExoPlayer mediaPlayer;
    MediaSource streamSrc = null;
    private Preference preference;
    private String notificationText = "Empty"; //this will be set when context is created
    private NotificationCompat.Builder builder;
    private Notification streamNotification;
    private AudioManager audioManager;
    private boolean mResumeOnFocusGain;
    private boolean isAudioPlaying;

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

        try {
            mediaPlayer = StreamPlayer.getPlayer(this);

            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, getString(R.string.app_name)));
            streamSrc = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(STREAM_URL));

            mediaPlayer.prepare(streamSrc);
            mediaPlayer.setPlayWhenReady(true);

            mediaPlayer.addListener(new Player.EventListener() {
                @Override
                public void onLoadingChanged(boolean isLoading) {
                    isAudioPlaying = false;
                }

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    Log.i(DEBUG_TAG, "Player state changed to : " + playbackState);
                    if (playWhenReady && playbackState == Player.STATE_READY) {
                        // Active playback.
                        isAudioPlaying = true;
                        sendResult(AudioStreamingState.STATUS_PLAYING);
                        preference.setStatus(STATUS_PLAYING);
                    } else if (playWhenReady) {
                        // Not playing because playback ended, the player is buffering, stopped or
                        // failed. Check playbackState and player.getPlaybackError for details.
                        isAudioPlaying = false;
                        sendResult(AudioStreamingState.STATUS_STOPPED);
                        preference.setStatus(STATUS_STOPPED);
                        Log.i(DEBUG_TAG, "Error Buffering");

                    } else {
                        // Paused by app.
                        isAudioPlaying = false;
                        sendResult(AudioStreamingState.STATUS_STOPPED);
                        preference.setStatus(STATUS_STOPPED);
                    }

                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {
                    isAudioPlaying = false;
                    sendResult(AudioStreamingState.STATUS_STOPPED);
                    preference.setStatus(STATUS_STOPPED);
                    Log.e(DEBUG_TAG, error.getMessage());

                    if (error.getSourceException() instanceof HttpDataSource.HttpDataSourceException)
                        Toast.makeText(getApplicationContext(), "Network Error", Toast.LENGTH_LONG).show();
                }
            });

        } catch (Exception e) {
            Log.e(DEBUG_TAG, e.getMessage());
        }
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


        builder = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
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
        mediaPlayer.setPlayWhenReady(true);

        if (mediaPlayer.getPlaybackState() == Player.STATE_IDLE)
            mediaPlayer.prepare(streamSrc);
    }

    /**
     * Stop playback and set playback status in SharedPreferences
     */
    private void stopPlayback() {
        if (isAudioPlaying)
            mediaPlayer.setPlayWhenReady(false);

//        preference.setStatus(STATUS_STOPPED);
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
                    stopPlayback();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                synchronized (mFocusLock) {
                    mResumeOnFocusGain = isAudioPlaying;
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
