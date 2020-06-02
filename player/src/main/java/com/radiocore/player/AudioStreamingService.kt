package com.radiocore.player

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
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.radiocore.core.di.DaggerAndroidService
import com.radiocore.core.util.*
import timber.log.Timber
import javax.inject.Inject


/***
 * Handle Audio playback
 */
//todo: use a lifecycle service
class AudioStreamingService : DaggerAndroidService(), AudioManager.OnAudioFocusChangeListener {
    private val binder = AudioServiceBinder()

    private val mFocusLock = Any()

    @Inject
    lateinit var mMediaPlayer: StreamPlayer

    @Inject
    lateinit var mRadioPreferences: RadioPreferences

    private lateinit var mNotificationManager: NotificationManager

    var metaData = MutableLiveData<String>()

    private var _playbackState = MutableLiveData(AudioStreamingState.STATUS_STOPPED)

    val playBackState: LiveData<AudioStreamingState>
        get() = _playbackState

    private fun setStreamState(state: AudioStreamingState) {
        _playbackState.postValue(state)
    }

    private lateinit var mNotificationText: String
    private lateinit var mStreamNotification: Notification
    private lateinit var mContentIntent: PendingIntent
    private lateinit var mAudioManager: AudioManager
    private lateinit var mFocusRequest: AudioFocusRequest
    private var mResumeOnFocusGain: Boolean = false

    companion object {
        private const val NOTIFICATION_ID: Int = 5
        const val NOTIFICATION_CHANNEL_ID = "com.radiocore.notification_channel"
        const val NOTIFICATION_CHANNEL_NAME = "RadioCore Notification Channel"
    }

    override fun onBind(intent: Intent): IBinder? {
        return binder
    }

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
        mNotificationText = this.getString(R.string.live_radio_freq, getString(R.string.org_freq))
        mRadioPreferences = RadioPreferences(this)
//        mBroadcastManager = LocalBroadcastManager.getInstance(this)
        mAudioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (playbackAuthorized)
            Timber.i("Audio Focus gained on Android o+")

        try {
            mMediaPlayer.streamSource = Uri.parse(STREAM_URL)

            mMediaPlayer.setPlayerStateChangesListener(object : StreamPlayer.StreamStateChangesListener {
                override fun onError(exception: Exception?) {
                    Timber.i("setPlayerStateChangesListener: Error Loading Stream ${exception?.message}")
                    Toast.makeText(applicationContext, "Error loading stream!", Toast.LENGTH_SHORT).show()
                    sendResult(AudioStreamingState.STATUS_STOPPED)
                    stopForeground(true)
                }

                override fun onPlay() {
                    Timber.i("setPlayerStateChangesListener: OnPlay")
                    sendResult(AudioStreamingState.STATUS_PLAYING)
                    startForeground(NOTIFICATION_ID, mStreamNotification)
                }

                override fun onBuffering() {
                    Timber.i("setPlayerStateChangesListener: OnBuffering")
                    sendResult(AudioStreamingState.STATUS_LOADING)
                }

                override fun onStop() {
                    Timber.i("setPlayerStateChangesListener: OnStop")
                    sendResult(AudioStreamingState.STATUS_STOPPED)
                    stopForeground(true)
                }
            })

        } catch (e: Exception) {
            Timber.i(e.message!!)
        }

    }

    override fun onUnbind(intent: Intent?): Boolean {
        return false
    }


    /**
     * Create notification channel on Android O+
     *
     * @return
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    NOTIFICATION_CHANNEL_NAME,
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
    private fun createNotification(contentIntent: PendingIntent = mContentIntent,
                                   notificationText: String = mNotificationText): Notification {
        createNotificationChannel()
        val playIntent = Intent(this, AudioStreamingService::class.java)
        val stopIntent = Intent(this, AudioStreamingService::class.java)

        stopIntent.action = ACTION_STOP
        playIntent.action = ACTION_PLAY

        val stopPendingIntent = PendingIntent.getService(this, 7, stopIntent, 0)

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Online Radio")
                .addAction(R.drawable.ic_stop_notification, getString(R.string.stop), stopPendingIntent)
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0))
                .setContentText(notificationText)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(contentIntent)

        return builder.build()
    }

    private fun createNotification(): Notification {
        createNotificationChannel()
        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setContentTitle("RadioCore")
                .setContentText("Initialized")
                .setColor(ContextCompat.getColor(this, R.color.amber_400))
                .setSmallIcon(R.drawable.notification)

        return builder.build()
    }

    /**
     * Start playback and set playback status in SharedPreferences.
     */
    fun startPlayback() {
        mMediaPlayer.play()
        startForeground(NOTIFICATION_ID, mStreamNotification)

        mMediaPlayer.addMetadataListener(object : StreamMetadataListener {
            var data = ""
            override fun onMetadataReceived(metadata: String) {
                if (metadata.isNotEmpty() && metadata != data) {
                    metaData.value = metadata
                    data = metadata
                }
            }
        })

        metaData.observeForever(metaDataObserver)
    }

    private val metaDataObserver = Observer<String> {
        mNotificationManager.notify(NOTIFICATION_ID, createNotification(notificationText = it))
    }

    /**
     * Stop playback and set playback status in SharedPreferences
     */
    fun stopPlayback() {
        stopForeground(true)
        mMediaPlayer.pause()
        cleanShutDown()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        if (mMediaPlayer.playBackState != StreamPlayer.PlaybackState.PLAYING)
            startForeground(NOTIFICATION_ID, createNotification())
        else
            stopPlayback()

        return START_NOT_STICKY
    }

    private fun cleanShutDown() {
        stopForeground(true)
        Timber.i("Performing stream status cleanup")
        mRadioPreferences.cleanShutdown = true
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAudioManager.abandonAudioFocusRequest(mFocusRequest)
        }

        Timber.i("onDestroy: Service Destroyed")
        metaData.removeObserver(metaDataObserver)

        super.onDestroy()
    }

    /**
     * Send a result back to the stream observers, in this case (HomeActivity.java)
     * The result is basically the state of the stream audio and is usually one of STATUS_LOADING, STATUS_STOPPED or STATUS_PLAYING
     *
     * @param state which is an [AudioStreamingState]
     */
    fun sendResult(state: AudioStreamingState) {
        setStreamState(state)
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
                // pausePlayback()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                synchronized(mFocusLock) {
                    val canResume = mMediaPlayer.playBackState == StreamPlayer.PlaybackState.PLAYING
                    mResumeOnFocusGain = canResume
                    Timber.i("OnAudioFocusChange: Focus Lost. We will resume. $canResume...pausing playback")
                }
                stopPlayback()
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
    }

    inner class AudioServiceBinder : Binder() {
        fun getAudioService(intent: PendingIntent): AudioStreamingService {
            mContentIntent = intent
            mStreamNotification = createNotification(intent)
            return this@AudioStreamingService
        }
    }
}
