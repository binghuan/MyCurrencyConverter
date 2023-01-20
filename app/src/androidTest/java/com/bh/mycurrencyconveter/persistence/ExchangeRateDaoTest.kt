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

package com.bh.mycurrencyconveter.persistence

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test the implementation of [UserDao]
 */
@RunWith(AndroidJUnit4::class)
class ExchangeRateDaoTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: ExchangeRateDatabase

    @Before
    fun initDb() {
        // using an in-memory database because the information stored here disappears after test
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            ExchangeRateDatabase::class.java
        )
            // allowing main thread queries, just for testing
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun getExchangeRateWhenNoDataInserted() {
        database.exchangeRateDao().getExchangeRateByCurrency("TWD")
            .test()
            .assertNoValues()
    }

    @Test
    fun insertAndGetExchangeRate() {
        // When inserting a new user in the data source
        database.exchangeRateDao().insertExchangeRate(EX_RATE).blockingAwait()

        // When subscribing to the emissions of the user
        database.exchangeRateDao().getExchangeRateByCurrency(EX_RATE.currency)
            .test()
            // assertValue asserts that there was only one emission of the user
            .assertValue { it.currency == EX_RATE.currency && it.usdConvertibleAmount == EX_RATE.usdConvertibleAmount }
    }

    @Test
    fun updateAndGetExchangeRate() {
        // Given that we have a user in the data source
        database.exchangeRateDao().insertExchangeRate(EX_RATE).blockingAwait()

        // When we are updating the name of the user
        val updatedExchangeRate = ExchangeRate(EX_RATE.currency, 31.0, 1674244524970)
        database.exchangeRateDao().insertExchangeRate(updatedExchangeRate).blockingAwait()

        // When subscribing to the emissions of the user
        database.exchangeRateDao().getExchangeRateByCurrency(EX_RATE.currency)
            .test()
            // assertValue asserts that there was only one emission of the user
            .assertValue { it.currency == EX_RATE.currency && it.usdConvertibleAmount == 31.0 }
    }

    @Test
    fun deleteAndGetExchangeRate() {
        // Given that we have a user in the data source
        database.exchangeRateDao().insertExchangeRate(EX_RATE).blockingAwait()

        //When we are deleting all users
        database.exchangeRateDao().deleteAllExchangeRates()
        // When subscribing to the emissions of the user
        database.exchangeRateDao().getExchangeRateByCurrency(EX_RATE.currency)
            .test()
            // check that there's no user emitted
            .assertNoValues()
    }

    companion object {
        private val EX_RATE = ExchangeRate("TWD", 30.0, 1674243912921)
    }
}
