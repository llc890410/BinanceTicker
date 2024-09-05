package com.example.binanceticker.utils

import android.graphics.Color
import com.example.binanceticker.domain.model.ApiTicker
import com.example.binanceticker.domain.model.KlineData
import com.example.binanceticker.domain.model.SymbolQuoteData
import com.example.binanceticker.domain.model.WebSocketKline
import com.example.binanceticker.domain.model.WebSocketTicker
import com.tradingview.lightweightcharts.api.chart.models.color.toIntColor
import com.tradingview.lightweightcharts.api.series.models.BarData
import com.tradingview.lightweightcharts.api.series.models.HistogramData
import com.tradingview.lightweightcharts.api.series.models.Time
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
        symbol = symbol,
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

fun KlineData.toBarData(): BarData {
    return BarData(
        time = Time.StringTime(convertTimestampToUTC(openTime), Locale.US),
        open = openPrice.toFloat(),
        high = highPrice.toFloat(),
        low = lowPrice.toFloat(),
        close = closePrice.toFloat()
    )
}

fun KlineData.toHistogramData(): HistogramData {
    val upColor = Color.argb(204, 0, 150, 136).toIntColor()
    val downColor = Color.argb(204, 255, 82, 82).toIntColor()
    return HistogramData(
        time = Time.StringTime(convertTimestampToUTC(openTime), Locale.US),
        value = volume.toFloat(),
        color = if (closePrice > openPrice) upColor else downColor
    )
}

private val utcDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

fun convertTimestampToUTC(timeInMillis: Long): String {
    val date = Date(timeInMillis)
    return utcDateFormat.format(date)
}