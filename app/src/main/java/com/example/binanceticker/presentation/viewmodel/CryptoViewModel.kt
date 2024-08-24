package com.example.binanceticker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.binanceticker.data.remote.NetworkResponse
import com.example.binanceticker.domain.model.CryptoCurrency
import com.example.binanceticker.domain.repository.CryptoRepository
import com.example.binanceticker.presentation.state.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CryptoViewModel @Inject constructor(
    private val repository: CryptoRepository
) : ViewModel() {

    private val _cryptoUIState = MutableStateFlow(UiState.Loading as UiState<List<CryptoCurrency>>)
    val cryptoUIState: StateFlow<UiState<List<CryptoCurrency>>> = _cryptoUIState

    fun fetchTop100CryptoData() {
        viewModelScope.launch {
            _cryptoUIState.value = UiState.Loading
            repository.getTop100Cryptos().collect { response ->
                _cryptoUIState.value = when (response) {
                    is NetworkResponse.Success -> UiState.Success(response.data)
                    is NetworkResponse.Error -> UiState.Error(response.errMessage)
                }
            }
        }
    }
}