package com.example.conversordemoedas.interaction

import com.example.conversordemoedas.viewmodel.main.MainViewModel

/**
 * Classe responsável por intermediar as chamadas da viewModel na MainActivity
 */
class MainInteraction(private val viewModel: MainViewModel) {

    fun updateBaseCurrencyCode(position: Int) {
        viewModel.updateBaseCurrency(position)
    }

    fun updateTargetCurrencyCode(position: Int) {
        viewModel.updateTargetCurrency(position)
    }

    fun reverseCurrencies() {
        viewModel.reverseCurrencies()
    }

    fun textChanged(monetaryValueText: String) {
        if (monetaryValueText.isEmpty()) return
        viewModel.textFormattingMonetaryValue(monetaryValueText)
    }

    fun rateChangedWithTextTyped() {
        viewModel.rateChangedWithTextTyped()
    }


}