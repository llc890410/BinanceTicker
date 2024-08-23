package com.example.binanceticker.presentation.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.binanceticker.databinding.FragmentCryptoListBinding
import com.example.binanceticker.presentation.viewmodel.CryptoViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class CryptoListFragment : Fragment() {

    private var _binding: FragmentCryptoListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CryptoViewModel by viewModels()
    private lateinit var cryptoListAdapter: CryptoListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCryptoListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeCryptoList()
    }

    private fun setupRecyclerView() {
        cryptoListAdapter = CryptoListAdapter()
        binding.recyclerViewCryptoList.apply {
            adapter = cryptoListAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeCryptoList() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fetchTop100CryptoData()
                viewModel.top100CryptoList.collect { cryptos ->
                    cryptoListAdapter.submitList(cryptos)
                    cryptos.forEach { crypto ->
                        Timber.d(
                            "Symbol: %s, Price Change: %s, Price Change Percent: %s, Last Price: %s, Quote Volume: %s",
                            crypto.symbol,
                            crypto.priceChange,
                            crypto.priceChangePercent,
                            crypto.lastPrice,
                            crypto.quoteVolume
                        )
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}