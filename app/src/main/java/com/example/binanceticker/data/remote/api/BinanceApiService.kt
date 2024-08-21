package com.example.binanceticker.data.remote.api

import com.example.binanceticker.domain.model.CryptoCurrency
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BinanceApiService {
    @GET("/api/v3/ticker/24hr")
    suspend fun getMiniTickers(
        @Query("type") type: String = "MINI")
    : Response<List<CryptoCurrency>>
}