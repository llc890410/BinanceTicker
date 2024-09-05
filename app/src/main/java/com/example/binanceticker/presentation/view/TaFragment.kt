package com.example.binanceticker.presentation.view

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
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
import com.tradingview.lightweightcharts.api.options.models.gridLineOptions
import com.tradingview.lightweightcharts.api.options.models.gridOptions
import com.tradingview.lightweightcharts.api.options.models.layoutOptions
import com.tradingview.lightweightcharts.api.options.models.priceScaleMargins
import com.tradingview.lightweightcharts.api.options.models.priceScaleOptions
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
        binding.progressBar.visibility = View.VISIBLE
        binding.chartsView.visibility = View.INVISIBLE

        binding.chartsView.subscribeOnChartStateChange { state ->
            when (state) {
                is ChartsView.State.Preparing -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.chartsView.visibility = View.INVISIBLE
                }
                is ChartsView.State.Ready -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.chartsView.visibility = View.VISIBLE
                }
                is ChartsView.State.Error -> {
                    binding.progressBar.visibility = View.INVISIBLE
                    binding.chartsView.visibility = View.VISIBLE
                    Timber.e("Chart error: ${state.exception}")
                }
            }
        }

        chartApi.applyChartOptions()
    }

    private fun ChartApi.applyChartOptions() = applyOptions {
        layout = layoutOptions {
            background = SolidColor(Color.parseColor("#131722").toIntColor())
            textColor = Color.parseColor("#d1d4dc").toIntColor()
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
                color = Color.argb(0, 42, 46, 57).toIntColor()
            }
            horzLines = gridLineOptions {
                color = Color.argb(153, 42, 46, 57).toIntColor()
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
            candlestickSeries?.let {
                chartApi.removeSeries(it)
            }
            addCandleSeries(barData)
        }

        viewModel.seriesHistogramData.observe(viewLifecycleOwner) { histogramData ->
            histogramSeries?.let {
                chartApi.removeSeries(it)
            }
            addHistogramSeries(histogramData)
        }
    }

    private fun addCandleSeries(barData: ChartData) {
        chartApi.addCandlestickSeries(
            options = candlestickSeriesOptions {
                priceScaleId = PriceScaleId.RIGHT
                upColor = Color.argb(204, 0, 150, 136).toIntColor()
                downColor = Color.argb(204, 255, 82, 82).toIntColor()
            },
            onSeriesCreated = { series ->
                candlestickSeries = series
                series.setData(barData.list)
            }
        )
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}