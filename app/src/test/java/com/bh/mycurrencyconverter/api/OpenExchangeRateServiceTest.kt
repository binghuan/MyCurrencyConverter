/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    fun testRetrofitInstance() {
        //Get an instance of Retrofit
        val instance: Retrofit? = OpenExchangeRateServiceInstance.retrofitInstance
        val baseUrl = instance?.baseUrl()?.url().toString()
        assert(baseUrl == OpenExchangeRateServiceInstance.BASE_URL)
    }

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
