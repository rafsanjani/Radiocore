package com.radiocore.app.di.modules

import com.radiocore.app.fragment.AboutFragment
import com.radiocore.app.fragment.HomeFragment
import com.radiocore.app.fragment.MainFragment
import com.radiocore.news.ui.NewsListFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentModule {
    @ContributesAndroidInjector
    abstract fun provideMainFragment(): MainFragment

    @ContributesAndroidInjector
    abstract fun provideHomeFragment(): HomeFragment

    @ContributesAndroidInjector
    abstract fun provideAboutFragment(): AboutFragment

    @ContributesAndroidInjector
    abstract fun provideNewsFragment(): NewsListFragment
}
