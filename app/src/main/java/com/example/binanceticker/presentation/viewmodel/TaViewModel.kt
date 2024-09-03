package com.example.binanceticker.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.binanceticker.data.remote.NetworkResponse
import com.example.binanceticker.data.remote.WebSocketManager
import com.example.binanceticker.domain.model.ChartData
import com.example.binanceticker.domain.repository.CryptoRepository
import com.example.binanceticker.utils.toBarData
import com.example.binanceticker.utils.toHistogramData
import com.tradingview.lightweightcharts.api.series.enums.SeriesType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TaViewModel @Inject constructor(
    private val repository: CryptoRepository,
    webSocketManager: WebSocketManager
) : BaseViewModel(webSocketManager) {

    val seriesBarData: LiveData<ChartData> get() = barData
    private val barData = MutableLiveData<ChartData>()

    val seriesHistogramData: LiveData<ChartData> get() = histogramData
    private val histogramData = MutableLiveData<ChartData>()

    fun getKlines(symbol: String, interval: String) {
        viewModelScope.launch {
            repository.getKlines(symbol, interval).collect { response ->
                when (response) {
                    is NetworkResponse.Success -> {
                        val barDataList = response.data.map { it.toBarData() }
                        val histogramDataList = response.data.map { it.toHistogramData() }
                        barData.postValue(ChartData(barDataList, SeriesType.BAR))
                        histogramData.postValue(ChartData(histogramDataList, SeriesType.HISTOGRAM))
                    }
                    is NetworkResponse.Error -> {
                        Timber.e(response.errMessage)
                    }
                }
            }
        }
    }
}