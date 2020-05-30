package com.radiocore.app

import DaggerAppComponent
import android.os.StrictMode
import com.foreverrafs.radiocore.BuildConfig
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import timber.log.Timber

class RadioCoreApp : DaggerApplication() {
    override fun onCreate() {
        super.onCreate()

        enableStrictMode()
        setUpTimber()
    }

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        return DaggerAppComponent.factory().create(this)
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