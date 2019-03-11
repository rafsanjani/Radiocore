package com.emperor95online.ashhfm.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.widget.Toast;

import com.emperor95online.ashhfm.HomeActivity;
import com.emperor95online.ashhfm.R;
import com.emperor95online.ashhfm.util.Constants;
import com.emperor95online.ashhfm.util.PrefManager;

import java.io.IOException;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import static com.emperor95online.ashhfm.util.Constants.ACTION_PLAY;
import static com.emperor95online.ashhfm.util.Constants.ACTION_STOP;
import static com.emperor95online.ashhfm.util.Constants.LOADING;
import static com.emperor95online.ashhfm.util.Constants.MESSAGE;
import static com.emperor95online.ashhfm.util.Constants.PLAYING;
import static com.emperor95online.ashhfm.util.Constants.RESULT;
import static com.emperor95online.ashhfm.util.Constants.STATUS_STOPPED;
import static com.emperor95online.ashhfm.util.Constants.STOPPED;
import static com.emperor95online.ashhfm.util.Constants.STREAM_URL;

/***
 * Handle Audio playback
 */
public class AudioStreamingService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {

    LocalBroadcastManager broadcastManager;

    MediaPlayer mediaPlayer = null;

    private PrefManager prefManager;
    private final String notificationText = "Live Radio - 101.1FM";
    private NotificationCompat.Builder builder;
    private Notification streamNotification;

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

        streamNotification = createNotification();
        prefManager = new PrefManager(AudioStreamingService.this);
        broadcastManager = LocalBroadcastManager.getInstance(this);

        mediaPlayer = new MediaPlayer();


        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        try {
            mediaPlayer.setDataSource(STREAM_URL);
        } catch (IOException e) {

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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //System.out.println("Trying to start service again");
        createNotificationChannel();
        startForeground(1, streamNotification);


        if (intent.getAction().equals(ACTION_PLAY) && !mediaPlayer.isPlaying()) {
            //  System.out.println("Current State:" + prefManager.getStatus());

            // prepare async to not block main thread
            prefManager.setStatus(LOADING);
            sendResult(AudioStreamingState.STATUS_LOADING);

            mediaPlayer.prepareAsync();
        } else if (intent.getAction().equals(ACTION_STOP)) {
            // System.out.println("Original State with :" + prefManager.getStatus());
            mediaPlayer.stop();
            prefManager.setStatus(STOPPED);
            prefManager.setStatus(STATUS_STOPPED);
            sendResult(AudioStreamingState.STATUS_STOPPED);
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
            prefManager.setStatus(STATUS_STOPPED);
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
        Toast.makeText(this, "Error Loading Stream::::Are you Online?", Toast.LENGTH_LONG).show();
        sendResult(AudioStreamingState.STATUS_STOPPED);
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
        STATUS_STOPPED,
        STATUS_LOADING
    }
}
