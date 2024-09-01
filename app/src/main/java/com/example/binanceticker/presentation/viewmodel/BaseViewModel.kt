package com.example.binanceticker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.binanceticker.data.remote.WebSocketManager
import com.example.binanceticker.domain.model.WebSocketKline
import com.example.binanceticker.domain.model.WebSocketTicker
import kotlinx.coroutines.flow.SharedFlow

abstract class BaseViewModel(
    private val webSocketManager: WebSocketManager
) : ViewModel() {

    val webSocketTickerFlow: SharedFlow<WebSocketTicker> = webSocketManager.ticker
    val webSocketKlineFlow: SharedFlow<WebSocketKline> = webSocketManager.kline

    fun subscribeTickers(symbols: List<String>) {
        webSocketManager.subscribeTicker(symbols)
    }

    fun unsubscribeTickers(symbols: List<String>) {
        webSocketManager.unsubscribeTicker(symbols)
    }

    fun subscribeKlines(symbols: List<String>, interval: String) {
        webSocketManager.subscribeKline(symbols, interval)
    }

    fun unsubscribeKlines(symbols: List<String>, interval: String) {
        webSocketManager.unsubscribeKline(symbols, interval)
    }
}