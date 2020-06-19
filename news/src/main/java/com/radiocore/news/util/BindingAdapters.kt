package com.radiocore.news.util

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.radiocore.news.R
import java.time.ZonedDateTime
import java.time.format.TextStyle
import java.util.*


@BindingAdapter("image")
fun setImage(image: ImageView, url: String) {
    Glide.with(image)
            .load(url)
            .error(R.drawable.newsimage)
            .placeholder(R.drawable.newsimage)
            .centerCrop()
            .into(image)
}


@BindingAdapter("date")
fun setDate(textView: TextView, date: String) {
    val dateObj = ZonedDateTime.parse(date).toLocalDate()

    val month = dateObj.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
    val day = dateObj.dayOfMonth.toString()
    val year = dateObj.year

    val formattedDate = "$month $day, $year"

    textView.text = formattedDate
}