package com.example.binanceticker.data.repository

import com.example.binanceticker.data.remote.RemoteDataSource
import com.example.binanceticker.domain.model.CryptoCurrency
import com.example.binanceticker.domain.repository.CryptoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class CryptoRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource
): CryptoRepository {
    override suspend fun getTop100Cryptos(): Flow<List<CryptoCurrency>> = flow {
        val response = remoteDataSource.fetchCryptoData()
        val result = if (response.isSuccessful && response.body() != null) {
            response.body()!!
                .filter { it.symbol.endsWith("USDT") }
                .sortedByDescending { it.quoteVolume.toDouble() }
                .take(100)
        } else {
            emptyList()
        }
        emit(result)
    }
}