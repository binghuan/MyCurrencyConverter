package com.bh.mycurrencyconverter.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// *** UNCOMMENT THE LINE BELOW FOR APPROOV ***
//import io.approov.service.retrofit.ApproovService

object OpenExchangeRateServiceInstance {

    private const val BASE_URL = "https://openexchangerates.org/"

    private var retrofit: Retrofit? = null

    @JvmStatic
    val retrofitInstance: Retrofit?
        get() {
            if (retrofit == null) {
                retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
            }
            return retrofit
        }
}




















