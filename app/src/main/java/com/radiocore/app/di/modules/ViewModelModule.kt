package com.radiocore.app.di.modules

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.radiocore.app.di.ViewModelKey
import com.radiocore.app.viewmodels.AppViewModel
import com.radiocore.app.viewmodels.ViewModelFactory
import com.radiocore.news.ui.NewsViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import javax.inject.Singleton

@Module
abstract class ViewModelModule {
    @Singleton
    @Binds
    @IntoMap
    @ViewModelKey(AppViewModel::class)
    abstract fun bindAuthViewModel(viewModel: AppViewModel): ViewModel

    @Singleton
    @Binds
    @IntoMap
    @ViewModelKey(NewsViewModel::class)
    abstract fun bindNewsViewModel(viewModel: NewsViewModel): ViewModel


    @Singleton
    @Binds
    abstract fun bindViewModelFactory(mainFactory: ViewModelFactory): ViewModelProvider.Factory

}