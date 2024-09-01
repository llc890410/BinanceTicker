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
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CryptoViewModel @Inject constructor(
    private val repository: CryptoRepository,
    webSocketManager: WebSocketManager
) : BaseViewModel(webSocketManager) {

    private val _cryptoUIState = MutableStateFlow(UiState.Loading as UiState<List<SymbolQuoteData>>)
    val cryptoUIState: StateFlow<UiState<List<SymbolQuoteData>>> = _cryptoUIState

    private var symbols = listOf<String>()

    init {
        fetchTop100CryptoData()
        collectWebSocketData()
    }

    private fun fetchTop100CryptoData() {
        viewModelScope.launch {
            _cryptoUIState.value = UiState.Loading
            repository.getTop100Cryptos().collect { response ->
                when (response) {
                    is NetworkResponse.Success -> {
                        val symbolQuoteDataList = response.data.map { it.toSymbolQuoteData() }.take(20) //暫時取20個
                        _cryptoUIState.value = UiState.Success(symbolQuoteDataList)
                        symbols = symbolQuoteDataList.map { it.symbol }
                        startTrackingSymbols()
                    }
                    is NetworkResponse.Error -> {
                        _cryptoUIState.value = UiState.Error(response.errMessage)
                    }
                }
            }
        }
    }

    private fun collectWebSocketData() {
        viewModelScope.launch {
            webSocketTickerFlow
                .filter { (symbol, _) -> symbols.contains(symbol) }
                .collect { (symbol, ticker) ->
                    Timber.d("Symbol: $symbol, WebSocketTicker: $ticker")
                    updateCryptoData(symbol, ticker.toSymbolQuoteData())
                }
        }
    }

    private fun updateCryptoData(symbol: String, symbolQuoteData: SymbolQuoteData) {
        val currentData = _cryptoUIState.value
        if (currentData is UiState.Success) {
            val updatedList = currentData.data.map { item ->
                if (item.symbol == symbol) {
                    symbolQuoteData
                } else {
                    item
                }
            }
            _cryptoUIState.value = UiState.Success(updatedList)
        }
    }

    private fun startTrackingSymbols() {
        subscribeTickers(symbols)
    }

    private fun stopTrackingSymbols() {
        unsubscribeTickers(symbols)
    }

    override fun onCleared() {
        super.onCleared()
        stopTrackingSymbols()
    }
}