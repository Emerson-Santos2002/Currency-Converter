package com.example.conversordemoedas.util

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*

class TextFormatter {

    companion object {

        /**
         * Remove os caracteres especiais da formatação de um valor monetário, como símbolos da moeda e espaços em branco.
         * Também substitui o separador decimal para o ponto, para que possa ser convertido para um número.
         *
         * @param currencyCode Código da moeda em formato ISO (ex: "BRL" para real brasileiro).
         * @param monetaryValueInFormattedText Valor monetário em formato de texto com formatação.
         * @return Retorna o valor monetário sem os caracteres especiais.
         */
        fun clearTextFormatting(currencyCode: String, monetaryValueInFormattedText: String): String {

            val currencySymbol = Currency.getInstance(currencyCode.uppercase()).symbol

            val defaultDecimalSeparator = DecimalFormatSymbols.getInstance(Locale("pt", "br")).decimalSeparator

            var unformattedText = monetaryValueInFormattedText.replace(currencySymbol, "").trim()

            // Substitui o separador decimal default para o padrão internacional
            unformattedText = unformattedText.replace(defaultDecimalSeparator, '.')

            unformattedText = unformattedText.replace(".", "")

            return unformattedText
        }

        /**
         * Formata um valor monetário para a moeda e a localidade especificadas.
         * O valor deve estar em centavos, sem os separadores decimais e sem o símbolo da moeda.
         * @param currencyCode Código da moeda em formato ISO (ex: "BRL" para real brasileiro).
         * @param textEditable Valor monetário em formato de texto (ex: "123456" para R$ 1.234,56).
         * @return O valor monetário formatado para a moeda e a localidade especificadas.
         */
        fun formatTextForSelectedCurrency(currencyCode: String, textEditable: String): String {

            val parsedText = BigDecimal.valueOf(textEditable.toDouble())
                .setScale(2, RoundingMode.FLOOR)
                .divide(BigDecimal(100), RoundingMode.FLOOR)

            val decimalFormatSymbols = DecimalFormatSymbols(Locale("pt", "br"))
            decimalFormatSymbols.decimalSeparator = ','
            decimalFormatSymbols.groupingSeparator = '.'

            decimalFormatSymbols.currencySymbol = Currency.getInstance(currencyCode.uppercase()).symbol

            val formatPattern = "\u00A4 ###,##0.00"

            val numberFormat = DecimalFormat(formatPattern, decimalFormatSymbols)

            //Aplicar a escala dos decimais no formato da localidade BR
            return numberFormat.format(parsedText).replace("[$currencyCode,.\u00A0]", "")

        }
    }
}