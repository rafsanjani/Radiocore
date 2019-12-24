package com.radiocore.app.di

import com.radiocore.app.fragment.AboutFragment
import com.radiocore.app.fragment.HomeFragment
import com.radiocore.app.fragment.MainFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class FragmentBuilderModule {
    @ContributesAndroidInjector
    abstract fun provideMainFragment(): MainFragment

    @ContributesAndroidInjector
    abstract fun provideHomeFragment(): HomeFragment

    @ContributesAndroidInjector
    abstract fun provideAboutFragment(): AboutFragment
}
