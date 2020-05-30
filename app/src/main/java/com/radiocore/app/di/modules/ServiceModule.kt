package com.radiocore.app.di.modules

import com.radiocore.player.AudioStreamingService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceModule {
    @ContributesAndroidInjector
    abstract fun provideStreamingService(): AudioStreamingService
}