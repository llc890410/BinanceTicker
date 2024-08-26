package com.example.binanceticker.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CryptoCurrency(
    @SerialName("symbol")               val symbol: String,
    @SerialName("priceChange")          val priceChange: String,
    @SerialName("priceChangePercent")   val priceChangePercent: String,
    @SerialName("weightedAvgPrice")     val weightedAvgPrice: String,
    @SerialName("prevClosePrice")       val prevClosePrice: String,
    @SerialName("lastPrice")            val lastPrice: String,
    @SerialName("lastQty")              val lastQty: String,
    @SerialName("bidPrice")             val bidPrice: String,
    @SerialName("bidQty")               val bidQty: String,
    @SerialName("askPrice")             val askPrice: String,
    @SerialName("askQty")               val askQty: String,
    @SerialName("openPrice")            val openPrice: String,
    @SerialName("highPrice")            val highPrice: String,
    @SerialName("lowPrice")             val lowPrice: String,
    @SerialName("volume")               val volume: String,
    @SerialName("quoteVolume")          val quoteVolume: String,
    @SerialName("openTime")             val openTime: Long,
    @SerialName("closeTime")            val closeTime: Long,
    @SerialName("firstId")               val firstId: Long,
    @SerialName("lastId")               val lastId: Long,
    @SerialName("count")                val count: Long
)