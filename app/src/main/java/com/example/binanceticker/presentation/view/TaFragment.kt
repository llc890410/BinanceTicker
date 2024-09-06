package com.example.binanceticker.presentation.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.example.binanceticker.R
import com.example.binanceticker.databinding.FragmentTaBinding
import com.example.binanceticker.domain.model.ChartData
import com.example.binanceticker.presentation.viewmodel.TaViewModel
import com.tradingview.lightweightcharts.api.chart.models.color.surface.SolidColor
import com.tradingview.lightweightcharts.api.chart.models.color.toIntColor
import com.tradingview.lightweightcharts.api.interfaces.ChartApi
import com.tradingview.lightweightcharts.api.interfaces.SeriesApi
import com.tradingview.lightweightcharts.api.options.models.HistogramSeriesOptions
import com.tradingview.lightweightcharts.api.options.models.PriceScaleMargins
import com.tradingview.lightweightcharts.api.options.models.PriceScaleOptions
import com.tradingview.lightweightcharts.api.options.models.candlestickSeriesOptions
import com.tradingview.lightweightcharts.api.options.models.crosshairOptions
import com.tradingview.lightweightcharts.api.options.models.gridLineOptions
import com.tradingview.lightweightcharts.api.options.models.gridOptions
import com.tradingview.lightweightcharts.api.options.models.layoutOptions
import com.tradingview.lightweightcharts.api.options.models.priceScaleMargins
import com.tradingview.lightweightcharts.api.options.models.priceScaleOptions
import com.tradingview.lightweightcharts.api.series.enums.CrosshairMode
import com.tradingview.lightweightcharts.api.series.models.BarPrice
import com.tradingview.lightweightcharts.api.series.models.BarPrices
import com.tradingview.lightweightcharts.api.series.models.PriceFormat
import com.tradingview.lightweightcharts.api.series.models.PriceScaleId
import com.tradingview.lightweightcharts.view.ChartsView
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.Locale

@AndroidEntryPoint
class TaFragment : Fragment() {

    private var _binding: FragmentTaBinding? = null
    private val binding get() = _binding!!

    private val chartApi get() = binding.chartsView.api
    private var candlestickSeries: SeriesApi? = null
    private var histogramSeries: SeriesApi? = null

    private val viewModel: TaViewModel by viewModels()

    private val upColor by lazy { ContextCompat.getColor(requireContext(), R.color.chart_up_color) }
    private val downColor by lazy { ContextCompat.getColor(requireContext(), R.color.chart_down_color) }
    private val whiteColor by lazy { ContextCompat.getColor(requireContext(), R.color.chart_white_color) }
    private val chartBackgroundColor by lazy { ContextCompat.getColor(requireContext(), R.color.chart_background_color) }
    private val chartVerticalGridColor by lazy { ContextCompat.getColor(requireContext(), R.color.chart_vertical_grid_color) }
    private val chartHorizontalGridColor by lazy { ContextCompat.getColor(requireContext(), R.color.chart_horizontal_grid_color) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupChartView()
        observeViewModelData()

        arguments?.let {
            val symbolQuoteData = TaFragmentArgs.fromBundle(it).symbolQuoteData
            symbolQuoteData.let { data ->
                viewModel.init(data)
            }
        }
    }

    private fun setupChartView() {
        updateChartViewVisibility(isLoading = true)
        binding.clBarInfo.visibility = View.INVISIBLE

        binding.chartsView.subscribeOnChartStateChange { state ->
            when (state) {
                is ChartsView.State.Preparing -> updateChartViewVisibility(isLoading = true)
                is ChartsView.State.Ready -> updateChartViewVisibility(isLoading = false)
                is ChartsView.State.Error -> {
                    updateChartViewVisibility(isLoading = false)
                    Timber.e("Chart error: ${state.exception}")
                }
            }
        }

        chartApi.subscribeCrosshairMove { param ->
            if (param.point == null) { //用這個判斷是否進入查價模式
                binding.clBarInfo.visibility = View.INVISIBLE
            } else {
                binding.clBarInfo.visibility = View.VISIBLE
            }
            param.seriesData?.let { data ->
                if (data.isEmpty()) {
                    clearPriceInfo()
                } else {
                    updatePriceInfo(data)
                }
            }
        }

        chartApi.applyChartOptions()
    }

    private fun updateChartViewVisibility(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.INVISIBLE
        binding.chartsView.visibility = if (isLoading) View.INVISIBLE else View.VISIBLE
    }

