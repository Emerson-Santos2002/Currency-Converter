package com.example.conversordemoedas.di

import com.example.conversordemoedas.data.repository.MainRepository
import com.example.conversordemoedas.ui.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val repositoryModule = module {
    single {
        MainRepository(apiService = get())
    }
}

val viewModelModule = module {
    viewModel { MainViewModel(repository = get()) }
}

val appModule = listOf(
    networkModule,
    repositoryModule,
    viewModelModule
)