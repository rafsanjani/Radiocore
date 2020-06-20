package com.radiocore.player

import android.content.Context
import android.net.Uri
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
import com.radiocore.RadioPreferences
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import java.util.concurrent.TimeUnit


@ExperimentalCoroutinesApi
class StreamPlayer(private var context: Context, private var preferences: RadioPreferences) : EventListener {
    val metadataChannel = ConflatedBroadcastChannel<String>()

    private var mMetaDataOutput = MetadataOutput { metadata ->
        for (n in 0 until metadata.length()) {
            when (val md = metadata[n]) {
                is com.google.android.exoplayer2.metadata.icy.IcyInfo -> {

                    CoroutineScope(Dispatchers.Default).launch {
                        metadataChannel.send(md.title!!.replace("';StreamUrl='", "RadioCore"))
                    }

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

    private lateinit var mStreamStateChangesListener: StreamStateChangesListener

    private var mPlaybackState = PlaybackState.IDLE


    val audioSessionId: Int
        get() = exoPlayer.audioSessionId


    val currentPosition: Long
        get() = exoPlayer.currentPosition

    /**
     * Gets the position of the playback and the total stream timer duration formatted and presented as two strings
     * The first string contains the total stream duration, the second string contains the time elapsed
     * An example output will be: 00:00:01 for elapsed and 05:00:00 (5 hours) for stream timer duration
     */
    @FlowPreview
    val streamDurationStringsFlow: Flow<Array<Long?>>
        get() = flow {
            for (i in 0..Int.MAX_VALUE) {
                val durations = arrayOfNulls<Long>(2)

                //total allocated stream time in seconds
                val streamTimer = TimeUnit.HOURS.toSeconds(preferences.streamingTimer!!.toLong())

                //current position in seconds
                val currentPosition = TimeUnit.MILLISECONDS.toSeconds(currentPosition)

                val difference = streamTimer - currentPosition

                if (difference <= 0) {
                    stop()
                }

                durations[0] = currentPosition
                durations[1] = streamTimer

                emit(durations)
                delay(1000)
            }
        }


    var streamSource: Uri? = null
        set(value) {
            val dataSourceFactory = DefaultDataSourceFactory(context, Util.getUserAgent(context, "RadioCore"))
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

    private fun stop() {
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
        if (state == STATE_BUFFERING) {
            mStreamStateChangesListener.onBuffering()
            Timber.i("onPlayerStatechanged: Buffering")
            return
        }

        if (playWhenReady && state == STATE_READY) {
            //active playback
            mStreamStateChangesListener.onPlay()
            mPlaybackState = PlaybackState.PLAYING
            return
        }

        if (!playWhenReady && state == STATE_READY) {
            mPlaybackState = PlaybackState.STOPPED
            mStreamStateChangesListener.onStop()
            return
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

        fun onError(exception: Exception?)
    }
}
