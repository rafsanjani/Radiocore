package com.radiocore.app.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import com.radiocore.app.R
import com.radiocore.app.viewmodels.SharedViewModel
import com.radiocore.core.di.DaggerAndroidXAppCompatActivity

class MainActivity : DaggerAndroidXAppCompatActivity() {

    private lateinit var viewModel: SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)
        viewModel = ViewModelProvider(this).get(SharedViewModel::class.java)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        startActivity(Intent(this, SettingsActivity::class.java))
        return true
    }

    override fun onDestroy() {
        if (viewModel.audioServiceConnection.isBound)
            unbindService(viewModel.audioServiceConnection)

        super.onDestroy()
    }
}