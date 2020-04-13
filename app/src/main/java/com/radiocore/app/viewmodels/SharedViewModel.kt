package com.radiocore.app.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radiocore.player.AudioServiceConnection
import com.radiocore.player.AudioStreamingService.AudioStreamingState

class SharedViewModel : ViewModel() {
    private var _metaData = MutableLiveData<String>()
    private var _playbackState = MutableLiveData(AudioStreamingState.STATUS_STOPPED)
    lateinit var audioServiceConnection: AudioServiceConnection

    val metaData: LiveData<String> = _metaData
    val playbackState: LiveData<AudioStreamingState> = _playbackState

    fun updateStreamMetaData(data: String) {
        _metaData.value = data
    }

    fun updatePlaybackState(state: AudioStreamingState) {
        _playbackState.value = state
    }
}