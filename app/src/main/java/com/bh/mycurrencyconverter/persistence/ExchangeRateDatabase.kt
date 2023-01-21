package com.bh.mycurrencyconverter.persistence

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

/**
 * The Room database that contains the Exchange Rates table
 */
@Database(entities = [ExchangeRate::class], version = 1)
abstract class ExchangeRateDatabase : RoomDatabase() {

    abstract fun exchangeRateDao(): ExchangeRateDao

    companion object {

        @Volatile
        private var INSTANCE: ExchangeRateDatabase? = null

        fun getInstance(context: Context): ExchangeRateDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext, ExchangeRateDatabase::class.java, "Sample.db"
        ).build()
    }
}
