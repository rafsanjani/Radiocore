package com.foreverrafs.radiocore.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.foreverrafs.radiocore.R
import com.foreverrafs.radiocore.activity.HomeActivity
import com.foreverrafs.radiocore.player.StreamPlayer
import com.foreverrafs.radiocore.util.Constants
import com.foreverrafs.radiocore.util.RadioPreferences
import timber.log.Timber


/***
 * Handle Audio playback
 */
class AudioStreamingService : LifecycleService(), AudioManager.OnAudioFocusChangeListener {


    private val TAG = "AudioStreamingService"
    private val mFocusLock = Any()
    private var mBroadcastManager: LocalBroadcastManager? = null
    private lateinit var mMediaPlayer: StreamPlayer
    private var mRadioPreferences: RadioPreferences? = null
    private var mNotificationText = "Empty" //this will be set when context is created
    private var streamNotification: Notification? = null
    private lateinit var mAudioManager: AudioManager
    private lateinit var mFocusRequest: AudioFocusRequest
    private var mResumeOnFocusGain: Boolean = false


    private val playbackAuthorized: Boolean
        get() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)
                return false

            mFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                setAudioAttributes(AudioAttributes.Builder().run {
                    setUsage(AudioAttributes.USAGE_GAME)
                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    build()
                })
                setOnAudioFocusChangeListener(this@AudioStreamingService).build()
            }


            var playbackNowAuthorized: Boolean
            val res = mAudioManager.requestAudioFocus(mFocusRequest)
            synchronized(mFocusLock) {
                playbackNowAuthorized = when (res) {
                    AudioManager.AUDIOFOCUS_REQUEST_FAILED -> false
                    AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                        true
                    }
                    AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                        false
                    }
                    else -> false
                }
            }
            return playbackNowAuthorized
        }

    override fun onCreate() {
        super.onCreate()
        mNotificationText = this.getString(R.string.live_radio_freq)
        streamNotification = createNotification()
        mRadioPreferences = RadioPreferences(this)
        mBroadcastManager = LocalBroadcastManager.getInstance(this)
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (playbackAuthorized)
            Timber.i("Audio Focus gained on Android o+")

        try {
            mMediaPlayer = StreamPlayer.getInstance(this)
            mMediaPlayer.streamSource = Uri.parse(Constants.STREAM_URL)

            mMediaPlayer.setPlayerStateChangesListener(object : StreamPlayer.StreamStateChangesListener {
                override fun onError(exception: Exception?) {
                    Timber.i("setPlayerStatechangesListener: Error Loading Stream ${exception?.message}")
                    Toast.makeText(applicationContext, "Error loading stream!", Toast.LENGTH_SHORT).show()
                    sendResult(AudioStreamingState.STATUS_STOPPED)
                    stopForeground(true)
                }

                override fun onPlay() {
                    Timber.i("setPlayerStatechangesListener: OnPlay")
                    sendResult(AudioStreamingState.STATUS_PLAYING)
                    startForeground(5, streamNotification)
                }

                override fun onBuffering() {
                    Timber.i("setPlayerStatechangesListener: OnBuffering")
                    sendResult(AudioStreamingState.STATUS_LOADING)
                }

                override fun onStop() {
                    Timber.i("setPlayerStateChangesListener: OnStop")
                    sendResult(AudioStreamingState.STATUS_STOPPED)
                    stopForeground(true)
                }

                override fun onPause() {
                    Timber.i("setPlayerStateChangesListener: OnPause")
                    sendResult(AudioStreamingState.STATUS_PAUSED)
                }

            })

        } catch (e: Exception) {
            Timber.i(e.message!!)
        }

    }

    /**
     * Create notification channel on Android O+
     *
     * @return
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                    Constants.NOTIFICATION_CHANNEL_ID,
                    "Music Streaming Channel",
                    NotificationManager.IMPORTANCE_LOW
            )


            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(notificationChannel)
        }
    }

    /**
     * Create notification using the channel created by [.createNotificationChannel]
     * and also instantiate the field variable (builder)
     *
     * @return a Notification object which c
     * TODO: Dynamically add play and pause buttons to notification
     */
    private fun createNotification(): Notification {
        val contentIntent = Intent(this, HomeActivity::class.java)
        val playIntent = Intent(this, AudioStreamingService::class.java)
        val stopIntent = Intent(this, AudioStreamingService::class.java)
//        val pauseIntent = Intent(this, AudioStreamingService::class.java)


        stopIntent.action = Constants.ACTION_STOP
        playIntent.action = Constants.ACTION_PLAY

        val contentPendingIntent = PendingIntent.getActivity(this, 5, contentIntent, 0)
//        val pausePendingIntent = PendingIntent.getService(this, 6, pauseIntent, 0)
        val stopPendingIntent = PendingIntent.getService(this, 7, stopIntent, 0)
//        val playPendingIntent = PendingIntent.getService(this, 8, playIntent, 0)


        val builder = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Online Radio")
                .addAction(R.drawable.ic_stop_notification, "Stop", stopPendingIntent)
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0))
                .setColor(ContextCompat.getColor(this, R.color.amber_400))
                .setContentText(mNotificationText)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(contentPendingIntent)

        return builder.build()
    }

    /**
     * Start playback and set playback status in SharedPreferences.
     */
    private fun startPlayback() {
        mMediaPlayer.play()
    }

    /**
     * Stop playback and set playback status in SharedPreferences
     */
    private fun stopPlayback() {
        mMediaPlayer.stop()
        cleanShutDown()
    }

    private fun pausePlayback() {
        mMediaPlayer.pause()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        //on some rare occasions, the state of the stream is indeterminate so we perform a cleanup before attempting to reload
        createNotificationChannel()

        startForeground(5, streamNotification)


        when (intent.action) {
            Constants.ACTION_PLAY -> startPlayback()

            Constants.ACTION_STOP -> stopPlayback()

            Constants.ACTION_PAUSE -> pausePlayback()
        }
        return START_NOT_STICKY
    }


    private fun cleanShutDown() {
        stopForeground(true)
        Timber.i("Performing stream status cleanup")
        mRadioPreferences?.cleanShutdown = true
    }

    override fun onDestroy() {
        super.onDestroy()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAudioManager.abandonAudioFocusRequest(mFocusRequest)
        }

        Timber.i("onDestroy: Service Destroyed")
    }

    /**
     * Send a result back to the Broadcast receiver of the calling activity_news_item_detail_pager, in this case (HomeActivity.java)
     * The result is basically the state of the stream audio and is usually one of STATUS_LOADING, STATUS_STOPPED or STATUS_PLAYING
     *
     * @param message which is an [AudioStreamingState]
     */
    fun sendResult(message: AudioStreamingState?) {
        val intent = Intent(Constants.STREAM_RESULT)
        intent.putExtra(Constants.STREAMING_STATUS, message.toString())

        mBroadcastManager?.sendBroadcast(intent)
    }

    /***
     * Called when a different application interrupts the audio on the target device. This can
     * be triggered by the phone ringing as a result of an incoming call or the user opening and accessing
     * an app which produces a sound such as a media player or a game
     * @param focusChange
     */
    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN ->
                if (mResumeOnFocusGain) {
                    synchronized(mFocusLock) {
                        mResumeOnFocusGain = false
                        startPlayback()
                        Timber.i("OnAudioFocusChange: Focus Gained...resuming playback")
                    }
                } else {
                    Timber.i("OnAudioFocusChange: Focus Gained...Unable to resume playback")
                }

            AudioManager.AUDIOFOCUS_LOSS -> {
                synchronized(mFocusLock) {
                    mResumeOnFocusGain = true
                    Timber.i("OnAudioFocusChange: Focus Lost completely...stopping playback")
                }
                pausePlayback()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                synchronized(mFocusLock) {
                    val canResume = StreamPlayer.getInstance(this).playBackState == StreamPlayer.PlaybackState.PLAYING
                    mResumeOnFocusGain = canResume
                    Timber.i("OnAudioFocusChange: Focus Lost. We will resume. $canResume...pausing playback")
                }
                pausePlayback()
            }
        }
    }


    /**
     * Discrete states of the Audio Streaming Service
     */
    enum class AudioStreamingState {
        STATUS_PLAYING,
        STATUS_STOPPED,
        STATUS_LOADING,
        STATUS_PAUSED,
    }
}
