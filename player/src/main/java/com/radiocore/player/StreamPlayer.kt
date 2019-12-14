package com.radiocore.player

import android.content.Context
import android.net.Uri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.metadata.MetadataOutput
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.radiocore.core.util.RadioPreferences
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.Period
import org.joda.time.Seconds
import org.joda.time.format.PeriodFormatterBuilder
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class StreamPlayer private constructor(context: Context) : EventListener, LifecycleObserver {
    companion object {
        private var instance: WeakReference<StreamPlayer>? = null
        private var mStreamMetadataListener: StreamMetadataListener? = null

        fun getInstance(context: Context): StreamPlayer {
            synchronized(StreamPlayer::class.java) {
                if (instance == null) {
                    instance = WeakReference(StreamPlayer(context))
                }
                return instance?.get()!!
            }
        }
    }

    private var mMetaDataOutput = MetadataOutput { metadata ->
        for (n in 0 until metadata.length()) {
            when (val md = metadata[n]) {
                is com.google.android.exoplayer2.metadata.icy.IcyInfo -> {
                    mStreamMetadataListener?.onMetadataReceived(md.title!!.replace("';StreamUrl='", "RadioCore"))
                }
                else -> {
                    Timber.i("metaDataOutput: Other -  $md")
                }
            }
        }
    }

    private var mediaSource: MediaSource? = null

    private val exoPlayer by lazy {
        ExoPlayerFactory.newSimpleInstance(context).apply {
            setAudioAttributes(
                    AudioAttributes.Builder()
                            .setContentType(C.CONTENT_TYPE_MUSIC)
                            .setUsage(C.USAGE_MEDIA)
                            .build(), true
            )
            addMetadataOutput(mMetaDataOutput)
        }
    }

    init {
        exoPlayer.addListener(this)
    }

    fun addMetadataListener(streamMetadataListener: StreamMetadataListener) {
        mStreamMetadataListener = streamMetadataListener
    }

    fun removeMetadataListener() {
        exoPlayer.removeMetadataOutput(mMetaDataOutput)
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
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun release() {
        removeMetadataOutput()
        exoPlayer.release()
    }

    private fun removeMetadataOutput() {
        exoPlayer.removeMetadataOutput(mMetaDataOutput)
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
            else -> {
            }
        }

    }

    override fun onLoadingChanged(isLoading: Boolean) {
        Timber.i("Media %s", if (isLoading) "Loading..." else "Loaded!")
        if (isLoading) {
            mStreamStateChangesListener.onBuffering()
            mPlaybackState = PlaybackState.BUFFERING
        }
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, state: Int) {
        when (state) {
            STATE_READY -> Timber.i("onPlayerStateChanged: Ready")

            STATE_ENDED -> Timber.i("onPlayerStateChanged: Ended")

            STATE_BUFFERING -> Timber.i("onPlayerStateChanged: Buffering...")
            STATE_IDLE -> Timber.i("onPlayerStateChanged: Idle")
        }

        if (playWhenReady && state == STATE_READY) {
            // Active playback.
            mStreamStateChangesListener.onPlay()
            mPlaybackState = PlaybackState.PLAYING
            Timber.i("onPlayerStateChanged: Playing...")
        } else if (playWhenReady) {
            // Not playing because playback ended, the player is buffering, stopped or
            // failed. Check mPlaybackState and player.getPlaybackError for details.
            mPlaybackState = PlaybackState.STOPPED
            mStreamStateChangesListener.onStop()
            if (state == STATE_BUFFERING) {
                Timber.i("onPlayerStateChanged: Buffering...")
                mPlaybackState = PlaybackState.BUFFERING
                mStreamStateChangesListener.onBuffering()
            }

        } else {
            // Paused by app.
            mStreamStateChangesListener.onPause()
            mPlaybackState = PlaybackState.PAUSED
            Timber.i("onPlayerStateChanged: Paused")
        }
    }

    override fun onPlayerError(error: ExoPlaybackException?) {
        mPlaybackState = PlaybackState.IDLE
        mStreamStateChangesListener.onError(error)
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
