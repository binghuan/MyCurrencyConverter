package com.bh.mycurrencyconveter.persistence

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exchange_rates")
data class ExchangeRate(
    @PrimaryKey
    @ColumnInfo(name = "currency")
    val currency: String,
    @ColumnInfo(name = "usd_convertible_amount")
    val usdConvertibleAmount: Double,
    @ColumnInfo(name = "timestamp")
    val timestamp: Long
)