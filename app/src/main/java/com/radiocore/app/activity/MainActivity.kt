package com.radiocore.app.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import com.radiocore.app.R
import com.radiocore.app.viewmodels.SharedViewModel
import com.radiocore.core.di.DaggerAndroidXAppCompatActivity
import com.radiocore.player.AudioStreamingService

class MainActivity : DaggerAndroidXAppCompatActivity() {

    private lateinit var viewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)
        viewModel = ViewModelProvider(this).get(com.radiocore.app.viewmodels.SharedViewModel::class.java)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        startActivity(Intent(this, SettingsActivity::class.java))
        return true
    }

    override fun onStart() {
        super.onStart()
        if (viewModel.playbackState.value == AudioStreamingService.AudioStreamingState.STATUS_PLAYING
                && !viewModel.audioServiceConnection.isBound) {
        }
    }

    override fun onDestroy() {
        if (viewModel.audioServiceConnection.isBound)
            unbindService(viewModel.audioServiceConnection)

        super.onDestroy()
    }
}