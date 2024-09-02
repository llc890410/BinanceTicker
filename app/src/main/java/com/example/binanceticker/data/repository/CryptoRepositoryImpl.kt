package com.example.binanceticker.data.repository

import com.example.binanceticker.data.remote.NetworkResponse
import com.example.binanceticker.data.remote.RemoteDataSource
import com.example.binanceticker.domain.model.KlineData
import com.example.binanceticker.domain.model.ApiTicker
import com.example.binanceticker.domain.repository.CryptoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONArray
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class CryptoRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource
): CryptoRepository {
    override suspend fun getTop100Cryptos(): Flow<NetworkResponse<List<ApiTicker>>> = flow {
        try {
            val response = remoteDataSource.fetchCryptoData()
            if (response.isSuccessful && response.body() != null) {
                val data = response.body()!!
                    .filter { it.symbol.endsWith("USDT") }
                    .sortedByDescending { it.quoteVolume.toDouble() }
                    .take(100)
                emit(NetworkResponse.Success(data))
            } else {
                emit(NetworkResponse.Error(response.message(), response.code()))
            }
        } catch (e: Exception) {
            emit(handleException(e))
        }
    }

    override suspend fun getKlines(
        symbol: String,
        interval: String,
    ): Flow<NetworkResponse<List<KlineData>>> = flow {
        try {
            val response = remoteDataSource.fetchKlines(symbol, interval)
            val responseBody = response.body()
            if (response.isSuccessful && responseBody != null) {
                val data = responseBody.string()
                val klines = parseKlines(data)
                emit(NetworkResponse.Success(klines))
            } else {
                emit(NetworkResponse.Error(response.message(), response.code()))
            }
        } catch (e: Exception) {
            emit(handleException(e))
        }
    }

    private fun parseKlines(jsonString: String?): List<KlineData> {
        return try {
            val jsonArray = JSONArray(jsonString)
            val klineDataList = mutableListOf<KlineData>()

            for (i in 0 until jsonArray.length()) {
                val klineArray = jsonArray.getJSONArray(i)
                val klineData = KlineData(
                    openTime = klineArray.getLong(0),
                    openPrice = klineArray.getString(1),
                    highPrice = klineArray.getString(2),
                    lowPrice = klineArray.getString(3),
                    closePrice = klineArray.getString(4),
                    volume = klineArray.getString(5),
                    closeTime = klineArray.getLong(6),
                    quoteAssetVolume = klineArray.getString(7),
                    numberOfTrades = klineArray.getLong(8),
                    takerBuyBaseAssetVolume = klineArray.getString(9),
                    takerBuyQuoteAssetVolume = klineArray.getString(10)
                )
                klineDataList.add(klineData)
            }

            klineDataList
        } catch (e: Exception) {
            Timber.e("Failed to parse klines: ${e.message}")
            emptyList()
        }
    }

    private fun handleException(e: Exception): NetworkResponse.Error {
        Timber.e(e, "Exception in CryptoRepositoryImpl")
        return when (e) {
            is HttpException -> NetworkResponse.Error("Network error: ${e.message()}", e.code())
            is IOException -> NetworkResponse.Error("IO error: ${e.message}")
            else -> NetworkResponse.Error("Unknown error: ${e.message}")
        }
    }

}