package com.example.binanceticker.presentation.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.binanceticker.databinding.FragmentTaBinding
import com.example.binanceticker.presentation.viewmodel.TaViewModel
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

@AndroidEntryPoint
class TaFragment : Fragment() {

    private var _binding: FragmentTaBinding? = null
    private val binding get() = _binding!!

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
        val symbolQuoteData = arguments?.let {
            TaFragmentArgs.fromBundle(it).symbolQuoteData
        }

        symbolQuoteData?.let {
            binding.textViewSymbol.text = it.symbol
            val formattedLastPrice = it.lastPrice.toBigDecimal().stripTrailingZeros().toPlainString()
            binding.textViewLastPrice.text = formattedLastPrice
            val formattedPercent = String.format(Locale.ROOT, "%.2f", it.priceChangePercent.toDouble()) + "%"
            binding.textViewPriceChangePercent.text = formattedPercent

            viewModel.getKlines(it.symbol, "1m")
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}