package com.example.binanceticker.data.remote.api

import com.example.binanceticker.domain.model.ApiTicker
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface BinanceApiService {
    @GET("/api/v3/ticker/24hr")
    suspend fun getMiniTickers(): Response<List<ApiTicker>>

    @GET("/api/v3/uiKlines")
    suspend fun getUiKlines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("startTime") startTime: Long? = null,
        @Query("endTime") endTime: Long? = null,
        @Query("timeZone") timeZone: String? = "0",
        @Query("limit") limit: Int? = 500
    ): Response<ResponseBody>
}