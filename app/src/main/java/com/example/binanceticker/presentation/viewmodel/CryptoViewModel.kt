package com.example.binanceticker.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.binanceticker.domain.model.CryptoCurrency
import com.example.binanceticker.domain.repository.CryptoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CryptoViewModel @Inject constructor(
    private val repository: CryptoRepository
) : ViewModel() {

    private val _top100CryptoList = MutableStateFlow<List<CryptoCurrency>>(emptyList())
    val top100CryptoList: StateFlow<List<CryptoCurrency>> = _top100CryptoList

    fun fetchTop100CryptoData() {
        viewModelScope.launch {
            repository.getTop100Cryptos().collect { cryptos ->
                _top100CryptoList.value = cryptos
            }
        }
    }
}