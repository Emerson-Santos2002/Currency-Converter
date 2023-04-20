package com.example.conversordemoedas.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.conversordemoedas.MainActivity
import com.example.conversordemoedas.R
import com.example.conversordemoedas.repositories.MainRepository
import com.example.conversordemoedas.util.TextFormatter
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Response

class MainViewModel constructor(private val repository: MainRepository) : ViewModel() {

    private var baseCurrencyCode : String = ""
    private var targetCurrencyCode : String = ""
    private var previousCurrencyCode : String = ""

    val currencyList = mutableListOf<String>()
    val currencyListObserver = MutableLiveData<List<String>>()

    var conversionRate : Double = 0.0
    val conversionRateObserver = MutableLiveData<Double>()

    private var monetaryValueEditText : String = ""
    val editTextMonetaryValueToBeConverted = MutableLiveData<String>()

    private var monetaryValueTextView : String = ""
    val textViewMonetaryValueConverted = MutableLiveData<String>()

    val errorMessageCallBack = MutableLiveData<String>()

    init {
        currencyListObserver.observeForever {
            setDefaultCurrency()
            getCurrencyConversionRate()
        }
    }

    fun updateBaseCurrency(position: Int) {
        baseCurrencyCode = currencyList[position]
        getCurrencyConversionRate()
    }

    fun updateTargetCurrency(position: Int) {
        targetCurrencyCode = currencyList[position]
        getCurrencyConversionRate()
    }

    fun reverseCurrencies() {
        baseCurrencyCode = targetCurrencyCode.also { targetCurrencyCode = baseCurrencyCode }

        if (monetaryValueEditText.isEmpty())
            previousCurrencyCode = baseCurrencyCode

        getCurrencyConversionRate()
    }

    private fun setDefaultCurrency() {
        val defaultBaseCurrency = MainActivity.Singleton.DEFAULT_BASE_CURRENCY
        val defaultTargetCurrency = MainActivity.Singleton.DEFAULT_TARGET_CURRENCY
        previousCurrencyCode = defaultBaseCurrency
        baseCurrencyCode = defaultBaseCurrency
        targetCurrencyCode = defaultTargetCurrency
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

        repository.getCurrencyConversionRate(baseCurrencyCode, targetCurrencyCode)
            .enqueue(object : retrofit2.Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>) {

                    val rateCallback = response.body()?.entrySet()?.find { currency -> currency.key == targetCurrencyCode }

                    conversionRate = rateCallback?.value.toString().toDouble()
                    conversionRateObserver.postValue(conversionRate)
                }

                override fun onFailure(call: Call<JsonObject>, error: Throwable) {

                    errorMessageCallBack.postValue(error.message)

                }
            })
    }


    fun textFormattingMonetaryValue(monetaryValueTextFormatted: String) {

        val unformattedMonetaryValue = TextFormatter.clearTextFormatting(baseCurrencyCode, monetaryValueTextFormatted)
        val convertedMonetaryValue = convertMoney(unformattedMonetaryValue)

        monetaryValueEditText = TextFormatter.formatTextForSelectedCurrency(baseCurrencyCode, unformattedMonetaryValue)
        editTextMonetaryValueToBeConverted.postValue(monetaryValueEditText)

        monetaryValueTextView = TextFormatter.formatTextForSelectedCurrency(targetCurrencyCode, convertedMonetaryValue)
        textViewMonetaryValueConverted.postValue(monetaryValueTextView)

    }

    fun rateChangedWithTextTyped(){

        when (previousCurrencyCode) {

            baseCurrencyCode -> { textFormattingMonetaryValue(monetaryValueEditText) }

            else -> {

                val unformattedMonetaryValue = TextFormatter.clearTextFormatting(previousCurrencyCode, monetaryValueEditText)
                val convertedMonetaryValue = convertMoney(unformattedMonetaryValue)

                monetaryValueEditText = TextFormatter.formatTextForSelectedCurrency(baseCurrencyCode, unformattedMonetaryValue)
                editTextMonetaryValueToBeConverted.postValue(monetaryValueEditText)

                monetaryValueTextView = TextFormatter.formatTextForSelectedCurrency(targetCurrencyCode, convertedMonetaryValue)
                textViewMonetaryValueConverted.postValue(monetaryValueTextView)

                previousCurrencyCode = baseCurrencyCode

            }
        }
    }

    private fun convertMoney(stringEditable: String): String {

        return (stringEditable.toDouble() * conversionRate).toString()

    }

}