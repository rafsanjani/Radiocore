package com.foreverrafs.radiocore.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.foreverrafs.radiocore.R
import com.foreverrafs.radiocore.databinding.FragmentHomeBinding
import com.foreverrafs.radiocore.player.StreamMetadataListener
import com.foreverrafs.radiocore.player.StreamPlayer
import com.foreverrafs.radiocore.viewmodels.HomeViewModel
import timber.log.Timber


// Created by Emperor95 on 1/13/2019
class HomeFragment : Fragment(), StreamMetadataListener {

    private val viewModel: HomeViewModel by lazy {
        ViewModelProviders.of(this)[HomeViewModel::class.java]
    }

    var data = ""
    override fun onMetadataReceived(metadata: String) {
        if (metadata.isNotEmpty() && metadata != data) {
            viewModel.updateStreamMetaData(metadata)
//            tvMetaData.text = metadata
//            data = metadata
//                    tvMetaData.animation = slideRight
//                    slideRight.start()
            Timber.i("onMetadataReceived: new metadata received")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding: FragmentHomeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        binding.lifecycleOwner = this
        binding.viewmodel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        val slideRight = AnimationUtils.loadAnimation(context, R.anim.text_slide_right)
//        val slideLeft = AnimationUtils.loadAnimation(context, R.anim.text_slide_left)
//        val slideToOrigin = AnimationUtils.loadAnimation(context, R.anim.text_slide_to_origin)
//        slideRight.setAnimationListener(object : Animation.AnimationListener {
//            override fun onAnimationRepeat(p0: Animation?) {
//
//            }
//
//            override fun onAnimationEnd(p0: Animation?) {
//                tvMetaData.clearAnimation()
//                tvMetaData.animation = slideLeft
//            }
//
//            override fun onAnimationStart(p0: Animation?) {
//            }
//        })
//
//        slideLeft.setAnimationListener(object : Animation.AnimationListener {
//            override fun onAnimationRepeat(p0: Animation?) {
//
//            }
//
//            override fun onAnimationEnd(p0: Animation?) {
//                tvMetaData.clearAnimation()
//                tvMetaData.animation = slideToOrigin
//
//            }
//
//            override fun onAnimationStart(p0: Animation?) {
//
//            }
//        })


        StreamPlayer.getInstance(activity!!.applicationContext).addMetadataListener(this)
    }


    override fun onPause() {
        super.onPause()
        StreamPlayer.getInstance(activity!!.applicationContext).removeMetadataListener()
    }
}