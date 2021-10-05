package com.jay.sunnyweather

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class SunnyWeatherApplication : Application() {
    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        const val TOKEN = "Q7DEhI5WAMjTRnJd"
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }
}