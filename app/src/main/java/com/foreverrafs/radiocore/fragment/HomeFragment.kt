package com.foreverrafs.radiocore.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.foreverrafs.radiocore.R
import com.foreverrafs.radiocore.player.StreamMetadataListener
import com.foreverrafs.radiocore.player.StreamPlayer
import kotlinx.android.synthetic.main.fragment_home.*


// Created by Emperor95 on 1/13/2019.
class HomeFragment : Fragment() {

    val TAG = "HomeFragment"
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        StreamPlayer.getInstance(context!!).addMetadataListener(object : StreamMetadataListener {
            override fun onMetadataReceived(metadata: String) {
                if (metadata.isNotEmpty()) {
                    tvMetaData.text = metadata
                    tvMetaData.isSelected = true
                } else
                    Log.i(TAG, "onMetadataReceived: Empty Metadata")
            }
        })
    }
}