package com.radiocore.app

import android.app.Application
import android.os.StrictMode
import com.crashlytics.android.Crashlytics
import io.fabric.sdk.android.Fabric
import timber.log.Timber

class RadioCoreApp : Application() {
    override fun onCreate() {
        super.onCreate()

        enableStrictMode()
        setUpCrashlytics()
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

    private fun setUpCrashlytics() {
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
            Timber.i("setUpCrashlytics: Enabled")
        }
    }
}