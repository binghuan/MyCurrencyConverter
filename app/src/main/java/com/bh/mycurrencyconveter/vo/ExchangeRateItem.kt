package com.bh.mycurrencyconveter.vo

import com.google.gson.annotations.SerializedName

data class ExchangeRateItem(
    val currency: String,
    val value: Double
)
