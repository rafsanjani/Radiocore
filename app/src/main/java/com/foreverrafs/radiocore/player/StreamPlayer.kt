package com.foreverrafs.radiocore.player

import android.content.Context
import android.net.Uri
import android.util.Log
import com.foreverrafs.radiocore.util.RadioPreferences
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.Period
import org.joda.time.Seconds
import org.joda.time.format.PeriodFormatterBuilder
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class StreamPlayer private constructor(context: Context) : EventListener {

    companion object {
        private const val TAG = "StreamPlayer"
        private var instance: WeakReference<StreamPlayer>? = null
        private var mStreamMetadataListener: StreamMetadataListener? = null

        fun getInstance(context: Context): StreamPlayer {
            synchronized(StreamPlayer::class.java) {
                if (instance == null) {
                    instance = WeakReference(StreamPlayer(context))
                    Log.d(TAG, "getInstance: Media Instance Created on  " + Thread.currentThread().name)
                }
                return instance?.get()!!
            }
        }
    }

    private var mediaSource: MediaSource? = null
    private val exoPlayer: SimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(context).apply {
        setAudioAttributes(
                AudioAttributes.Builder()
                        .setContentType(C.CONTENT_TYPE_MUSIC)
                        .setUsage(C.USAGE_MEDIA)
                        .build(), true
        )
        addMetadataOutput { metadata ->
            for (n in 0 until metadata.length()) {
                when (val md = metadata[n]) {
                    is com.google.android.exoplayer2.metadata.icy.IcyInfo -> {
                        mStreamMetadataListener?.onMetadataReceived(md.title!!)
                    }
                    else -> {
                        Log.d(TAG, "metaDataOutput: Other -  $md")
                    }
                }
            }
        }
    }

    init {
        exoPlayer.addListener(this)
    }

    fun addMetadataListener(streamMetadataListener: StreamMetadataListener) {
        mStreamMetadataListener = streamMetadataListener
    }

    private lateinit var mStreamStateChangesListener: StreamStateChangesListener

    private var mPlaybackState = PlaybackState.IDLE
    private val context: WeakReference<Context> = WeakReference(context)
    private val mPreferences: RadioPreferences = RadioPreferences(context)

    val audioSessionId: Int
        get() = exoPlayer.audioSessionId


    val currentPosition: Long
        get() = exoPlayer.currentPosition

    /**
     * Gets the position of the playback and the total stream timer duration formatted and presented as two strings
     * The first string contains the total stream duration, the second string contains the time elapsed
     * An example output will be: 00:00:01 for elapsed and 05:00:00 (5 hours) for stream timer duration
     */
    private val streamDurationStrings: Array<String?>
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

    /**
     * Get an observable from [streamDurationStrings]
     */
    @Suppress("unused")
    val streamDurationStringsObservable: Observable<Array<out String?>>
        get() = Observable
                .interval(1, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map { streamDurationStrings }


    var streamSource: Uri? = null
        set(value) {
            val dataSourceFactory = DefaultDataSourceFactory(context.get(), Util.getUserAgent(context.get(), "RadioCore"))
            mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(value)
        }

    val playBackState: PlaybackState
        get() {
            return this.mPlaybackState
        }


    /**
     * Attaches a mStreamStateChangesListener to the media object which propagates media events back to the context class.
     *
     * @param listener StreamStateChangesListener which will be used to send events back to the context class
     */
    fun setPlayerStateChangesListener(listener: StreamStateChangesListener) {
        this.mStreamStateChangesListener = listener
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
        when (mPlaybackState) {
            PlaybackState.IDLE, PlaybackState.STOPPED -> {
                exoPlayer.prepare(mediaSource)
                exoPlayer.playWhenReady = true
            }
            PlaybackState.PAUSED, PlaybackState.BUFFERING -> exoPlayer.playWhenReady = true
            else -> TODO()
        }

    }

    override fun onLoadingChanged(isLoading: Boolean) {
        Log.i(TAG, "Media" + if (isLoading) "Loading..." else "Loaded!")
        if (isLoading) {
            // isPlaying = false
            mStreamStateChangesListener.onBuffering()
            mPlaybackState = PlaybackState.BUFFERING
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, state: Int) {
        when (state) {
            STATE_READY -> Log.d(TAG, "onPlayerStateChanged: Ready")

            STATE_ENDED -> Log.d(TAG, "onPlayerStateChanged: Ended")

            STATE_BUFFERING -> Log.d(TAG, "onPlayerStateChanged: Buffering...")
            STATE_IDLE -> Log.d(TAG, "onPlayerStateChanged: Idle")
        }

        if (playWhenReady && state == STATE_READY) {
            // Active playback.
            mStreamStateChangesListener.onPlay()
            //  isPlaying = true
            mPlaybackState = PlaybackState.PLAYING
            Log.d(TAG, "onPlayerStateChanged: Playing...")
        } else if (playWhenReady) {
            // Not playing because playback ended, the player is buffering, stopped or
            // failed. Check mPlaybackState and player.getPlaybackError for details.
            // isPlaying = false
            mPlaybackState = PlaybackState.STOPPED
            mStreamStateChangesListener.onStop()
            if (state == STATE_BUFFERING) {
                Log.d(TAG, "onPlayerStateChanged: Buffering...")
                mPlaybackState = PlaybackState.BUFFERING
            }

        } else {
            // Paused by app.
            // isPlaying = false
            mStreamStateChangesListener.onPause()
            mPlaybackState = PlaybackState.PAUSED
            Log.d(TAG, "onPlayerStateChanged: Paused")
        }
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        //isPlaying = false
        mPlaybackState = PlaybackState.IDLE
        mStreamStateChangesListener.onError(error)
    }

    /**
     * Distinct Media playback states
     */
    @Suppress("unused")
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
