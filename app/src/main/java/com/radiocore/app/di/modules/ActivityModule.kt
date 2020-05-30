package com.radiocore.app.di.modules


import com.radiocore.app.activity.MainActivity
import com.radiocore.app.activity.SettingsActivity
import com.radiocore.app.activity.SplashActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class ActivityModule {

    @ContributesAndroidInjector
    abstract fun contributeMainActivity(): MainActivity

    @ContributesAndroidInjector
    abstract fun contributeSettingsActivity(): SettingsActivity

    @ContributesAndroidInjector
    abstract fun contributeSplashActivity(): SplashActivity
}