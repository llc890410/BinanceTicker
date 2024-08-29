package com.example.binanceticker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.binanceticker.data.remote.WebSocketManager
import com.example.binanceticker.domain.model.WebSocketTicker
import kotlinx.coroutines.flow.SharedFlow

abstract class BaseViewModel(
    private val webSocketManager: WebSocketManager
) : ViewModel() {

    val webSocketTickerFlow: SharedFlow<Pair<String, WebSocketTicker>> = webSocketManager.ticker

    fun subscribeSymbols(symbols: List<String>) {
        webSocketManager.subscribe(symbols)
    }

    fun unsubscribeSymbols(symbols: List<String>) {
        webSocketManager.unsubscribe(symbols)
    }
}