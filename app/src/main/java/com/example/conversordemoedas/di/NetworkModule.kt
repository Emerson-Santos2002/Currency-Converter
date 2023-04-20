package com.example.conversordemoedas.di

import com.example.conversordemoedas.data.remote.ApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

const val BASE_URl = "https://cdn.jsdelivr.net/"

fun provideInterceptor(): HttpLoggingInterceptor =
    HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

fun provideHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .addInterceptor(provideInterceptor())
        .build()
}

val networkModule = module {
    single<ApiService> {

        Retrofit.Builder()
            .baseUrl(BASE_URl)
            .client(provideHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

    }
}