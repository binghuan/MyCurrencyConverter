package com.bh.mycurrencyconverter

import android.content.Context
import com.bh.mycurrencyconverter.persistence.ExchangeRateDao
import com.bh.mycurrencyconverter.persistence.ExchangeRateDatabase
import com.bh.mycurrencyconverter.ui.ViewModelFactory

/**
 * Enables injection of data sources.
 */
object Injection {

    private fun provideOpenExchangeRateDataSource(context: Context): ExchangeRateDao {
        val database = ExchangeRateDatabase.getInstance(context)
        return database.exchangeRateDao()
    }

    fun provideViewModelFactory(context: Context): ViewModelFactory {
        val dataSource = provideOpenExchangeRateDataSource(context)
        return ViewModelFactory(dataSource)
    }
}
