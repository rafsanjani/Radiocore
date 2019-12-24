package com.radiocore.app.di

import android.content.Context

import com.radiocore.app.RadioCoreApp
import com.radiocore.core.util.RadioPreferences
import com.radiocore.news.api.ApiServiceGenerator
import com.radiocore.news.data.remote.NewsApi
import com.radiocore.player.StreamPlayer
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Provides
    @Singleton
    fun provideContext(application: RadioCoreApp): Context {
        return application.applicationContext
    }

    @Provides
    @Singleton
    fun provideRadioPreferences(context: Context): RadioPreferences {
        return RadioPreferences(context)
    }

    @Provides
    @Singleton
    fun provideStreamPlayer(context: Context): StreamPlayer {
        return StreamPlayer(context)
    }

    @Provides
    @Singleton
    fun provideNewsApiService(): NewsApi {
        return ApiServiceGenerator.createService(NewsApi::class.java)
    }

}
