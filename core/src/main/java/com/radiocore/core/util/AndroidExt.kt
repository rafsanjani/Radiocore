package com.radiocore.core.util
import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar


fun <T : Context> T.rateActivity() {
    val uri = Uri.parse("market://details?id=" + this.packageName)
    val goToMarket = Intent(Intent.ACTION_VIEW, uri)
    try {
        this.startActivity(goToMarket)
    } catch (e: ActivityNotFoundException) {
        this.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + this.packageName)))
    }
}

fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
    val tmp = this[index1] // 'this' corresponds to the list
    this[index1] = this[index2]
    this[index2] = tmp
}

fun isServiceRunning(serviceClass: Class<*>, context: Context): Boolean {
    val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}

fun toggleViewsVisibility(flag: Int, vararg views: ProgressBar?) {
    for (view in views) {
        if (flag == View.VISIBLE || flag == View.INVISIBLE || flag == View.GONE)
            view?.visibility = flag
    }
}

/**
 * Morph a target Button's imageUrl property from it's present one to the drawable specified by toDrawable
 *
 * @param target     the imageButton whose imageUrl property is to be animated
 * @param toDrawable the drawable which will be used for the morphing
 */
fun animateButtonDrawable(target: ImageButton, toDrawable: Drawable) {
    target.setImageDrawable(toDrawable)
    val animatable = target.drawable as Animatable
    animatable.start()
}
