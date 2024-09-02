package com.example.binanceticker.data.remote

import com.example.binanceticker.data.remote.api.BinanceApiService
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val apiService: BinanceApiService
){
    suspend fun fetchCryptoData() = apiService.getMiniTickers()

    suspend fun fetchKlines(
        symbol: String,
        interval: String,
        startTime: Long? = null,
        endTime: Long? = null,
        timeZone: String? = "0",
        limit: Int? = 500
    ) = apiService.getUiKlines(symbol, interval, startTime, endTime, timeZone, limit)
}