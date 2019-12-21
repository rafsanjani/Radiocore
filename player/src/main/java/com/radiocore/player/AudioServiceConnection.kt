package com.radiocore.player

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder

class AudioServiceConnection : ServiceConnection {
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
        val binder = service as AudioStreamingService.LocalBinder
        audioStreamingService = binder.getAudioService()
        isBound = true
    }

}