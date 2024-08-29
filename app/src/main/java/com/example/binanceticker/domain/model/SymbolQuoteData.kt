package com.example.binanceticker.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SymbolQuoteData(
    val symbol: String,
    val priceChange: String,
    val priceChangePercent: String,
    val lastPrice: String,
    val volume: String
) : Parcelable

