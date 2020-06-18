package com.radiocore.news.util

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.radiocore.news.R
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat


@BindingAdapter("date")
fun setDate(view: TextView, date: DateTime) {
    val formatter = DateTimeFormat.forPattern("MMMM d, yyyy")
    view.text = date.toString(formatter)
}

@BindingAdapter("image")
fun setImage(image: ImageView, url: String) {
    Glide.with(image)
            .load(url)
            .error(R.drawable.newsimage)
            .placeholder(R.drawable.newsimage)
            .centerCrop()
            .into(image)
}