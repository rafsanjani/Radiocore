package com.radiocore.app.fragment

import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.radiocore.app.R
import com.radiocore.app.databinding.FragmentHomeBinding
import com.radiocore.app.viewmodels.SharedViewModel
import com.radiocore.core.di.DaggerAndroidXFragment
import com.radiocore.player.StreamPlayer
import kotlinx.android.synthetic.main.fragment_home.*
import javax.inject.Inject


// Created by Emperor95 on 1/13/2019
class HomeFragment : DaggerAndroidXFragment() {

    private lateinit var viewModel: SharedViewModel

    @Inject
    lateinit var mStreamPlayer: StreamPlayer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(activity!!).get(SharedViewModel::class.java)

        val binding: FragmentHomeBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        binding.lifecycleOwner = this
        binding.viewmodel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
}