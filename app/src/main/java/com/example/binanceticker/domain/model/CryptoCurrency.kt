package com.example.binanceticker.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class CryptoCurrency(
    val symbol: String,
    val priceChange: String,
    val priceChangePercent: String,
    val lastPrice: String,
    val quoteVolume: String,
)