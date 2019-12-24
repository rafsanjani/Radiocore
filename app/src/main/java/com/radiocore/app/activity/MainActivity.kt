package com.radiocore.app.activity

import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import com.radiocore.app.R
import com.radiocore.core.di.DaggerAndroidXAppCompatActivity

class MainActivity : DaggerAndroidXAppCompatActivity(R.layout.activity_host) {


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        startActivity(Intent(this, SettingsActivity::class.java))
        return true
    }
}