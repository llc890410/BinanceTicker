package com.example.binanceticker.presentation.view

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.binanceticker.databinding.ActivityMainBinding
import com.example.binanceticker.domain.model.CryptoCurrency
import com.example.binanceticker.presentation.viewmodel.CryptoViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: CryptoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        fetchTop100CryptoData()
    }

    private fun fetchTop100CryptoData() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.fetchTop100CryptoData()
                viewModel.top100CryptoList.collect { cryptos ->
                    Timber.d("Fetched %d cryptocurrencies", cryptos.size)
                    cryptos.forEach { crypto ->
                        Timber.d("Symbol: %s, Volume: %s", crypto.symbol, crypto.quoteVolume)
                    }
                    updateTextView(cryptos)
                }
            }
        }
    }

    private fun updateTextView(cryptos: List<CryptoCurrency>) {
        val symbols = cryptos.joinToString(separator = "\n") { it.symbol }
        binding.textViewCryptoSymbols.text = symbols
    }

}