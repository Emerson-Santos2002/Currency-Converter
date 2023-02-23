package com.example.conversordemoedas.repositories

import com.example.conversordemoedas.rest.RetrofitService

/**
 * Classe responsável por  encapsular os detalhes de implementação de chamada Web do Retrofit
 */
class MainRepository(private val retrofitService : RetrofitService){

    fun getAllCurrencies() = retrofitService.getListCurrencies()
    fun getCurrencyConversionRate(baseCurrencyCode: String, targetCurrencyCode: String) = retrofitService.getCurrencyConversionRate(baseCurrencyCode, targetCurrencyCode)

}