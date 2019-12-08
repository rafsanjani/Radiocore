package com.foreverrafs.radiocore.util

import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.foreverrafs.radiocore.service.AudioStreamingService.AudioStreamingState
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

@BindingAdapter("app:hideIfLoading")
fun hideIfPlaying(view: ProgressBar, state: AudioStreamingState) {
    view.visibility = if (state == AudioStreamingState.STATUS_LOADING) View.VISIBLE else View.INVISIBLE
}

@BindingAdapter("app:date")
fun setDate(view: TextView, date: DateTime) {
    val formatter = DateTimeFormat.forPattern("MMMM d, yyyy")
    view.text = date.toString(formatter)
}