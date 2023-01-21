package com.bh.mycurrencyconveter

import android.content.Context
import com.bh.mycurrencyconveter.persistence.ExchangeRateDao
import com.bh.mycurrencyconveter.persistence.ExchangeRateDatabase
import com.bh.mycurrencyconveter.ui.ViewModelFactory

/**
 * Enables injection of data sources.
 */
object Injection {

    private fun provideUserDataSource(context: Context): ExchangeRateDao {
        val database = ExchangeRateDatabase.getInstance(context)
        return database.exchangeRateDao()
    }

    fun provideViewModelFactory(context: Context): ViewModelFactory {
        val dataSource = provideUserDataSource(context)
        return ViewModelFactory(dataSource)
    }
}
