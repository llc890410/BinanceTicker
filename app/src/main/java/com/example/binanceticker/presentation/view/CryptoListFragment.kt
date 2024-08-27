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
import com.example.binanceticker.presentation.state.UiState
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
                viewModel.cryptoUIState.collect { uiState ->
                    when (uiState) {
                        is UiState.Loading -> {
                            binding.progressBar.visibility = View.VISIBLE
                            binding.recyclerViewCryptoList.visibility = View.GONE
                        }
                        is UiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            binding.recyclerViewCryptoList.visibility = View.VISIBLE
                            cryptoListAdapter.submitList(uiState.data)
                        }
                        is UiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            binding.recyclerViewCryptoList.visibility = View.GONE
                            Timber.e(uiState.message)
                        }
                    }
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.startTrackingSymbols()
                viewModel.tickerStream.collect { (symbol, ticker) ->
                    Timber.d("Symbol: $symbol, Ticker: $ticker")
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.stopTrackingSymbols()
    }
}