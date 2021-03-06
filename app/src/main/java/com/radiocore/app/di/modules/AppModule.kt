package com.radiocore.app.di.modules

import android.app.Application
import androidx.room.Room
import com.radiocore.RadioPreferences
import com.radiocore.news.data.repository.NewsRepositoryImpl
import com.radiocore.news.data.repository.NewsRepository
import com.radiocore.news.data.database.NewsDao
import com.radiocore.news.data.database.NewsDatabase
import com.radiocore.news.data.api.NewsApi
import com.radiocore.player.StreamPlayer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Module
@InstallIn(ApplicationComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideRadioPreferences(app: Application): RadioPreferences {
        return RadioPreferences(app)
    }

    @Singleton
    @Provides
    fun provideRetrofit(): Retrofit.Builder {
        val url = "https://newscentral.herokuapp.com/"

        return Retrofit.Builder()
                .baseUrl(url)
                .addConverterFactory(GsonConverterFactory.create())
    }

    @Singleton
    @Provides
    fun provideNewsApiService(retrofit: Retrofit.Builder): NewsApi {
        return retrofit
                .build()
                .create(NewsApi::class.java)
    }

    @Singleton
    @Provides
    fun provideNewsDatabase(app: Application) : NewsDatabase{
       return Room.databaseBuilder(app, NewsDatabase::class.java, "RadioCoreDB")
                .fallbackToDestructiveMigration().build()
    }

    @Singleton
    @Provides
    fun provideNewsDao(newsDatabase: NewsDatabase) : NewsDao{
        return newsDatabase.newsDao()
    }

    @Singleton
    @Provides
    fun provideNewsRepository(newsDao: NewsDao, newsApi: NewsApi) : NewsRepository {
        return NewsRepositoryImpl(newsDao, newsApi)
    }

    @Singleton
    @Provides
    fun provideStreamPlayer(app: Application, preferences: RadioPreferences): StreamPlayer {
        return StreamPlayer(app, preferences)
    }
}
