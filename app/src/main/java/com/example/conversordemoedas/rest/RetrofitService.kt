package com.example.conversordemoedas.rest

import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

/**
 *Interface responsável por recuperar na Web lista de moedas e taxa de conversão utilizando Retrofit
 */
interface RetrofitService {

    @GET("/gh/fawazahmed0/currency-api@1/latest/currencies.json")
    fun getListCurrencies() : Call<JsonObject>

    @GET("/gh/fawazahmed0/currency-api@1/latest/currencies/{baseCurrency}/{targetCurrency}.json")
    fun getCurrencyConversionRate(
        @Path(value = "baseCurrency", encoded = true) baseCurrency : String,
        @Path(value = "targetCurrency", encoded = true) targetCurrency : String
    ) : Call<JsonObject>

    companion object {

        private val retrofitService : RetrofitService by lazy {

            val retrofit = Retrofit.Builder()
                .baseUrl("https://cdn.jsdelivr.net/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            retrofit.create(RetrofitService::class.java)
        }

        fun getInstance() : RetrofitService{
            return retrofitService
        }
    }
}