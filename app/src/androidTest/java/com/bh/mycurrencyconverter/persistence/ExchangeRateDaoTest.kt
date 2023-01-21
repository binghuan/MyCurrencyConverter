package com.bh.mycurrencyconverter.persistence

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
 * Test the implementation of [ExchangeRateDao]
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
    fun insertMultipleExchangeRatesAndGetThemBack() {

        val dataList = ArrayList<ExchangeRate>()
        dataList.add(
            ExchangeRate(
                currency = "TWD",
                usdConvertibleAmount = 30.0,
                timestamp = 1674290532064,
                lastFetchTime = 1674290532064,
            )
        )
        dataList.add(
            ExchangeRate(
                currency = "EUR",
                usdConvertibleAmount = 0.92,
                timestamp = 1674290532064,
                lastFetchTime = 1674290532064,
            )
        )

        // When inserting list of exchange rates in the data source
        database.exchangeRateDao().insertExchangeRateList(dataList).blockingAwait()

        // When subscribing to the emissions of the exchange rate
        database.exchangeRateDao().getAllExchangeRates()
            .test()
            // assertValue asserts that there was only one emission of the exchange rate
            .assertValue {
                (it[0].currency == "TWD" || it[0].currency == "EUR") &&
                        (it[1].currency == "TWD" || it[1].currency == "EUR")
            }
    }

    @Test
    fun insertAndGetExchangeRate() {
        // When inserting a new data in the data source
        database.exchangeRateDao().insertExchangeRate(EX_RATE).blockingAwait()

        // When subscribing to the emissions of the exchange rate
        database.exchangeRateDao().getExchangeRateByCurrency(EX_RATE.currency)
            .test()
            // assertValue asserts that there was only one emission of the exchange rate
            .assertValue { it.currency == EX_RATE.currency && it.usdConvertibleAmount == EX_RATE.usdConvertibleAmount }
    }

    @Test
    fun updateAndGetExchangeRate() {
        // Given that we have a exchange rate in the data source
        database.exchangeRateDao().insertExchangeRate(EX_RATE).blockingAwait()

        // When we are updating the usdConvertibleAmount of the exchange rate
        val updatedExchangeRate = ExchangeRate(
            currency = EX_RATE.currency,
            usdConvertibleAmount = 31.0,
            timestamp = 1674244524970,
            lastFetchTime = 1674244524970,
        )
        database.exchangeRateDao().insertExchangeRate(updatedExchangeRate).blockingAwait()

        // When subscribing to the emissions of the exchange rate
        database.exchangeRateDao().getExchangeRateByCurrency(EX_RATE.currency)
            .test()
            // assertValue asserts that there was only one emission of the exchange rate
            .assertValue { it.currency == EX_RATE.currency && it.usdConvertibleAmount == 31.0 }
    }

    @Test
    fun deleteAndGetExchangeRate() {
        // Given that we have a exchange rate in the data source
        database.exchangeRateDao().insertExchangeRate(EX_RATE).blockingAwait()

        //When we are deleting all exchange rates
        database.exchangeRateDao().deleteAllExchangeRates()
        // When subscribing to the emissions of the exchange rate
        database.exchangeRateDao().getExchangeRateByCurrency(EX_RATE.currency)
            .test()
            // check that there's no exchange rate emitted
            .assertNoValues()
    }

    companion object {
        private val EX_RATE = ExchangeRate(
            currency = "TWD",
            usdConvertibleAmount = 30.0,
            timestamp = 1674243912921,
            lastFetchTime = 1674243912921,
        )
    }
}
