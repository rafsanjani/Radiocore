package com.radiocore.app.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.ViewModelProvider
import com.foreverrafs.radiocore.R
import com.radiocore.app.viewmodels.AppViewModel
import com.radiocore.core.di.DaggerAndroidXAppCompatActivity

class MainActivity : DaggerAndroidXAppCompatActivity() {

    private lateinit var viewModel: AppViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_host)
        viewModel = ViewModelProvider(this).get(AppViewModel::class.java)
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