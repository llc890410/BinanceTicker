package com.example.binanceticker.data.repository

import com.example.binanceticker.data.remote.NetworkResponse
import com.example.binanceticker.data.remote.RemoteDataSource
import com.example.binanceticker.domain.model.CryptoCurrency
import com.example.binanceticker.domain.repository.CryptoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class CryptoRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource
): CryptoRepository {
    override suspend fun getTop100Cryptos(): Flow<NetworkResponse<List<CryptoCurrency>>> = flow {
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
        } catch (e: HttpException) {
            emit(NetworkResponse.Error("Network error: ${e.message()}", e.code()))
        } catch (e: IOException) {
            emit(NetworkResponse.Error("IO error: ${e.message}"))
        } catch (e: Exception) {
            emit(NetworkResponse.Error("Unknown error: ${e.message}"))
        }
    }
}