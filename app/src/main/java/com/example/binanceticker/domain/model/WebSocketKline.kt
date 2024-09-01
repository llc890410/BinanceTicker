package com.example.binanceticker.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WebSocketKline(
    @SerialName("t") val startTime: Long,
    @SerialName("T") val closeTime: Long,
    @SerialName("s") val symbol: String,
    @SerialName("i") val interval: String,
    @SerialName("f") val firstTradeId: Long,
    @SerialName("L") val lastTradeId: Long,
    @SerialName("o") val openPrice: String,
    @SerialName("c") val closePrice: String,
    @SerialName("h") val highPrice: String,
    @SerialName("l") val lowPrice: String,
    @SerialName("v") val baseAssetVolume: String,
    @SerialName("n") val numberOfTrades: Long,
    @SerialName("x") val isClosed: Boolean,
    @SerialName("q") val quoteAssetVolume: String,
    @SerialName("V") val takerBuyBaseAssetVolume: String,
    @SerialName("Q") val takerBuyQuoteAssetVolume: String,
    @SerialName("B") val ignore: String
)