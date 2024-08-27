package com.example.binanceticker.domain.repository

import com.example.binanceticker.data.remote.NetworkResponse
import com.example.binanceticker.domain.model.ApiTicker
import kotlinx.coroutines.flow.Flow

interface CryptoRepository {
    suspend fun getTop100Cryptos(): Flow<NetworkResponse<List<ApiTicker>>>
}