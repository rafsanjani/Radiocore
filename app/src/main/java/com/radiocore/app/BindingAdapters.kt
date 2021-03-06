package com.radiocore.app

import android.view.View
import android.widget.ProgressBar
import androidx.databinding.BindingAdapter
import com.radiocore.player.AudioStreamingService.AudioStreamingState
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
@BindingAdapter("hideIfLoading")
fun hideIfPlaying(view: ProgressBar, state: AudioStreamingState) {
    view.visibility = if (state == AudioStreamingState.STATUS_LOADING) View.VISIBLE else View.INVISIBLE
}