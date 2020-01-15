package com.aurimteam.recordclient

import android.app.Application
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class App : Application() {
    companion object {
        private val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }).build()
        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.43.153:4000/") //api-адрес к серверу
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}