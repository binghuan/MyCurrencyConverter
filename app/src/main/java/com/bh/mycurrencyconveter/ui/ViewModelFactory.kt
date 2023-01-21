package com.bh.mycurrencyconveter.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bh.mycurrencyconveter.persistence.ExchangeRateDao
import com.bh.mycurrencyconveter.viewmodel.MainViewModel

/**
 * Factory for ViewModels
 */
class ViewModelFactory(private val dataSource: ExchangeRateDao) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(dataSource) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
