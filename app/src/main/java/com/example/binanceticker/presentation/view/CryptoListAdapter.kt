package com.example.binanceticker.presentation.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.binanceticker.databinding.ItemCryptoBinding
import com.example.binanceticker.domain.model.SymbolQuoteData
import java.util.Locale

class CryptoListAdapter
    : ListAdapter<SymbolQuoteData, CryptoListAdapter.CryptoViewHolder>(CryptoDiffCallback()) {

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

        fun bind(data: SymbolQuoteData) {
            binding.textViewSymbol.text = data.symbol

            val formattedLastPrice = data.lastPrice.toBigDecimal().stripTrailingZeros().toPlainString()
            binding.textViewLastPrice.text = formattedLastPrice

            val formattedPercent = String.format(Locale.ROOT, "%.2f", data.priceChangePercent.toDouble()) + "%"
            binding.textViewPriceChangePercent.text = formattedPercent
        }
    }
}

class CryptoDiffCallback : DiffUtil.ItemCallback<SymbolQuoteData>() {
    override fun areItemsTheSame(oldItem: SymbolQuoteData, newItem: SymbolQuoteData): Boolean {
        return oldItem.symbol == newItem.symbol
    }

    override fun areContentsTheSame(oldItem: SymbolQuoteData, newItem: SymbolQuoteData): Boolean {
        return oldItem == newItem
    }
}
