package com.bh.mycurrencyconverter.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bh.mycurrencyconverter.api.OpenExchangeRateService
import com.bh.mycurrencyconverter.persistence.ExchangeRate
import com.bh.mycurrencyconverter.persistence.ExchangeRateDao
import com.bh.mycurrencyconverter.api.OpenExchangeRateServiceInstance
import com.bh.mycurrencyconverter.util.RateLimiter
import com.bh.mycurrencyconverter.vo.ExchangeRateItem
import com.bh.mycurrencyconverter.vo.ExchangeRatesAPIResponse
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.TimeUnit

class MainViewModel(private val dataSource: ExchangeRateDao) : ViewModel() {

    companion object {
        private val TAG = "BH_Lin_${MainViewModel::class.java.simpleName}"
        const val DEFAULT_BASE_CURRENCY = "USD"
        const val TIMEOUT_TO_FETCH_DATA_IN_MINUTES = 30
    }

    private var _positionForUSD: Int = 0
    private var baseCurrency = MutableLiveData(DEFAULT_BASE_CURRENCY)
    var exchangeRates = MutableLiveData<Map<String, Double>>()
    var calculatedExchangeRates = MutableLiveData<ArrayList<ExchangeRateItem>>()

    var currencyList = MutableLiveData<ArrayList<String>>()

    private fun getExchangeRates(): Map<String, Double>? {
        return exchangeRates.value
    }

    fun getCurrencyFromItemPosition(position: Int): String {
        val currencies = getCurrencyList()
        return if (currencies != null) {
            currencies[position]
        } else {
            ""
        }
    }

    fun getItemPositionTargetCurrency(
        currencies: List<String>, targetCurrency: String
    ): Int {
        for ((index, value) in currencies.withIndex()) {
            if (value == targetCurrency) {
                return index
            }
        }
        return 0
    }

    fun getItemPositionForUSD(): Int {
        return _positionForUSD
    }

    fun setExchangeRates(rates: HashMap<String, Double>, timestamp: Long?, lastFetchTime: Long) {
        exchangeRates.postValue(rates)

        val dataList = ArrayList<ExchangeRate>()
        for ((currency, amount) in rates) {
            dataList.add(
                ExchangeRate(
                    currency = currency,
                    usdConvertibleAmount = amount,
                    timestamp = timestamp ?: -1,
                    lastFetchTime = lastFetchTime
                )
            )
        }

        insertExchangeRatesIntoDB(dataList)
    }

    private fun insertExchangeRatesIntoDB(dataList: List<ExchangeRate>) {
        // Subscribe to updating the user name.
        // Enable back the button once the user name has been updated
        disposable.add(
            dataSource.insertExchangeRateList(dataList).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({
                    Log.v(TAG, "Data has been inserted. ")
                }, { error ->
                    Log.v(TAG, "Unable to update username $error")
                })
        )
    }

    fun getCurrencyList(): ArrayList<String>? {
        return currencyList.value
    }

    private fun setCurrencyList(currencies: ArrayList<String>) {
        _positionForUSD = getItemPositionTargetCurrency(currencies, "USD")
        currencyList.postValue(currencies)
    }

    private fun extractCurrencies(rates: List<ExchangeRate>): ArrayList<String> {
        val currencies = ArrayList<String>()
        for (exchangeRate in rates) {
            currencies.add(exchangeRate.currency)
        }
        currencies.sort()
        return currencies
    }

    private fun extractCurrencies(rates: HashMap<String, Double>): ArrayList<String> {
        val currencies = ArrayList<String>()
        for ((key, _) in rates) {
            currencies.add(key)
        }
        currencies.sort()
        return currencies
    }

    private val dataListRateLimit =
        RateLimiter(TIMEOUT_TO_FETCH_DATA_IN_MINUTES, TimeUnit.MINUTES)
    private val disposable = CompositeDisposable()

    fun fetchData() {

        Log.v(TAG, "+++ fetchData +++")

        disposable.add(
            fetchDataFromDB().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ listOfExchangeRate ->
                    if (listOfExchangeRate.isEmpty()) {
                        Log.v(
                            TAG,
                            "X> There is no data in database. we should fetch data from network."
                        )
                        fetchDataFromNetwork()
                    } else {
                        Log.v(TAG, "O> There is data in database. we need to re-use it.")
                        val lastFetchTime = listOfExchangeRate.first().lastFetchTime
                        if (dataListRateLimit.shouldFetch(lastFetched = lastFetchTime)) {
                            fetchDataFromNetwork()
                        } else {
                            setCurrencyList(extractCurrencies(listOfExchangeRate))
                            val dataMap = listOfExchangeRate.associate {
                                it.currency to it.usdConvertibleAmount
                            }
                            exchangeRates.postValue(dataMap)
                        }
                    }
                }, { error ->
                    Log.v(TAG, "Unable to fetchDataFromDB: $error")
                })
        )
    }

    private fun fetchDataFromDB(): Flowable<List<ExchangeRate>> {
        Log.v(TAG, "+++ fetchDataFromDB +++")
        return dataSource.getAllExchangeRates()
    }

    private fun fetchDataFromNetwork() {
        Log.v(TAG, "+++ fetchDataFromNetwork +++")

        val service = OpenExchangeRateServiceInstance.retrofitInstance!!.create(
            OpenExchangeRateService::class.java
        )
        service.getExchangeRateInfo.enqueue(object : Callback<ExchangeRatesAPIResponse?> {
            override fun onResponse(
                call: Call<ExchangeRatesAPIResponse?>, response: Response<ExchangeRatesAPIResponse?>
            ) {
                Log.v(TAG, "Data was received. ")
                val timestampForData = response.body()?.timestamp
                response.body()?.rates?.let {
                    setCurrencyList(extractCurrencies(it))
                    val lastFetchTime = System.currentTimeMillis()
                    setExchangeRates(it, timestampForData, lastFetchTime)
                }
            }

            override fun onFailure(call: Call<ExchangeRatesAPIResponse?>, t: Throwable) {
                Log.v(TAG, t.message ?: "")
            }
        })
    }

    fun calculateExchangeRateForCurrencies(
        inputValue: Double = 0.0, baseCurrency: String = "USD"
    ) {

        val exchangeRates = getExchangeRates()
        exchangeRates?.let {

            val newExchangeRates = arrayListOf<ExchangeRateItem>()

            val baseExchangeRate = it[baseCurrency]
            val baseCurrencyToUSD = 1 / baseExchangeRate!!

            val currencies = getCurrencyList() ?: ArrayList()

            if (baseExchangeRate != 1.0) {

                for (currency in currencies) {
                    val exchangeRate = it[currency]
                    val currencyToUSD = 1 / exchangeRate!!
                    var convertedValue = (baseCurrencyToUSD / currencyToUSD * inputValue)
                    if (currency == "USD") {
                        convertedValue = baseExchangeRate * inputValue
                    }

                    newExchangeRates.add(
                        ExchangeRateItem(
                            currency = currency, value = convertedValue
                        )
                    )
                }
            } else {
                for (currency in currencies) {
                    val diffToUSD = it[currency]
                    newExchangeRates.add(
                        ExchangeRateItem(
                            currency = currency, value = inputValue * diffToUSD!!
                        )
                    )
                }
            }

            calculatedExchangeRates.postValue(newExchangeRates)
        }
    }

    fun setBaseCurrency(currency: String) {
        baseCurrency.postValue(currency)
    }

    fun getBaseCurrency(): String? {
        return baseCurrency.value
    }
}