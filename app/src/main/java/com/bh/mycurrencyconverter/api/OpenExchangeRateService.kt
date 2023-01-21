package com.bh.mycurrencyconverter.api

import com.bh.mycurrencyconverter.vo.ExchangeRatesAPIResponse
import retrofit2.Call
import retrofit2.http.GET

interface OpenExchangeRateService {

    companion object {
        const val APP_ID = "a9b9c96f959b4a9396929dac01de540c"
    }

    @get:GET("/api/latest.json?app_id=$APP_ID&base=USD")
    val getExchangeRateInfo: Call<ExchangeRatesAPIResponse>

}









