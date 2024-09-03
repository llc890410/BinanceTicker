package com.example.binanceticker.domain.model

import com.tradingview.lightweightcharts.api.series.common.SeriesData
import com.tradingview.lightweightcharts.api.series.enums.SeriesType

data class ChartData(
    val list: List<SeriesData>,
    val type: SeriesType
)