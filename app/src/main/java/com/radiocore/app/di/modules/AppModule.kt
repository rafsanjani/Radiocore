package com.radiocore.app.di.modules

import android.app.Application
import com.radiocore.core.util.NEWS_URL
import com.radiocore.core.util.RadioPreferences
import com.radiocore.news.data.NewsDataSource
import com.radiocore.news.data.remote.NewsApi
import com.radiocore.news.data.remote.RemoteDataSource
import com.radiocore.news.util.GsonConverters
import com.radiocore.player.StreamPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.components.ServiceComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {
    @Provides
    fun provideRadioPreferences(app: Application): RadioPreferences {
        return RadioPreferences(app)
    }


    @Provides
    @JvmStatic
    fun provideRetrofit(): Retrofit.Builder {
        return Retrofit.Builder()
                .baseUrl(NEWS_URL)
                .addConverterFactory(GsonConverterFactory.create(GsonConverters.instance!!))
    }

    @Provides
    @JvmStatic
    fun provideNewsApiService(retrofit: Retrofit.Builder): NewsApi {
        return retrofit
                .build()
                .create(NewsApi::class.java)
    }

    @Provides
    @JvmStatic
    fun provideRemoteDataSource(preferences: RadioPreferences, newsApi: NewsApi): NewsDataSource {
        return RemoteDataSource(preferences, newsApi)
    }

    @Provides
    @JvmStatic
    fun provideStreamPlayer(app: Application, preferences: RadioPreferences): StreamPlayer {
        return StreamPlayer(app, preferences)
    }
}
