package com.foreverrafs.radiocore.util

import android.view.View
import android.widget.ProgressBar
import androidx.databinding.BindingAdapter
import com.foreverrafs.radiocore.service.AudioStreamingService.AudioStreamingState

@BindingAdapter("app:hideIfLoading")
fun hideIfPlaying(view: ProgressBar, state: AudioStreamingState) {
    view.visibility = if (state == AudioStreamingState.STATUS_LOADING) View.VISIBLE else View.INVISIBLE
}