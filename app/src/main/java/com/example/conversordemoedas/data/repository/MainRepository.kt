package com.example.conversordemoedas.data.repository

import com.example.conversordemoedas.data.remote.ApiService

/**
 * Classe responsável por  encapsular os detalhes de implementação de chamada Web do Retrofit
 */
class MainRepository(private val apiService : ApiService){

    fun getAllCurrencies() = apiService.getListCurrencies()
    fun getCurrencyConversionRate(baseCurrencyCode: String, targetCurrencyCode: String) = apiService.getCurrencyConversionRate(baseCurrencyCode, targetCurrencyCode)

}