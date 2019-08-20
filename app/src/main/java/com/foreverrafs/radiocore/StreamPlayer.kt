package com.foreverrafs.radiocore

import android.content.Context
import android.net.Uri
import android.util.Log
import com.foreverrafs.radiocore.util.RadioPreferences
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.joda.time.Period
import org.joda.time.Seconds
import org.joda.time.format.PeriodFormatterBuilder
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class StreamPlayer private constructor(context: Context) : EventListener {

    companion object {
        private val TAG = "StreamPlayer"
        private var instance: WeakReference<StreamPlayer>? = null

        fun getInstance(context: Context): StreamPlayer? {
            synchronized(StreamPlayer::class.java) {
                if (instance == null) {
                    instance = WeakReference(StreamPlayer(context))
                    Log.d(TAG, "getInstance: Media Instance Created on  " + Thread.currentThread().name)
                }
                return instance?.get()
            }
        }
    }

    private var mediaSource: MediaSource? = null
    private val exoPlayer: SimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(context)

    /**
     * Checks whether the media object is currently playing a media. This is usually true when there is active playback
     *
     * @return True when there is active playback and false otherwise
     */
    var isPlaying: Boolean = false

    private lateinit var listener: StreamStateChangesListener

    private var playbackState = PlaybackState.IDLE
    private val context: WeakReference<Context> = WeakReference(context)
    private val mPreferences: RadioPreferences = RadioPreferences(context)

    val audioSessionId: Int
        get() = exoPlayer.audioSessionId


    private val currentPosition: Long
        get() = exoPlayer.currentPosition

    private//get the current stream position
    //the total Stream duration
    //the current position of the stream
    //the difference between the total duration and the current duration
    val streamDurationStrings: Array<String?>
        get() {
            val durations = arrayOfNulls<String>(2)
            val streamTimer = Integer.parseInt(mPreferences.streamingTimer!!) * 3600

            val streamDurationHrs = Seconds.seconds(streamTimer)
            val currentPosition = Seconds.seconds(currentPosition.toInt() / 1000)
            val streamDurationPeriod = Period(streamDurationHrs)
            val currentPositionPeriod = Period(currentPosition)
            val diffPeriod = streamDurationPeriod.minus(currentPositionPeriod)


            if (diffPeriod.seconds == 0) {
                stop()
            }

            val formatter = PeriodFormatterBuilder()
                    .printZeroAlways()
                    .minimumPrintedDigits(2)
                    .appendHours()
                    .appendSuffix(":")
                    .appendMinutes()
                    .appendSuffix(":")
                    .appendSeconds()
                    .toFormatter()

            val totalStreamStr = formatter.print(streamDurationPeriod.normalizedStandard())
            val streamProgressStr = formatter.print(currentPositionPeriod.normalizedStandard())

            durations[0] = totalStreamStr
            durations[1] = streamProgressStr

            return durations
        }

    val streamDurationStringsObservable: Observable<Array<out String?>>?
        get() = Observable
                .interval(1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .map { streamDurationStrings }

    init {
        exoPlayer.addListener(this)
    }

    /**
     * Sets the Uri of the audio stream source.
     *
     * @param uri
     */
    fun setStreamSource(uri: Uri) {
        val dataSourceFactory = DefaultDataSourceFactory(context.get(), Util.getUserAgent(context.get(), "RadioCore"))
        mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

    /**
     * Attaches a listener to the media object which propagates media events back to the context class.
     *
     * @param listener StreamStateChangesListener which will be used to send events back to the context class
     */
    fun setPlayerStateChangesListener(listener: StreamStateChangesListener) {
        this.listener = listener
    }

    /**
     * Release resources held by the media player back to the system
     */
    fun release() {
        exoPlayer.release()
    }

    fun pause() {
        exoPlayer.playWhenReady = false
    }

    fun stop() {
        exoPlayer.stop()
    }

    /**
     * Only play when there is media loaded and the media controller is ready to play. If the state of the
     * media controller is IDLE or STOPPED, there is apparently no media playing so a call to prepare is required
     * which prepares the media controller for playback using the specified media source
     */
    fun play() {
        if ((playbackState == PlaybackState.IDLE || playbackState == PlaybackState.STOPPED) && !isPlaying)
            exoPlayer.prepare(mediaSource)

        exoPlayer.playWhenReady = true
    }

    override fun onLoadingChanged(isLoading: Boolean) {
        isPlaying = false

        Log.i(TAG, "Media " + if (isLoading) "Loading..." else "Loaded!")
        if (isLoading) {
            listener.onBuffering()
            playbackState = PlaybackState.BUFFERING
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        when (playbackState) {
            STATE_READY -> Log.d(TAG, "onPlayerStateChanged: Ready")

            STATE_ENDED -> Log.d(TAG, "onPlayerStateChanged: Ended")

            STATE_BUFFERING -> Log.d(TAG, "onPlayerStateChanged: Buffering...")
            STATE_IDLE -> Log.d(TAG, "onPlayerStateChanged: Idle")
        }

        if (playWhenReady && playbackState == STATE_READY) {
            // Active playback.
            listener.onPlay()
            isPlaying = true
            Log.d(TAG, "onPlayerStateChanged: Playing...")
        } else if (playWhenReady) {
            // Not playing because playback ended, the player is buffering, stopped or
            // failed. Check playbackState and player.getPlaybackError for details.
            isPlaying = false
            listener.onStop()
            if (playbackState == STATE_BUFFERING)
                Log.d(TAG, "onPlayerStateChanged: Buffering...")

        } else {
            // Paused by app.
            isPlaying = false
            //playbackState = PlaybackState.PAUSED
            listener.onPause()
            Log.d(TAG, "onPlayerStateChanged: Paused")
        }
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        isPlaying = false
        playbackState = PlaybackState.IDLE
        listener.onError(error)
    }

    /**
     * Distinct Media playback states
     */
    enum class PlaybackState {
        PLAYING,
        PAUSED,
        BUFFERING,
        STOPPED,
        IDLE
    }

    interface StreamStateChangesListener {
        fun onPlay()

        fun onBuffering()

        fun onStop()

        fun onPause()

        fun onError(exception: Exception?)
    }
}
