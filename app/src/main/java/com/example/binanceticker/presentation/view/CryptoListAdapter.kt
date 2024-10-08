package com.example.binanceticker.presentation.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.binanceticker.R
import com.example.binanceticker.databinding.ItemCryptoBinding
import com.example.binanceticker.domain.model.SymbolQuoteData
import java.util.Locale

class CryptoListAdapter(
    private val onItemClicked: (SymbolQuoteData) -> Unit
) : ListAdapter<SymbolQuoteData, CryptoListAdapter.CryptoViewHolder>(CryptoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CryptoViewHolder {
        val binding = ItemCryptoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CryptoViewHolder(binding, onItemClicked)
    }

    override fun onBindViewHolder(holder: CryptoViewHolder, position: Int) {
        val crypto = getItem(position)
        holder.bind(crypto)
    }

    class CryptoViewHolder(
        private val binding: ItemCryptoBinding,
        private val onItemClicked: (SymbolQuoteData) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(data: SymbolQuoteData) {
            binding.tvSymbol.text = data.symbol

            val formattedLastPrice = data.lastPrice.toBigDecimal().stripTrailingZeros().toPlainString()
            binding.tvLastPrice.text = formattedLastPrice

            val formattedPercent = String.format(Locale.ROOT, "%.2f", data.priceChangePercent.toDouble()) + "%"
            binding.tvPriceChangePercent.text = formattedPercent

            val colorRes = when {
                data.priceChangePercent.toDouble() > 0 -> R.color.chart_up_color
                data.priceChangePercent.toDouble() < 0 -> R.color.chart_down_color
                else -> R.color.chart_white_color
            }
            binding.tvLastPrice.setTextColor(
                ContextCompat.getColor(binding.root.context, colorRes)
            )
            binding.tvPriceChangePercent.setTextColor(
                ContextCompat.getColor(binding.root.context, colorRes)
            )

            binding.root.setOnClickListener {
                onItemClicked(data)
            }
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
