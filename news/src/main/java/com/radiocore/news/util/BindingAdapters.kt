package com.radiocore.news.util

import android.widget.TextView
import androidx.databinding.BindingAdapter
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat


@BindingAdapter("date")
fun setDate(view: TextView, date: DateTime) {
    val formatter = DateTimeFormat.forPattern("MMMM d, yyyy")
    view.text = date.toString(formatter)
}
