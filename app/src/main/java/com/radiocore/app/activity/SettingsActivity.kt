package com.radiocore.app.activity

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.radiocore.app.R
import com.radiocore.core.di.DaggerAndroidXAppCompatActivity

class SettingsActivity : DaggerAndroidXAppCompatActivity(R.layout.settings_activity) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    internal class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }
}