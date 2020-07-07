package com.radiocore.app.fragment

import android.animation.AnimatorInflater
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.foreverrafs.radiocore.R
import com.foreverrafs.radiocore.databinding.FragmentHomeBinding
import com.radiocore.app.viewmodels.AppViewModel
import com.radiocore.player.StreamPlayer
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject


// Created by Emperor95 on 1/13/2019
@ExperimentalCoroutinesApi
@AndroidEntryPoint
class LiveFragment : Fragment() {
    private val viewModel: AppViewModel by activityViewModels()

    @Inject
    lateinit var mStreamPlayer: StreamPlayer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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