package com.radiocore.app

import android.app.Application
import android.os.StrictMode
import com.foreverrafs.radiocore.BuildConfig
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class RadioCoreApp @Inject constructor() : Application () {
    override fun onCreate() {
        super.onCreate()

        enableStrictMode()
        setUpTimber()
    }


    private fun setUpTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun enableStrictMode() {
        if (BuildConfig.DEBUG) {
            val policy = StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build()

            StrictMode.setVmPolicy(policy)
        }
    }
}