    private fun clearPriceInfo() {
        listOf(binding.tvOpen, binding.tvHigh, binding.tvLow, binding.tvClose, binding.tvVolume).forEach {
            it.text = "Ø"
            it.setTextColor(whiteColor)
        }
    }

    private fun updatePriceInfo(data: List<BarPrices>) {
        val barPrice = data.first().prices
        binding.tvOpen.text = barPrice.open?.toString() ?: ""
        binding.tvHigh.text = barPrice.high?.toString() ?: ""
        binding.tvLow.text = barPrice.low?.toString() ?: ""
        binding.tvClose.text = barPrice.close?.toString() ?: ""

        if (data.size >= 2) {
            binding.tvVolume.text = data[1].prices.value?.toString() ?: ""
        }

        val textColor = getPriceTextColor(barPrice)
        listOf(binding.tvOpen, binding.tvHigh, binding.tvLow, binding.tvClose, binding.tvVolume).forEach {
            it.setTextColor(textColor)
        }
    }

    private fun getPriceTextColor(barPrice: BarPrice): Int {
        return when {
            barPrice.close != null && barPrice.open != null -> {
                when {
                    barPrice.close!! > barPrice.open!! -> upColor
                    barPrice.close!! < barPrice.open!! -> downColor
                    else -> whiteColor
                }
            }
            else -> whiteColor
        }
    }

    private fun ChartApi.applyChartOptions() = applyOptions {
        crosshair = crosshairOptions {
            mode = CrosshairMode.NORMAL // 普通模式 不會自動對齊K線
        }
        layout = layoutOptions {
            background = SolidColor(chartBackgroundColor.toIntColor())
            textColor = whiteColor.toIntColor()
        }
        rightPriceScale = priceScaleOptions {
            scaleMargins = priceScaleMargins {
                top = 0.3f
                bottom = 0.25f
            }
            borderVisible = false
        }
        grid = gridOptions {
            vertLines = gridLineOptions {
                color = chartVerticalGridColor.toIntColor()
            }
            horzLines = gridLineOptions {
                color = chartHorizontalGridColor.toIntColor()
            }
        }
    }

    private fun observeViewModelData() = viewModel.run {
        viewModel.cryptoData.observe(viewLifecycleOwner) { symbolQuoteData ->
            binding.textViewSymbol.text = symbolQuoteData.symbol
            binding.textViewLastPrice.text = symbolQuoteData.lastPrice.toBigDecimal().stripTrailingZeros().toPlainString()
            binding.textViewPriceChangePercent.text = String.format(Locale.ROOT, "%.2f%%", symbolQuoteData.priceChangePercent.toDouble())
        }

        viewModel.seriesBarData.observe(viewLifecycleOwner) { barData ->
            if (candlestickSeries == null) {
                addCandleSeries(barData)
            } else {
                updateCandleSeries(barData)
            }
        }

        viewModel.seriesHistogramData.observe(viewLifecycleOwner) { histogramData ->
            if (histogramSeries == null) {
                addHistogramSeries(histogramData)
            } else {
                updateHistogramSeries(histogramData)
            }
        }
    }

    private fun addCandleSeries(barData: ChartData) {
        chartApi.addCandlestickSeries(
            options = candlestickSeriesOptions {
                priceScaleId = PriceScaleId.RIGHT
                upColor = this@TaFragment.upColor.toIntColor()
                downColor = this@TaFragment.downColor.toIntColor()
            },
            onSeriesCreated = { series ->
                candlestickSeries = series
                series.setData(barData.list)
            }
        )
    }

    private fun updateCandleSeries(barData: ChartData) {
        val lastBar = barData.list.lastOrNull()
        lastBar?.let {
            candlestickSeries?.update(it)
        }
    }

    private fun addHistogramSeries(histogramData: ChartData) {
        chartApi.addHistogramSeries(
            options = HistogramSeriesOptions(
                priceFormat = PriceFormat.priceFormatBuiltIn(
                    type = PriceFormat.Type.VOLUME,
                    precision = 1,
                    minMove = 1f,
                ),
                priceScaleId = PriceScaleId(""),
            ),
            onSeriesCreated = { series ->
                series.priceScale().applyOptions(PriceScaleOptions().apply {
                    scaleMargins = PriceScaleMargins(
                        top = 0.8f,
                        bottom = 0f,
                    )
                })
                histogramSeries = series
                series.setData(histogramData.list)
            }
        )
    }

    private fun updateHistogramSeries(histogramData: ChartData) {
        val lastBar = histogramData.list.lastOrNull()
        lastBar?.let {
            histogramSeries?.update(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}