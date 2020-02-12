package com.radiocore.player

import android.app.PendingIntent
import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder

/***
 * The intent passed to this class will be used to relaunch the activity when the content of the notification
 * is tapped.
 */
class AudioServiceConnection(var intent: PendingIntent) : ServiceConnection {
    private var isBound: Boolean = false
    private lateinit var audioStreamingService: AudioStreamingService

    val audioService: AudioStreamingService?
        get() {
            return if (isBound) audioStreamingService else null
        }

    override fun onServiceDisconnected(name: ComponentName?) {
        isBound = false
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as AudioStreamingService.AudioServiceBinder
        audioStreamingService = binder.getAudioService(intent)
        isBound = true
    }

}