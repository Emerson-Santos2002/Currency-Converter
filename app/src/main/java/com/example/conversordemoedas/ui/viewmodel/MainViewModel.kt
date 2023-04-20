package com.example.conversordemoedas.ui.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.conversordemoedas.R
import com.example.conversordemoedas.data.repository.MainRepository
import com.example.conversordemoedas.util.TextFormatter
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Response

class MainViewModel(private val repository: MainRepository) : ViewModel() {

    private var baseCurrencyLiveData = MutableLiveData<String>()
    private var targetCurrencyLiveData = MutableLiveData<String>()
    private var previousCurrencyCode : String = ""
    private var baseCurrency : String = ""
    private var targetCurrency : String = ""

    val currencyList = mutableListOf<String>()
    val currencyListObserver = MutableLiveData<List<String>>()

    var conversionRate : Double = 0.0
    val conversionRateObserver = MutableLiveData<Double>()

    private var monetaryValueEditText : String = ""
    val editTextMonetaryValueToBeConverted = MutableLiveData<String>()

    private var monetaryValueTextView : String = ""
    val textViewMonetaryValueConverted = MutableLiveData<String>()

    val errorMessageCallBack = MutableLiveData<String>()

    fun setDefaultCurrency(base: String, target: String) {

        previousCurrencyCode = base
        baseCurrency = base
        targetCurrency = target
        getCurrencyConversionRate()
    }
    init {
        baseCurrencyLiveData.observeForever { result ->

            result?.let {
                baseCurrency = it
                getCurrencyConversionRate()
            }
        }
        targetCurrencyLiveData.observeForever { result ->

            result?.let {
                targetCurrency = it
                getCurrencyConversionRate()
            }
        }
    }

    fun updateBaseCurrency(position: Int) {
        baseCurrencyLiveData.postValue(currencyList[position])
    }

    fun updateTargetCurrency(position: Int) {
        targetCurrencyLiveData.postValue(currencyList[position])
    }

    fun reverseCurrencies() {

        baseCurrency = targetCurrency.also { targetCurrency = baseCurrency }
        if (monetaryValueEditText.isEmpty()){
            previousCurrencyCode = baseCurrency
        }
        Log.i(
            "emerson",
            "base: $baseCurrency\ntarget:$targetCurrency\nprevious:$previousCurrencyCode"
        )
        getCurrencyConversionRate()
    }

    fun getAllCurrencies(context: Context) {

        repository.getAllCurrencies().enqueue(object : retrofit2.Callback<JsonObject> {
            override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {

                val listCurrenciesDefault: List<String> =
                    context.resources.getStringArray(R.array.currency_list_default).toList()

                response.body()?.keySet()?.iterator()?.forEach { currency ->

                    if (currency in listCurrenciesDefault)
                        currencyList.add(currency)

                }
                currencyListObserver.postValue(currencyList)
            }

            override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                errorMessageCallBack.postValue(t.message)
            }
        })
    }

    private fun getCurrencyConversionRate() {

        repository.getCurrencyConversionRate(baseCurrency, targetCurrency)
            .enqueue(object : retrofit2.Callback<JsonObject> {
                override fun onResponse(
                    call: Call<JsonObject>,
                    response: Response<JsonObject>
                ) {

                    val rateCallback = response.body()?.entrySet()?.find { currency -> currency.key == targetCurrency }

                    conversionRate = rateCallback?.value.toString().toDouble()
                    conversionRateObserver.postValue(conversionRate)
                }
                override fun onFailure(call: Call<JsonObject>, error: Throwable) {
                    errorMessageCallBack.postValue(error.message)
                }
            })
    }

    fun textFormattingMonetaryValue(monetaryValueTextFormatted: String) {

        val unformattedMonetaryValue =
            TextFormatter.clearTextFormatting(baseCurrency, monetaryValueTextFormatted)
        val convertedMonetaryValue = convertMoney(unformattedMonetaryValue)

        monetaryValueEditText =
            TextFormatter.formatTextForSelectedCurrency(baseCurrency, unformattedMonetaryValue)
        editTextMonetaryValueToBeConverted.postValue(monetaryValueEditText)

        monetaryValueTextView =
            TextFormatter.formatTextForSelectedCurrency(targetCurrency, convertedMonetaryValue)
        textViewMonetaryValueConverted.postValue(monetaryValueTextView)

    }

    fun rateChangedWithTextTyped(){

        when (previousCurrencyCode) {

            baseCurrency -> { textFormattingMonetaryValue(monetaryValueEditText) }

            else -> {

                val unformattedMonetaryValue = TextFormatter.clearTextFormatting(previousCurrencyCode, monetaryValueEditText)
                val convertedMonetaryValue = convertMoney(unformattedMonetaryValue)

                monetaryValueEditText = TextFormatter.formatTextForSelectedCurrency(baseCurrency, unformattedMonetaryValue)
                editTextMonetaryValueToBeConverted.postValue(monetaryValueEditText)
                previousCurrencyCode = baseCurrency

                monetaryValueTextView = TextFormatter.formatTextForSelectedCurrency(targetCurrency, convertedMonetaryValue)
                textViewMonetaryValueConverted.postValue(monetaryValueTextView)
            }
        }
    }

    private fun convertMoney(stringEditable: String): String {

        return (stringEditable.toDouble() * conversionRate).toString()
    }
}