package com.bh.mycurrencyconveter.vo

import com.google.gson.annotations.SerializedName

/*
    ## Sample of response
    {
        "disclaimer": "Usage subject to terms: https://openexchangerates.org/terms",
        "license": "https://openexchangerates.org/license",
        "timestamp": 1674064800,
        "base": "USD",
        "rates": {
            "AED": 3.67299,
            "AFN": 88.500008,
            ...
        }
    }
 */
data class ExchangeRatesAPIResponse(
    @field:SerializedName("disclaimer")
    val disclaimer: String,
    @field:SerializedName("license")
    val license: String,
    @field:SerializedName("timestamp")
    val timestamp: Long?,
    @field:SerializedName("base")
    val base: String?,
    @field:SerializedName("rates")
    val rates: HashMap<String, Double>?,
)
