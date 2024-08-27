package com.example.binanceticker.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WebSocketTicker(
    @SerialName("e") val eventType: String,
    @SerialName("E") val eventTime: Long,
    @SerialName("s") val symbol: String,
    @SerialName("p") val priceChange: String,
    @SerialName("P") val priceChangePercent: String,
    @SerialName("w") val weightedAvgPrice: String,
    @SerialName("x") val firstTradeBeforeWindow: String,
    @SerialName("c") val lastPrice: String,
    @SerialName("Q") val lastQuantity: String,
    @SerialName("b") val bestBidPrice: String,
    @SerialName("B") val bestBidQuantity: String,
    @SerialName("a") val bestAskPrice: String,
    @SerialName("A") val bestAskQuantity: String,
    @SerialName("o") val openPrice: String,
    @SerialName("h") val highPrice: String,
    @SerialName("l") val lowPrice: String,
    @SerialName("v") val totalTradedBaseAssetVolume: String,
    @SerialName("q") val totalTradedQuoteAssetVolume: String,
    @SerialName("O") val statisticsOpenTime: Long,
    @SerialName("C") val statisticsCloseTime: Long,
    @SerialName("F") val firstTradeId: Long,
    @SerialName("L") val lastTradeId: Long,
    @SerialName("n") val totalNumberOfTrades: Long
)