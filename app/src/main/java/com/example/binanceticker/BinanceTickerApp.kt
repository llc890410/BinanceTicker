package com.example.binanceticker

import android.app.Application
import com.example.binanceticker.data.remote.WebSocketManager
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class BinanceTickerApp : Application() {

    @Inject
    lateinit var webSocketManager: WebSocketManager

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        webSocketManager.connect()
    }

    override fun onTerminate() {
        super.onTerminate()
        webSocketManager.disconnect()
    }
}