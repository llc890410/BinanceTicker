package com.example.binanceticker.presentation.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.binanceticker.databinding.ItemCryptoBinding
import com.example.binanceticker.domain.model.CryptoCurrency
import java.util.Locale

class CryptoListAdapter
    : ListAdapter<CryptoCurrency, CryptoListAdapter.CryptoViewHolder>(CryptoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CryptoViewHolder {
        val binding = ItemCryptoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CryptoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CryptoViewHolder, position: Int) {
        val crypto = getItem(position)
        holder.bind(crypto)
    }

    class CryptoViewHolder(private val binding: ItemCryptoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(crypto: CryptoCurrency) {
            binding.textViewSymbol.text = crypto.symbol

            val formattedLastPrice = crypto.lastPrice.toBigDecimal().stripTrailingZeros().toPlainString()
            binding.textViewLastPrice.text = formattedLastPrice

            val formattedPercent = String.format(Locale.ROOT, "%.2f", crypto.priceChangePercent.toDouble()) + "%"
            binding.textViewPriceChangePercent.text = formattedPercent
        }
    }
}

class CryptoDiffCallback : DiffUtil.ItemCallback<CryptoCurrency>() {
    override fun areItemsTheSame(oldItem: CryptoCurrency, newItem: CryptoCurrency): Boolean {
        return oldItem.symbol == newItem.symbol
    }

    override fun areContentsTheSame(oldItem: CryptoCurrency, newItem: CryptoCurrency): Boolean {
        return oldItem == newItem
    }
}
