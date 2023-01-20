package com.bh.mycurrencyconveter.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable

import io.reactivex.Flowable

/**
 * Data Access Object for the users table.
 */
@Dao
interface ExchangeRateDao {

    /**
     * Get all exchange rates
     * @return the exchange rates from the table
     */
    @Query("SELECT * FROM exchange_rates")
    fun getAllExchangeRates(): Flowable<List<ExchangeRate>>

    /**
     * Get exchange rate by currency.
     * @return the exchangeRate from the table with a specific currency.
     */
    @Query("SELECT * FROM exchange_rates WHERE currency = :currency")
    fun getExchangeRateByCurrency(currency: String): Flowable<ExchangeRate>

    /**
     * Insert an exchange rate in the database. If the currency already exists, replace it.
     * @param exchangeRate the user to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExchangeRate(exchangeRate: ExchangeRate): Completable

    /**
     * Insert exchange rates in the database. If the currency already exists, replace it.
     * @param exchangeRates the list of data to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExchangeRateList(exchangeRates: List<ExchangeRate>): Completable

    /**
     * Delete all users.
     */
    @Query("DELETE FROM exchange_rates")
    fun deleteAllExchangeRates()
}
