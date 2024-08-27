package com.example.binanceticker.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.binanceticker.data.remote.NetworkResponse
import com.example.binanceticker.data.remote.WebSocketManager
import com.example.binanceticker.domain.model.SymbolQuoteData
import com.example.binanceticker.domain.repository.CryptoRepository
import com.example.binanceticker.presentation.state.UiState
import com.example.binanceticker.utils.toSymbolQuoteData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CryptoViewModel @Inject constructor(
    private val repository: CryptoRepository,
    webSocketManager: WebSocketManager
) : BaseViewModel(webSocketManager) {

    private val _cryptoUIState = MutableStateFlow(UiState.Loading as UiState<List<SymbolQuoteData>>)
    val cryptoUIState: StateFlow<UiState<List<SymbolQuoteData>>> = _cryptoUIState

    fun fetchTop100CryptoData() {
        viewModelScope.launch {
            _cryptoUIState.value = UiState.Loading
            repository.getTop100Cryptos().collect { response ->
                _cryptoUIState.value = when (response) {
                    is NetworkResponse.Success -> UiState.Success(response.data.map { it.toSymbolQuoteData() })
                    is NetworkResponse.Error -> UiState.Error(response.errMessage)
                }
            }
        }
    }

    private val symbols = listOf("BTCUSDT", "ETHUSDT", "BNBUSDT")

    fun startTrackingSymbols() {
        symbols.forEach { symbol ->
            subscribeSymbols(symbol)
        }
    }

    fun stopTrackingSymbols() {
        symbols.forEach { symbol ->
            unsubscribeSymbols(symbol)
        }
    }
}