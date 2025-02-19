package com.bh.mycurrencyconverter.api

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.bh.mycurrencyconverter.vo.ExchangeRatesAPIResponse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Call
import retrofit2.Retrofit

@RunWith(JUnit4::class)
class OpenExchangeRateServiceTest {
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()



    @Test
    fun testOpenExchangeRateService() {
        //Get an instance of PlacesService by providing the Retrofit instance
        val service = OpenExchangeRateServiceInstance.retrofitInstance!!.create(
            OpenExchangeRateService::class.java
        )

        //Create a new request for our API calling
        val call: Call<ExchangeRatesAPIResponse> = service.getExchangeRateInfo

        //Execute the API call
        val response = call.execute()

        //Check for error body
        val errorBody = response.errorBody()
        assert(errorBody == null)

        //Check for success body
        val responseWrapper = response.body()
        assert(responseWrapper != null)
        assert(response.code() == 200)
    }
}
