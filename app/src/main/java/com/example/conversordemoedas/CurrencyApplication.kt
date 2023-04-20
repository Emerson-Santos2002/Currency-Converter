package com.example.conversordemoedas

import android.app.Application
import com.example.conversordemoedas.di.appModule
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class CurrencyApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin{
            androidLogger()
            modules(appModule)
        }
    }
}