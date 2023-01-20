package com.bh.mycurrencyconveter.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bh.mycurrencyconveter.api.OpenExchangeRateService
import com.bh.mycurrencyconveter.persistence.ExchangeRate
import com.bh.mycurrencyconveter.persistence.ExchangeRateDao
import com.bh.mycurrencyconveter.ui.main.OpenExchangeRateServiceInstance
import com.bh.mycurrencyconveter.vo.ExchangeRateItem
import com.bh.mycurrencyconveter.vo.ExchangeRatesAPIResponse
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainViewModel(private val dataSource: ExchangeRateDao) : ViewModel() {

    companion object {
        const val DEFAULT_BASE_CURRENCY = "USD"
    }

    fun getDefaultBaseCurrency(): String {
        return DEFAULT_BASE_CURRENCY
    }

    private var _positionForUSD: Int = 0
    private var baseCurrency = MutableLiveData<String>(DEFAULT_BASE_CURRENCY)
    var exchangeRates = MutableLiveData<HashMap<String, Double>>()
    var calculatedExchangeRates = MutableLiveData<ArrayList<ExchangeRateItem>>()

    var currencyList = MutableLiveData<ArrayList<String>>()

    private fun getExchangeRates(): HashMap<String, Double>? {
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

    private fun getItemPositionTargetCurrency(
        currencies: List<String>,
        targetCurrency: String
    ): Int {
        for ((index, value) in currencies.withIndex()) {
            if (value == targetCurrency) {
                return index
            }
        }
        return 0
    }

    fun getPositionForUSD(): Int {
        return _positionForUSD
    }

    fun setExchangeRates(rates: HashMap<String, Double>, timestamp: Long?) {
        exchangeRates.postValue(rates)

        val dataList = ArrayList<ExchangeRate>()
        for ((currency, amount) in rates) {
            println("$currency = $amount")
            dataList.add(ExchangeRate(
                currency = currency,
                usdConvertibleAmount = amount,
                timestamp = timestamp ?: -1
            ))
        }
        dataSource.insertExchangeRateList(dataList)
    }

    private fun getCurrencyList(): ArrayList<String>? {
        return currencyList.value
    }

    private fun extractCurrencies(rates: HashMap<String, Double>) {
        val currencies = ArrayList<String>()
        for ((key, value) in rates) {
            println("$key = $value")
            currencies.add(key)
        }
        println("currencyList = $currencies")
        currencies.sort()
        _positionForUSD = getItemPositionTargetCurrency(currencies, "USD")
        currencyList.postValue(currencies)
    }

    fun fetchData() {
        // Subscribe to the emissions of the user name from the view model.
        // Update the user name text view, at every onNext emission.
        // In case of error, log the exception.
        val disposable = CompositeDisposable()
        disposable.add(
            fetchDataFromDB()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        println("Fetched data: $it")
                        if (it.isEmpty()) {
                            retrieveLatestExchangeRateInfo()
                        }
                    },
                    { error ->
                        println("Unable to fetchDataFromDB: $error")
                    })
        )
    }

    private fun fetchDataFromDB(): Flowable<List<ExchangeRate>> {
        return dataSource.getAllExchangeRates()
    }

    private fun retrieveLatestExchangeRateInfo() {

        val service = OpenExchangeRateServiceInstance.retrofitInstance!!.create(
            OpenExchangeRateService::class.java
        )
        service.getExchangeRateInfo.enqueue(object : Callback<ExchangeRatesAPIResponse?> {
            override fun onResponse(
                call: Call<ExchangeRatesAPIResponse?>,
                response: Response<ExchangeRatesAPIResponse?>
            ) {
                println("Received Response")


                val timestamp = response.body()?.timestamp
                response.body()?.rates?.let {
                    extractCurrencies(it)
                    setExchangeRates(it, timestamp)
                }

            }

            override fun onFailure(call: Call<ExchangeRatesAPIResponse?>, t: Throwable) {
                println(t.message ?: "")

            }
        })
    }

    fun calculateExchangeRateForCurrencies(
        inputValue: Double = 0.0,
        baseCurrency: String = "USD"
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
                        println("hit")
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