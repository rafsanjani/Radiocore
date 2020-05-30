package com.radiocore.app.di.modules

import android.content.Context
import com.radiocore.app.RadioCoreApp
import com.radiocore.core.util.Constants
import com.radiocore.core.util.RadioPreferences
import com.radiocore.news.data.NewsDataSource
import com.radiocore.news.data.remote.NewsApi
import com.radiocore.news.data.remote.RemoteDataSource
import com.radiocore.news.util.GsonConverters
import com.radiocore.player.StreamPlayer
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
object AppModule {

    @Provides
    @JvmStatic
    @Singleton
    fun provideContext(application: RadioCoreApp): Context {
        return application.applicationContext
    }

    @Provides
    @JvmStatic
    @Singleton
    fun provideRadioPreferences(context: Context): RadioPreferences {
        return RadioPreferences(context)
    }

    @Provides
    @JvmStatic
    @Singleton
    fun provideStreamPlayer(context: Context, preferences: RadioPreferences): StreamPlayer {
        return StreamPlayer(context, preferences)
    }

    @Provides
    @JvmStatic
    @Singleton
    fun provideRetrofit(): Retrofit.Builder {
        return Retrofit.Builder()
                .baseUrl(Constants.NEWS_URL)
                .addConverterFactory(GsonConverterFactory.create(GsonConverters.instance!!))
    }

    @Provides
    @JvmStatic
    @Singleton
    fun provideNewsApiService(retrofit: Retrofit.Builder): NewsApi {
        return retrofit
                .build()
                .create(NewsApi::class.java)
    }

    @Provides
    @JvmStatic
    @Singleton
    fun provideRemoteDataSource(preferences: RadioPreferences, newsApi: NewsApi) : NewsDataSource {
        return RemoteDataSource(preferences, newsApi)
    }
}
