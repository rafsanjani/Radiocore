package com.radiocore.app.di

import com.radiocore.player.AudioStreamingService
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ServiceBuilderModule {
    @ContributesAndroidInjector
    abstract fun provideStreamingService(): AudioStreamingService
}