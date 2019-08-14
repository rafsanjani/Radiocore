package com.foreverrafs.radiocore;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.foreverrafs.radiocore.util.RadioPreferences;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.joda.time.Period;
import org.joda.time.Seconds;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;

import static com.foreverrafs.radiocore.util.Constants.DEBUG_TAG;
import static com.google.android.exoplayer2.Player.STATE_BUFFERING;
import static com.google.android.exoplayer2.Player.STATE_ENDED;
import static com.google.android.exoplayer2.Player.STATE_IDLE;
import static com.google.android.exoplayer2.Player.STATE_READY;

public class StreamPlayer implements Player.EventListener {
    private static final String TAG = DEBUG_TAG;
    private static WeakReference<StreamPlayer> instance;
    private MediaSource mediaSource;
    private SimpleExoPlayer exoPlayer;
    private boolean isPlaying;
    private StreamStateChangesListener listener;
    private PlaybackState playbackState = PlaybackState.IDLE;
    private WeakReference<Context> context;
    private RadioPreferences mPreferences;

    private StreamPlayer(Context context) {
        this.context = new WeakReference<>(context);
        exoPlayer = ExoPlayerFactory.newSimpleInstance(context);
        mPreferences = new RadioPreferences(context);
        exoPlayer.addListener(this);
    }

    public static StreamPlayer getInstance(Context context) {
        synchronized (StreamPlayer.class) {
            if (instance == null) {
                instance = new WeakReference<>(new StreamPlayer(context));
                Log.d(TAG, "getInstance: Media Instance Created on  " + Thread.currentThread().getName());
            }
            return instance.get();
        }
    }

    /**
     * Sets the Uri of the audio stream source.
     *
     * @param uri
     */
    public void setStreamSource(Uri uri) {
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context.get(), Util.getUserAgent(context.get(), "RadioCore"));
        mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri);
    }

    /**
     * Attaches a listener to the media object which propagates media events back to the context class.
     *
     * @param listener StreamStateChangesListener which will be used to send events back to the context class
     */
    public void setPlayerStateChangesListener(StreamStateChangesListener listener) {
        this.listener = listener;
    }

    /**
     * Checks whether the media object is currently playing a media. This is usually true when there is active playback
     *
     * @return True when there is active playback and false otherwise
     */
    public boolean isPlaying() {
        return isPlaying;
    }

    /**
     * Gets the state of the current media player object
     *
     * @return PlaybackState which is a representation of the current state of the media object
     */
    private PlaybackState getPlaybackState() {
        return playbackState;
    }

    /**
     * Sets the state of the media player object
     *
     * @param state one of PlaybackState.PLAYING, PlaybackState.STOPPED, PlaybackState.PAUSED, PlaybackState.IDLE
     *              and PlaybackState.BUFFERING
     */
    private void setPlaybackState(PlaybackState state) {
        this.playbackState = state;
    }

    /**
     * Release resources held by the media player back to the system
     */
    public void release() {
        exoPlayer.release();
    }

    public void pause() {
        exoPlayer.setPlayWhenReady(false);
    }

    public void stop() {
        exoPlayer.stop();
    }

    public int getAudioSessionId() {
        if (exoPlayer != null)
            return exoPlayer.getAudioSessionId();

        return -1;
    }

    /**
     * Only play when there is media loaded and the media controller is ready to play. If the state of the
     * media controller is IDLE or STOPPED, there is apparently no media playing so a call to prepare is required
     * which prepares the media controller for playback using the specified media source
     */
    public void play() {
        if ((getPlaybackState() == PlaybackState.IDLE || getPlaybackState() == PlaybackState.STOPPED) && !isPlaying)
            exoPlayer.prepare(mediaSource);

        exoPlayer.setPlayWhenReady(true);
    }


    public long getCurrentPosition() {
        return exoPlayer.getCurrentPosition();
    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        isPlaying = false;

        Log.i(DEBUG_TAG, "Media " + (isLoading ? "Loading..." : "Loaded!"));
        if (isLoading) {
            listener.onBuffering();
            setPlaybackState(PlaybackState.BUFFERING);
        }
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        switch (playbackState) {
            case STATE_READY:
                Log.d(TAG, "onPlayerStateChanged: Ready");
                break;

            case STATE_ENDED:
                Log.d(TAG, "onPlayerStateChanged: Ended");
                break;

            case STATE_BUFFERING:
                Log.d(TAG, "onPlayerStateChanged: Buffering...");
                break;
            case STATE_IDLE:
                Log.d(TAG, "onPlayerStateChanged: Idle");
        }

        if (playWhenReady && playbackState == Player.STATE_READY) {
            // Active playback.
            listener.onPlay();
            isPlaying = true;
            setPlaybackState(PlaybackState.PLAYING);
            Log.d(TAG, "onPlayerStateChanged: Playing...");
        } else if (playWhenReady) {
            // Not playing because playback ended, the player is buffering, stopped or
            // failed. Check playbackState and player.getPlaybackError for details.
            setPlaybackState(PlaybackState.STOPPED);
            isPlaying = false;
            listener.onStop();
            if (playbackState == Player.STATE_BUFFERING)
                Log.d(TAG, "onPlayerStateChanged: Buffering...");

        } else {
            // Paused by app.
            isPlaying = false;
            setPlaybackState(PlaybackState.PAUSED);
            listener.onPause();
            Log.d(TAG, "onPlayerStateChanged: Paused");
        }
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        isPlaying = false;
        setPlaybackState(PlaybackState.IDLE);
        listener.onError(error);
    }

    private String[] getStreamDurationStrings() {
        String[] durations = new String[2];
        int streamTimer = Integer.parseInt(mPreferences.getStreamingTimer()) * 3600;

        Seconds streamDurationHrs = Seconds.seconds(streamTimer);

        //get the current stream position
        Seconds currentPosition = Seconds.seconds((int) getCurrentPosition() / 1000);

        //the total Stream duration
        Period streamDurationPeriod = new Period(streamDurationHrs);

        //the current position of the stream
        Period currentPositionPeriod = new Period(currentPosition);

        //the difference between the total duration and the current duration
        Period diffPeriod = streamDurationPeriod.minus(currentPositionPeriod);


        if (diffPeriod.getSeconds() == 0) {
            stop();
        }

        PeriodFormatter formatter = new PeriodFormatterBuilder()
                .printZeroAlways()
                .minimumPrintedDigits(2)
                .appendHours()
                .appendSuffix(":")
                .appendMinutes()
                .appendSuffix(":")
                .appendSeconds()
                .toFormatter();

        String totalStreamStr = formatter.print(streamDurationPeriod.normalizedStandard());
        String streamProgressStr = formatter.print(currentPositionPeriod.normalizedStandard());

        durations[0] = totalStreamStr;
        durations[1] = streamProgressStr;

        return durations;
    }

    public Observable<String[]> getStreamDurationStringsObservable() {
        return Observable
                .interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map(aLong -> getStreamDurationStrings());
    }

    /**
     * Distinct Media playback states
     */
    public enum PlaybackState {
        PLAYING,
        PAUSED,
        BUFFERING,
        STOPPED,
        IDLE
    }


    public interface StreamStateChangesListener {
        void onPlay();

        void onBuffering();

        void onStop();

        void onPause();

        void onError(Exception exception);
    }
}
