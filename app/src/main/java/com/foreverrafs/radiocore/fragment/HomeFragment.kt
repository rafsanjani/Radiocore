package com.foreverrafs.radiocore.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import com.foreverrafs.radiocore.R
import com.foreverrafs.radiocore.player.StreamMetadataListener
import com.foreverrafs.radiocore.player.StreamPlayer
import kotlinx.android.synthetic.main.fragment_home.*
import java.lang.ref.WeakReference


// Created by Emperor95 on 1/13/2019.
class HomeFragment : Fragment() {

    val TAG = "HomeFragment"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val slideRight = AnimationUtils.loadAnimation(context, R.anim.text_slide_right)
        val slideLeft = AnimationUtils.loadAnimation(context, R.anim.text_slide_left)
        val slideToOrigin = AnimationUtils.loadAnimation(context, R.anim.text_slide_to_origin)
        slideRight.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                tvMetaData.clearAnimation()
                tvMetaData.animation = slideLeft
            }

            override fun onAnimationStart(p0: Animation?) {
            }
        })

        slideLeft.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationRepeat(p0: Animation?) {

            }

            override fun onAnimationEnd(p0: Animation?) {
                tvMetaData.clearAnimation()
                tvMetaData.animation = slideToOrigin

            }

            override fun onAnimationStart(p0: Animation?) {

            }
        })

        var data = "Nothing"
        val listener = object : StreamMetadataListener {
            override fun onMetadataReceived(metadata: String) {
                if (metadata.isNotEmpty() && metadata != data) {
                    tvMetaData.text = metadata
                    data = metadata
                    tvMetaData.animation = slideRight
                    slideRight.start()
                    Log.i(TAG, "onMetadataReceived: new metadata received")
                }
            }
        }

        StreamPlayer.getInstance(context!!).addMetadataListener(WeakReference(listener))
    }

    override fun onDestroy() {
        super.onDestroy()
        StreamPlayer.getInstance(context!!).removeMetadataListener()
    }

}