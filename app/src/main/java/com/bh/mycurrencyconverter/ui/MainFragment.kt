package com.bh.mycurrencyconverter.ui

import android.R.layout.simple_spinner_dropdown_item
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.bh.mycurrencyconverter.Injection
import com.bh.mycurrencyconverter.databinding.FragmentMainBinding
import com.bh.mycurrencyconverter.viewmodel.MainViewModel
import com.bh.mycurrencyconverter.vo.ExchangeRateItem


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
        private val TAG = "BH_Lin_${MainFragment::class.java.simpleName}"
    }


    private lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: MainViewModel by viewModels { viewModelFactory }

    private lateinit var binding: FragmentMainBinding

    private fun adapterOnClick(exchangeRateItem: ExchangeRateItem) {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModelFactory = Injection.provideViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        binding = FragmentMainBinding.inflate(inflater)
        exchangeRateInfoAdapter = ExchangeRateInfoAdapter { item -> adapterOnClick(item) }
        binding.exchangeRateList.layoutManager = LinearLayoutManager(context);
        binding.exchangeRateList.adapter = exchangeRateInfoAdapter

        binding.baseValue.doAfterTextChanged { inputString ->
            println("Input: $inputString")
            var inputValue = 0.0
            if (inputString != null && inputString.isNotEmpty()) {
                inputValue = inputString.toString().toDouble()
            }
            viewModel.calculateExchangeRateForCurrencies(
                inputValue = inputValue,
                baseCurrency = viewModel.getBaseCurrency() ?: MainViewModel.DEFAULT_BASE_CURRENCY
            )
        }
        binding.targetCurrencySelector.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val currency = viewModel.getCurrencyFromItemPosition(position)
                    println("onItemSelected: position=$position, currency=$currency")
                    viewModel.setBaseCurrency(currency)
                    var inputValue = 0.0
                    if (binding.baseValue.text.toString().isNotEmpty()) {
                        inputValue = binding.baseValue.text.toString().toDouble()
                    }
                    viewModel.calculateExchangeRateForCurrencies(
                        inputValue = inputValue,
                        baseCurrency = currency
                    )
                }
            }

        //viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        viewModel.currencyList.observe(
            viewLifecycleOwner
        ) { currencyList ->
            val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
                requireContext(),
                simple_spinner_dropdown_item,
                currencyList
            )
            adapter.setDropDownViewResource(simple_spinner_dropdown_item)
            binding.targetCurrencySelector.adapter = adapter
            binding.targetCurrencySelector.setSelection(viewModel.getPositionForUSD())
        }
        viewModel.calculatedExchangeRates.observe(viewLifecycleOwner) {
            exchangeRateInfoAdapter.submitList(it)
        }
        viewModel.exchangeRates.observe(viewLifecycleOwner) {
            viewModel.calculateExchangeRateForCurrencies(
                inputValue = binding.baseValue.text.toString().toDouble(),
                baseCurrency = viewModel.getBaseCurrency() ?: MainViewModel.DEFAULT_BASE_CURRENCY
            )
        }

        viewModel.fetchData()

        return binding.root
    }

    private lateinit var exchangeRateInfoAdapter: ExchangeRateInfoAdapter

}