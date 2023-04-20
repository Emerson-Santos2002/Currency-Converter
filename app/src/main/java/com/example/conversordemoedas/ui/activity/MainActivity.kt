package com.example.conversordemoedas.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.conversordemoedas.R
import com.example.conversordemoedas.databinding.ActivityMainBinding
import com.example.conversordemoedas.ui.viewmodel.MainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Única classe de view da aplicação, cuida dos componentes de visualização e dos parâmetros base da aplicação
 */
class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val mainViewModel: MainViewModel by viewModel()

    private lateinit var currencyList: List<String>
    private lateinit var spinnerAdapter: ArrayAdapter<String>

    private var basePosition: Int = 0
    private var targetPosition: Int = 0

    object Singleton{
        const val DEFAULT_BASE_CURRENCY = "brl"
        const val DEFAULT_TARGET_CURRENCY = "usd"
    }

    /**
     * Responsavel por instanciar os paramêtros base da aplicação
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        mainViewModel.getAllCurrencies(context = applicationContext)
    }

    /**
     * Responsavel por inicializar os observers da classe viewModel
     * e fazer a recuperação da lista de moedas da aplicação
     */
    override fun onStart() {
        super.onStart()
        observeCurrencyList()
    }

    override fun onResume() {
        super.onResume()
        setButtonReverse()
        binding.editTextMonetaryValueToBeConverted.addTextChangedListener(textWatcher())
    }

    /**
     * Instancia o observador da lista de moedas da aplicação
     * Quando receber a lista de moedas e instancia o adapter
     * @see setDefaultCurrencies() configura os valores padrão do spinner
     */
    private fun observeCurrencyList() {
        mainViewModel.currencyListObserver.observe(this) { list ->

            spinnerAdapter = ArrayAdapter(this, R.layout.drop_down_item, list)
            currencyList = list
            println(currencyList)

            observeConversionRate()
            observeErrorMessage()
            observeEditTextMonetaryValueToBeConverter()
            observeTextViewMonetaryValueConverted()
            setDefaultCurrencies(list)
        }
    }
    private fun setDefaultCurrencies(list: List<String>) {
        binding.spinnerSelectorBaseCurrency.apply {
            adapter = spinnerAdapter
            basePosition = list.indexOf(Singleton.DEFAULT_BASE_CURRENCY)
            setSelection(basePosition)
        }
        binding.spinnerSelectorTargetCurrency.apply {
            adapter = spinnerAdapter
            targetPosition = list.indexOf(Singleton.DEFAULT_TARGET_CURRENCY)
            setSelection(targetPosition)
        }
        mainViewModel.setDefaultCurrency(
            base = Singleton.DEFAULT_BASE_CURRENCY,
            target = Singleton.DEFAULT_TARGET_CURRENCY
        )
    }

    /**
     * Instancia o observador da taxa de conversão das moedas
     * @see spinnerListener() Inicializa o spinnerListener quando os valores padrões ja tiverem sido setados.
     * @see setDefaultTextInformation() Atualiza os TextView informativos do topo da Tela
     * @if Função responsável por verificar se tem digitado no EditText, senão apenas atualiza a conversionRate
     * Se sim remove o textChangedListener para evitar ciclo infinito e chama a função de formatação.
     */
    private fun observeConversionRate() {

        mainViewModel.conversionRateObserver.observe(this) { rate ->

            spinnerListener()
            setDefaultTextInformation(rate)

            if(binding.editTextMonetaryValueToBeConverted.text.toString().isNotEmpty()){
                binding.editTextMonetaryValueToBeConverted.removeTextChangedListener(textWatcher())
                mainViewModel.rateChangedWithTextTyped()
            }
        }
    }

    /**
     * Faz as chamadas de funções responsáveis caso algum texto seja digitado
     * Remove o changedListener, pois ao fazer a formatação do texto ele entederá como uma nova mudança de texto,
     * entrando em um ciclo infinito de formatação.
     * o ChangedListener será adicionado novamente depois do valor formatado ser adicionado no Observer do EditText.
     * @see mainViewModel chama a função para formatar o texto.
     */

    private fun textWatcher() = object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(textEditable: Editable?) {

            textEditable?.let {
                if (it.isNotEmpty()){
                    binding.editTextMonetaryValueToBeConverted.removeTextChangedListener(this)
                    mainViewModel.textFormattingMonetaryValue(it.toString())
                }
            }
        }
    }

    /**
     * Aplica as informações padrões dos textView principal da View
     * @param conversionRate taxa de conversão das moedas.
     */
    private fun setDefaultTextInformation(conversionRate: Double) {

        binding.baseCurrencyConversionRateTextView.text =
            getString(
                R.string.info_da_moeda_base,
                binding.spinnerSelectorBaseCurrency.selectedItem.toString().uppercase()
            )
        binding.targetCurrencyConversionRateTextView.text =
            getString(
                R.string.info_da_moeda_alvo,
                "%.2f".format(conversionRate),
                binding.spinnerSelectorTargetCurrency.selectedItem.toString().uppercase()
            )
    }

    @SuppressLint("ClickableViewAccessibility")
    /**
     * Observa se houve mudança no listener por clique e não de forma programatica.
     */
    private fun spinnerListener() {

        binding.apply {

            var userSelect = false

            val touchListener = View.OnTouchListener { _, _ ->
                userSelect = true
                false
            }

            val itemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                    if (userSelect){
                        when (parent) {

                            spinnerSelectorBaseCurrency -> {
                                if (position == spinnerSelectorTargetCurrency.selectedItemPosition){
                                    toastError()
                                    spinnerSelectorBaseCurrency.setSelection(basePosition)
                                }
                                else {
                                    basePosition = position
                                    mainViewModel.updateBaseCurrency(position)
                                }
                            }

                            spinnerSelectorTargetCurrency -> {
                                if (position == spinnerSelectorBaseCurrency.selectedItemPosition){
                                    toastError()
                                    spinnerSelectorTargetCurrency.setSelection(targetPosition)
                                }
                                else{
                                    targetPosition = position
                                    mainViewModel.updateTargetCurrency(position)
                                }

                            }
                            else -> Log.i("emerson", "parent não encontrado")
                        }
                        userSelect = false
                    }
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            spinnerSelectorBaseCurrency.setOnTouchListener(touchListener)
            spinnerSelectorTargetCurrency.setOnTouchListener(touchListener)
            spinnerSelectorBaseCurrency.onItemSelectedListener = itemSelectedListener
            spinnerSelectorTargetCurrency.onItemSelectedListener = itemSelectedListener
        }
    }

    /**
     * Instancia o observador de valores do EditText.
     * Quando um novo valor do EditText é gerado atualiza na View pelo binding.
     * @see binding "" addTextChangedListener() Adiciona novamente o Listener,
     * para voltar a verificar se um novo texto foi digitado.
     */
    private fun observeEditTextMonetaryValueToBeConverter() {

        mainViewModel.editTextMonetaryValueToBeConverted.observe(this@MainActivity){ formattedText ->

            println("editText: $formattedText")
            binding.editTextMonetaryValueToBeConverted.setText(formattedText)
            binding.editTextMonetaryValueToBeConverted.setSelection(formattedText.length)
        }
    }

    private fun observeTextViewMonetaryValueConverted() {
        mainViewModel.textViewMonetaryValueConverted.observe(this@MainActivity){ formattedText ->

            println("textView: $formattedText")
            binding.textViewMonetaryValueConverted.text = formattedText
            binding.editTextMonetaryValueToBeConverted.addTextChangedListener(textWatcher())
        }
    }

    /**
     * observa se um erro foi gerado no callback de dados da aplicação
     */
    private fun observeErrorMessage() {
        mainViewModel.errorMessageCallBack.observe(this@MainActivity) { errorMessage ->
            Toast.makeText(this@MainActivity, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Inverte as moedas, a moeda base vira a alvo e a alvo vira a base.
     * Remove os listeners para não chamar a função updateBase/Target e atualizar o rate antes de atualizar as duas.
     * Atualizar a View da inversão das moedas no setSelection
     * chamar a reverseCurrencies pra atualizar os dados na viewModel e atualizar o Rate.
     * @see spinnerListener() adicionar novamente o listener.
     */
    private fun setButtonReverse() {

        binding.apply {

            buttonCurrencyReversal.setOnClickListener {

                val baseCurrencyPosition = spinnerAdapter.getPosition(spinnerSelectorBaseCurrency.selectedItem.toString())
                val targetCurrencyPosition = spinnerAdapter.getPosition(spinnerSelectorTargetCurrency.selectedItem.toString())

                binding.spinnerSelectorBaseCurrency.onItemSelectedListener = null
                binding.spinnerSelectorTargetCurrency.onItemSelectedListener = null

                spinnerSelectorBaseCurrency.setSelection(targetCurrencyPosition)
                spinnerSelectorTargetCurrency.setSelection(baseCurrencyPosition)

                binding.editTextMonetaryValueToBeConverted.removeTextChangedListener(textWatcher())
                mainViewModel.reverseCurrencies()
            }
        }
    }

    private fun toastError() {
        Toast.makeText(this@MainActivity, "As moedas são iguais", Toast.LENGTH_SHORT).show()
    }
}