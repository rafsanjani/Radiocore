package com.radiocore.app

import DaggerAppComponent
import android.os.StrictMode
import com.crashlytics.android.Crashlytics
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication
import io.fabric.sdk.android.Fabric
import timber.log.Timber

class RadioCoreApp : DaggerApplication() {
    override fun onCreate() {
        super.onCreate()

        enableStrictMode()
        setUpCrashlytics()
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

    private fun setUpCrashlytics() {
        if (!BuildConfig.DEBUG) {
            Fabric.with(this, Crashlytics())
            Timber.i("setUpCrashlytics: Enabled")
        }
    }
}