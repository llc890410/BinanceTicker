package com.example.binanceticker.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CryptoCurrency(
    val symbol: String,
    val quoteVolume: String
)