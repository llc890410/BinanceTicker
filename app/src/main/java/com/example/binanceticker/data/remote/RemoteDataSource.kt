package com.example.binanceticker.data.remote

import com.example.binanceticker.data.remote.api.BinanceApiService
import javax.inject.Inject

class RemoteDataSource @Inject constructor(
    private val apiService: BinanceApiService
){
    suspend fun fetchCryptoData() = apiService.getMiniTickers()
}