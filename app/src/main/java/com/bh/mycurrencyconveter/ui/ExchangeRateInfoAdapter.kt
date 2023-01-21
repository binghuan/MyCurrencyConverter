package com.bh.mycurrencyconveter.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bh.mycurrencyconveter.R
import com.bh.mycurrencyconveter.vo.ExchangeRateItem


class ExchangeRateInfoAdapter(private val onClick: (ExchangeRateItem) -> Unit) :
    ListAdapter<ExchangeRateItem, ExchangeRateInfoAdapter.ViewHolder>(ItemDiffCallback) {

    /* ViewHolder for Item, takes in the inflated view and the onClick behavior. */
    class ViewHolder(itemView: View, val onClick: (ExchangeRateItem) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val textViewForBaseValue: TextView = itemView.findViewById(R.id.baseValue)
        private val textViewForBaseCurrency: TextView = itemView.findViewById(R.id.baseCurrency)
        private var currentItem: ExchangeRateItem? = null

        init {
            itemView.setOnClickListener {
                currentItem?.let {
                    onClick(it)
                }
            }
        }

        /* Bind currency and value. */
        fun bind(item: ExchangeRateItem) {
            currentItem = item
            textViewForBaseValue.text = item.value.toString()
            textViewForBaseCurrency.text = item.currency
        }
    }

    /* Creates and inflates view and return ItemViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_view, parent, false)
        return ViewHolder(view, onClick)
    }

    /* Gets current item and uses it to bind view. */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)

    }
}

object ItemDiffCallback : DiffUtil.ItemCallback<ExchangeRateItem>() {
    override fun areItemsTheSame(oldItem: ExchangeRateItem, newItem: ExchangeRateItem): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: ExchangeRateItem, newItem: ExchangeRateItem): Boolean {
        return oldItem.currency == newItem.currency
    }
}