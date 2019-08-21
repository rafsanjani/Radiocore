package com.foreverrafs.radiocore.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.foreverrafs.radiocore.R
import com.foreverrafs.radiocore.StreamPlayer
import com.foreverrafs.radiocore.activity.HomeActivity
import com.foreverrafs.radiocore.util.Constants
import com.foreverrafs.radiocore.util.RadioPreferences


/***
 * Handle Audio playback
 */
class AudioStreamingService : Service(), AudioManager.OnAudioFocusChangeListener {
    override fun onBind(p0: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private val TAG = "AudioStreamingService"
    private val mFocusLock = Any()
    private var broadcastManager: LocalBroadcastManager? = null
    private lateinit var mediaPlayer: StreamPlayer
    private var radioPreferences: RadioPreferences? = null
    private var notificationText = "Empty" //this will be set when context is created
    private var streamNotification: Notification? = null
    private var audioManager: AudioManager? = null
    private var mResumeOnFocusGain: Boolean = false


    private val audioFocus: Boolean
        get() {
            audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

            synchronized(mFocusLock) {
                val audioFocusReqCode = audioManager?.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
                if (audioFocusReqCode == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
                    return false
                }
            }

            return true
        }

    override fun onCreate() {
        super.onCreate()

        notificationText = this.getString(R.string.live_radio_freq)

        streamNotification = createNotification()
        radioPreferences = RadioPreferences(this)
        broadcastManager = LocalBroadcastManager.getInstance(this)

        if (!audioFocus) {
            Log.i(TAG, "Couldn't gain audio focus:::::Exiting")
            return
        }


        try {
            mediaPlayer = StreamPlayer.getInstance(this)
            mediaPlayer.streamSource = Uri.parse(Constants.STREAM_URL)

            mediaPlayer.setPlayerStateChangesListener(object : StreamPlayer.StreamStateChangesListener {
                override fun onError(exception: Exception?) {
                    Toast.makeText(applicationContext, "Error loading stream!", Toast.LENGTH_SHORT).show()
                    sendResult(AudioStreamingState.STATUS_STOPPED)
                    radioPreferences?.status = Constants.STATUS_STOPPED
                    stopForeground(true)
                }

                override fun onPlay() {
                    sendResult(AudioStreamingState.STATUS_PLAYING)
                    radioPreferences?.status = Constants.STATUS_PLAYING
                    startForeground(5, streamNotification)
                }

                override fun onBuffering() {
                    sendResult(AudioStreamingState.STATUS_LOADING)
                    radioPreferences?.status = Constants.STATUS_LOADING
                }

                override fun onStop() {
                    sendResult(AudioStreamingState.STATUS_STOPPED)
                    radioPreferences?.status = Constants.STATUS_STOPPED
                    stopForeground(true)
                }

                override fun onPause() {
                    sendResult(AudioStreamingState.STATUS_PAUSED)
                    radioPreferences?.status = Constants.STATUS_STOPPED
//                    stopForeground(true)
                }

            })

        } catch (e: Exception) {
            Log.e(TAG, e.message!!)
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
     */
    private fun createNotification(): Notification {
        val contentIntent = Intent(this, HomeActivity::class.java)
        val pauseIntent = Intent(this, AudioStreamingService::class.java)
        val stopIntent = Intent(this, AudioStreamingService::class.java)

        pauseIntent.action = Constants.ACTION_PAUSE
        stopIntent.action = Constants.ACTION_STOP

        val contentPendingIntent = PendingIntent.getActivity(this, 5, contentIntent, 0)
        val pausePendingIntent = PendingIntent.getService(this, 6, pauseIntent, 0)
        val stopPendingIntent = PendingIntent.getService(this, 7, stopIntent, 0)


        val builder = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
                .setContentTitle("Online Radio")
                .addAction(R.drawable.ic_pause_notification, "Pause", pausePendingIntent)
                .addAction(R.drawable.ic_stop_notification, "Stop", stopPendingIntent)
                .setStyle(androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0))
                .setContentText(notificationText)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentIntent(contentPendingIntent)

        return builder.build()
    }

    /**
     * Start playback and set playback status in SharedPreferences.
     */
    private fun startPlayback() {
        mediaPlayer.play()
    }

    /**
     * Stop playback and set playback status in SharedPreferences
     */
    private fun stopPlayback() {
        mediaPlayer.stop()
        cleanShutDown()
    }

    private fun pausePlayback() {
        mediaPlayer.pause()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
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
        Log.i(TAG, "Performing stream status cleanup")
        radioPreferences?.cleanShutdown = true
    }

    override fun onDestroy() {
        super.onDestroy()
        radioPreferences?.status = Constants.STATUS_STOPPED
        audioManager?.abandonAudioFocus(this)
        StreamPlayer.getInstance(this).release()

        Log.d(TAG, "onDestroy: Service Destroyed")
    }

    /**
     * Send a result back to the Broadcast receiver of the calling activity_news_item_detail_pager, in this case (HomeActivity.java)
     * The result is basically the state of the stream audio and is usually one of STATUS_LOADING, STATUS_STOPPED or STATUS_PLAYING
     *
     * @param message which is an [AudioStreamingState]
     */
    fun sendResult(message: AudioStreamingState?) {
        val intent = Intent(Constants.STREAM_RESULT)
        //if (message != null)
        //Perform backwards conversion to AudioStreamingState in it's receivers
        intent.putExtra(Constants.STREAMING_STATUS, message.toString())

        broadcastManager?.sendBroadcast(intent)
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
                        Log.d(TAG, "OnAudioFocusChange: Focus Gained...resuming playback")
                    }
                } else {
                    Log.d(TAG, "OnAudioFocusChange: Focus Gained...Unable to resume playback")
                }

            AudioManager.AUDIOFOCUS_LOSS ->
                synchronized(mFocusLock) {
                    mResumeOnFocusGain = false
                    stopPlayback()
                    Log.d(TAG, "OnAudioFocusChange: Focus Lost completely...stopping playback")
                }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                synchronized(mFocusLock) {
                    val canResume = StreamPlayer.getInstance(this).isPlaying
                    mResumeOnFocusGain = canResume
                    Log.d(TAG, "OnAudioFocusChange: Focus Lost. We will resume. $canResume...pausing playback")
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
        STATUS_PAUSED
    }
}
