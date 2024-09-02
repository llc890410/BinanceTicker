package com.example.binanceticker.utils

import com.example.binanceticker.domain.model.ApiTicker
import com.example.binanceticker.domain.model.KlineData
import com.example.binanceticker.domain.model.SymbolQuoteData
import com.example.binanceticker.domain.model.WebSocketKline
import com.example.binanceticker.domain.model.WebSocketTicker

fun ApiTicker.toSymbolQuoteData(): SymbolQuoteData {
    return SymbolQuoteData(
        symbol = symbol,
        priceChange = priceChange,
        priceChangePercent = priceChangePercent,
        lastPrice = lastPrice,
        volume = volume
    )
}

fun WebSocketTicker.toSymbolQuoteData(): SymbolQuoteData {
    return SymbolQuoteData(
        symbol = symbol,
        priceChange = priceChange,
        priceChangePercent = priceChangePercent,
        lastPrice = lastPrice,
        volume = totalTradedBaseAssetVolume
    )
}

fun WebSocketKline.toKlineData(): KlineData {
    return KlineData(
        openTime = startTime,
        openPrice = openPrice,
        highPrice = highPrice,
        lowPrice = lowPrice,
        closePrice = closePrice,
        volume = baseAssetVolume,
        closeTime = closeTime,
        quoteAssetVolume = quoteAssetVolume,
        numberOfTrades = numberOfTrades,
        takerBuyBaseAssetVolume = takerBuyBaseAssetVolume,
        takerBuyQuoteAssetVolume = takerBuyQuoteAssetVolume,
    )
}