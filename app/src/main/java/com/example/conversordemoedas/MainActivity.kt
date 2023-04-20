package com.example.conversordemoedas

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
import androidx.lifecycle.ViewModelProvider
import com.example.conversordemoedas.databinding.ActivityMainBinding
import com.example.conversordemoedas.repositories.MainRepository
import com.example.conversordemoedas.rest.RetrofitService
import com.example.conversordemoedas.viewmodel.MainViewModel
import com.example.conversordemoedas.viewmodel.MainViewModelFactory

/**
 * Única classe de view da aplicação, cuida dos componentes de visualização e dos parâmetros base da aplicação
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var currencyList: List<String>
    private lateinit var spinnerAdapter: ArrayAdapter<String>

    private val retrofitService = RetrofitService.getInstance()

    object Singleton{
        const val DEFAULT_BASE_CURRENCY = "brl"
        const val DEFAULT_TARGET_CURRENCY = "usd"
    }

    /**
     * Responsavel por instanciar os paramêtros base da aplicação
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        setContentView(binding.root)

        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(MainRepository(retrofitService = retrofitService))
        )[MainViewModel::class.java]

        viewModel.getAllCurrencies(context = applicationContext)
    }

    /**
     * Responsavel por inicializar os observers da classe viewModel
     * e fazer a recuperação da lista de moedas da aplicação
     */
    override fun onStart() {
        super.onStart()
        observeCurrencyList()
        observeConversionRate()
        observeEditTextMonetaryValueToBeConverter()
        observeTextViewMonetaryValueConverted()
        observeErrorMessage()
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
        viewModel.currencyListObserver.observe(this) { list ->
            currencyList = list
            spinnerAdapter = ArrayAdapter(this, R.layout.drop_down_item, list)
            setDefaultCurrencies(list)
            println(list)
        }
    }
    private fun setDefaultCurrencies(list: List<String>) {
        binding.spinnerSelectorBaseCurrency.apply {
            adapter = spinnerAdapter
            setSelection(list.indexOf(Singleton.DEFAULT_BASE_CURRENCY))
        }
        binding.spinnerSelectorTargetCurrency.apply {
            adapter = spinnerAdapter
            setSelection(list.indexOf(Singleton.DEFAULT_TARGET_CURRENCY))
        }
    }

    /**
     * Instancia o observador da taxa de conversão das moedas
     * @see spinnerListener() Inicializa o spinnerListener quando os valores padrões ja tiverem sido setados.
     * @see setDefaultTextInformation() Atualiza os TextView informativos do topo da Tela
     * @if Função responsável por verificar se tem digitado no EditText, senão apenas atualiza a conversionRate
     * Se sim remove o textChangedListener para evitar ciclo infinito e chama a função de formatação.
     */
    private fun observeConversionRate() {

        viewModel.conversionRateObserver.observe(this) { rate ->

            spinnerListener()
            setDefaultTextInformation(rate)

            if(binding.editTextMonetaryValueToBeConverted.text.toString().isNotEmpty()){
                binding.editTextMonetaryValueToBeConverted.removeTextChangedListener(textWatcher())
                viewModel.rateChangedWithTextTyped()
            }
        }
    }

    /**
     * Faz as chamadas de funções responsáveis caso algum texto seja digitado
     * Remove o changedListener, pois ao fazer a formatação do texto ele entederá como uma nova mudança de texto,
     * entrando em um ciclo infinito de formatação.
     * o ChangedListener será adicionado novamente depois do valor formatado ser adicionado no Observer do EditText.
     * @see viewModel chama a função para formatar o texto.
     */

    private fun textWatcher() = object : TextWatcher {

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

        override fun afterTextChanged(textEditable: Editable?) {

            textEditable?.let {
                if (it.isNotEmpty()){
                    binding.editTextMonetaryValueToBeConverted.removeTextChangedListener(this)
                    viewModel.textFormattingMonetaryValue(it.toString())
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

                            spinnerSelectorBaseCurrency -> { viewModel.updateBaseCurrency(position) }
                            spinnerSelectorTargetCurrency -> { viewModel.updateTargetCurrency(position) }
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
        viewModel.editTextMonetaryValueToBeConverted.observe(this@MainActivity){ formattedText ->

            println("editText: $formattedText")
            binding.editTextMonetaryValueToBeConverted.setText(formattedText)
            binding.editTextMonetaryValueToBeConverted.setSelection(formattedText.length)
            binding.editTextMonetaryValueToBeConverted.addTextChangedListener(textWatcher())

        }
    }

    private fun observeTextViewMonetaryValueConverted() {
        viewModel.textViewMonetaryValueConverted.observe(this@MainActivity){ formattedText ->

            println("textView: $formattedText")
            binding.textViewMonetaryValueConverted.text = formattedText

        }
    }

    /**
     * observa se um erro foi gerado no callback de dados da aplicação
     */
    private fun observeErrorMessage() {
        viewModel.errorMessageCallBack.observe(this@MainActivity) { errorMessage ->
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

                spinnerSelectorBaseCurrency.setSelection(targetCurrencyPosition)
                spinnerSelectorTargetCurrency.setSelection(baseCurrencyPosition)
                viewModel.reverseCurrencies()
            }
        }
    }
}