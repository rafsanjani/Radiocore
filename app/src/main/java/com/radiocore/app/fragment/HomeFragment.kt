package com.radiocore.app.fragment

import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.radiocore.app.R
import com.radiocore.app.databinding.FragmentHomeBinding
import com.radiocore.app.viewmodels.HomeViewModel
import com.radiocore.player.StreamMetadataListener
import com.radiocore.player.StreamPlayer
import kotlinx.android.synthetic.main.fragment_home.*
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
        StreamPlayer.getInstance(activity!!.applicationContext).addMetadataListener(this)
        startAnimations()
    }

    private fun startAnimations() {
        val metadataAnimation = AnimatorInflater.loadAnimator(activity, R.animator.metadata_anim_set)
        val logoAnimation = AnimatorInflater.loadAnimator(activity, R.animator.scale)

        metadataAnimation.setTarget(tvMetaData)
        logoAnimation.setTarget(imageCentralLogo)

        logoAnimation.start()
        metadataAnimation.start()

    }


    override fun onPause() {
        super.onPause()
        StreamPlayer.getInstance(activity!!.applicationContext).removeMetadataListener()
    }
}