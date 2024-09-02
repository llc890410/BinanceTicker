package com.example.binanceticker.presentation.viewmodel

import androidx.lifecycle.viewModelScope
import com.example.binanceticker.data.remote.NetworkResponse
import com.example.binanceticker.data.remote.WebSocketManager
import com.example.binanceticker.domain.repository.CryptoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class TaViewModel @Inject constructor(
    private val repository: CryptoRepository,
    webSocketManager: WebSocketManager
) : BaseViewModel(webSocketManager) {

    fun getKlines(symbol: String, interval: String) {
        viewModelScope.launch {
            repository.getKlines(symbol, interval).collect { response ->
                when (response) {
                    is NetworkResponse.Success -> {
                        Timber.d(response.data.toString())
                    }
                    is NetworkResponse.Error -> {
                        Timber.e(response.errMessage)
                    }
                }
            }
        }
    }

}