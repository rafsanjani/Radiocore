package com.radiocore.app.fragment

import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.radiocore.app.R
import com.radiocore.app.databinding.FragmentHomeBinding
import com.radiocore.app.viewmodels.HomeViewModel
import com.radiocore.core.di.DaggerAndroidXFragment
import com.radiocore.player.StreamMetadataListener
import com.radiocore.player.StreamPlayer
import kotlinx.android.synthetic.main.fragment_home.*
import timber.log.Timber
import javax.inject.Inject


// Created by Emperor95 on 1/13/2019
class HomeFragment : DaggerAndroidXFragment(), StreamMetadataListener {

    private val viewModel: HomeViewModel by lazy {
        ViewModelProviders.of(this)[HomeViewModel::class.java]
    }

    @Inject
    lateinit var mStreamPlayer: StreamPlayer


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
        startAnimations()
    }

    override fun onResume() {
        super.onResume()
        mStreamPlayer.addMetadataListener(this)
    }

    private fun startAnimations() {
        val metadataAnimation = AnimatorInflater.loadAnimator(activity, R.animator.metadata_anim_set)
        val logoAnimation = AnimatorInflater.loadAnimator(activity, R.animator.scale)

        metadataAnimation.setTarget(tvMetaData)
        logoAnimation.setTarget(imageCentralLogo)

        logoAnimation.start()
        metadataAnimation.start()
    }

}