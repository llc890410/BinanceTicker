package com.example.binanceticker.domain.model

data class SymbolQuoteData(
    val symbol: String,
    val priceChange: String,
    val priceChangePercent: String,
    val lastPrice: String,
    val volume: String
)

