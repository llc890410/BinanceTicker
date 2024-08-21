package com.example.binanceticker.domain.repository

import com.example.binanceticker.domain.model.CryptoCurrency
import kotlinx.coroutines.flow.Flow

interface CryptoRepository {
    suspend fun getTop100Cryptos(): Flow<List<CryptoCurrency>>
}