package com.example.binanceticker.data.remote.api

import com.example.binanceticker.domain.model.CryptoCurrency
import retrofit2.Response
import retrofit2.http.GET

interface BinanceApiService {
    @GET("/api/v3/ticker/24hr")
    suspend fun getMiniTickers()
    : Response<List<CryptoCurrency>>
}