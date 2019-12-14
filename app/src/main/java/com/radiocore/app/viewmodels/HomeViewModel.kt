package com.radiocore.app.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.radiocore.app.AudioStreamingService.AudioStreamingState

class HomeViewModel : ViewModel() {
    private var _metaData = MutableLiveData<String>()
    private var _playbackState = MutableLiveData<AudioStreamingState>(AudioStreamingState.STATUS_STOPPED)

    val metaData: LiveData<String> = _metaData
    val playbackState: LiveData<AudioStreamingState> = _playbackState

    fun updateStreamMetaData(data: String) {
        _metaData.value = data
    }

    fun updatePlaybackState(state: AudioStreamingState) {
        _playbackState.value = state
    }
}