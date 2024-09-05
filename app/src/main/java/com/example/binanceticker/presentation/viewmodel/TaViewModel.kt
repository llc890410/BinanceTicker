package com.example.binanceticker.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.binanceticker.data.remote.NetworkResponse
import com.example.binanceticker.data.remote.WebSocketManager
import com.example.binanceticker.domain.model.ChartData
import com.example.binanceticker.domain.model.SymbolQuoteData
import com.example.binanceticker.domain.repository.CryptoRepository
import com.example.binanceticker.utils.toBarData
import com.example.binanceticker.utils.toHistogramData
import com.example.binanceticker.utils.toKlineData
import com.example.binanceticker.utils.toSymbolQuoteData
import com.tradingview.lightweightcharts.api.series.enums.SeriesType
import com.tradingview.lightweightcharts.api.series.models.BarData
import com.tradingview.lightweightcharts.api.series.models.HistogramData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TaViewModel @Inject constructor(
    private val repository: CryptoRepository,
    webSocketManager: WebSocketManager
) : BaseViewModel(webSocketManager) {

    val cryptoData: LiveData<SymbolQuoteData> get() = _cryptoData
    private val _cryptoData = MutableLiveData<SymbolQuoteData>()

    val seriesBarData: LiveData<ChartData> get() = barData
    private val barData = MutableLiveData<ChartData>()

    val seriesHistogramData: LiveData<ChartData> get() = histogramData
    private val histogramData = MutableLiveData<ChartData>()

    private var apiBarDataList = mutableListOf<BarData>()
    private var apiHistogramDataList = mutableListOf<HistogramData>()

    private var symbols = listOf<String>()
    private val interval = "1d"

    fun init(symbolQuoteData: SymbolQuoteData) {
        _cryptoData.value = symbolQuoteData
        symbols = listOf(symbolQuoteData.symbol)
        getKlines(symbolQuoteData.symbol, interval)
        startTrackingSymbols()
        collectWebSocketData()
    }

    private fun getKlines(symbol: String, interval: String) {
        viewModelScope.launch {
            repository.getKlines(symbol, interval).collect { response ->
                when (response) {
                    is NetworkResponse.Success -> {
                        apiBarDataList = response.data.map { it.toBarData() }.toMutableList()
                        apiHistogramDataList = response.data.map { it.toHistogramData() }.toMutableList()
                        barData.postValue(ChartData(apiBarDataList, SeriesType.BAR))
                        histogramData.postValue(ChartData(apiHistogramDataList, SeriesType.HISTOGRAM))
                    }
                    is NetworkResponse.Error -> {
                        Timber.e(response.errMessage)
                    }
                }
            }
        }
    }

    private fun collectWebSocketData() {
        viewModelScope.launch {
            webSocketTickerFlow
                .filter { ticker ->
                    symbols.contains(ticker.symbol)
                }
                .collect { ticker ->
                    _cryptoData.postValue(ticker.toSymbolQuoteData())
                    Timber.d("Symbol: ${ticker.symbol}, WebSocketTicker: $ticker")
                }
        }
        viewModelScope.launch {
            webSocketKlineFlow
                .filter { kline ->
                    symbols.contains(kline.symbol)
                }
                .collect { kline ->
                    val klineData = kline.toKlineData()
                    val newBarData = klineData.toBarData()
                    val newHistogramData = klineData.toHistogramData()

                    if (apiBarDataList.isEmpty() || apiHistogramDataList.isEmpty()) {
                        return@collect
                    }

                    val newBarDataList = apiBarDataList.toMutableList()
                    if (newBarDataList.last().time.date == newBarData.time.date) {
                        newBarDataList.removeAt(newBarDataList.size - 1)
                        newBarDataList.add(newBarData)
                    } else if (newBarDataList.last().time.date < newBarData.time.date) {
                        newBarDataList.add(newBarData)
                    }
                    val newHistogramDataList = apiHistogramDataList.toMutableList()
                    if (newHistogramDataList.last().time.date == newHistogramData.time.date) {
                        newHistogramDataList.removeAt(newHistogramDataList.size - 1)
                        newHistogramDataList.add(newHistogramData)
                    } else if (newHistogramDataList.last().time.date < newHistogramData.time.date) {
                        newHistogramDataList.add(newHistogramData)
                    }

                    barData.postValue(ChartData(newBarDataList, SeriesType.BAR))
                    histogramData.postValue(ChartData(newHistogramDataList, SeriesType.HISTOGRAM))
                    Timber.d("Symbol: ${klineData.symbol}, KlineData: $klineData")
                }
        }
    }

    private fun startTrackingSymbols() {
        subscribeTickers(symbols)
        subscribeKlines(symbols, interval)
    }

    private fun stopTrackingSymbols() {
        unsubscribeTickers(symbols)
        unsubscribeKlines(symbols, interval)
    }

    override fun onCleared() {
        super.onCleared()
        stopTrackingSymbols()
    }
}