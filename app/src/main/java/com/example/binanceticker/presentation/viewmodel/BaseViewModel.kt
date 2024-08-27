package com.example.binanceticker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.binanceticker.data.remote.WebSocketManager
import com.example.binanceticker.domain.model.Ticker
import kotlinx.coroutines.flow.SharedFlow

abstract class BaseViewModel(
    private val webSocketManager: WebSocketManager
) : ViewModel() {

    val tickerStream: SharedFlow<Pair<String, Ticker>> = webSocketManager.tickerData

    init {
        webSocketManager.connect()
    }

    override fun onCleared() {
        super.onCleared()
        webSocketManager.disconnect()
    }

    fun subscribeSymbols(symbols: String) {
        webSocketManager.subscribe(symbols)
    }

    fun unsubscribeSymbols(symbols: String) {
        webSocketManager.unsubscribe(symbols)
    }
}