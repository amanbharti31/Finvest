package com.finance.finvest.fragments

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import com.finance.finvest.adapters.FilterAdapter
import com.finance.finvest.databinding.FragmentFilterBinding
import com.finance.finvest.datamodels.FilterData
import com.finance.finvest.datamodels.FilterItem
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class FilterFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentFilterBinding? = null
    private val binding get() = _binding!!

    private lateinit var filterAdapter: FilterAdapter
    private val selectedFilters = mutableMapOf<String, String>()
    private var savedAmountRange = listOf(0f, 10000f)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFilterBinding.inflate(inflater, container, false)
        setupAmountSlider()
        setupRecyclerView()
        setupButtons()
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val filterData = arguments?.getParcelable<FilterData>("filterData")
        if (filterData == null) {
            Log.w("FilterFragment", "No filter data provided. Using defaults.")
        } else {
            Log.d("FilterFragment", "Received filter data: $filterData")
            selectedFilters.clear()
            selectedFilters.putAll(filterData.selectedFilters)
            savedAmountRange = filterData.amountRange.toList()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding.rvFilters.adapter = null
        _binding = null
    }


    private fun setupAmountSlider() {
        binding.sliderAmount.values = savedAmountRange
        binding.sliderAmount.addOnChangeListener { slider, _, _ ->
            val values = slider.values
            if (values.size == 2) {
                savedAmountRange = values
                Log.d("FilterFragment", "Slider values updated: $savedAmountRange")
            } else {
                Log.e("FilterFragment", "Unexpected number of values: ${values.size}")
            }
        }

    }

    private fun setupRecyclerView() {
        val filters = listOf(
            FilterItem(
                "Date Range",
                listOf("All time", "Current month", "Last month", "This year", "Previous year")
            ),
            FilterItem("Status", listOf("All", "Pending", "Completed")),
            FilterItem("Transaction Type", listOf("All", "Buy", "Sell", "Deposit", "Withdrawal")),
            FilterItem(
                "Categories",
                listOf(
                    "All",
                    "Food & Dining",
                    "Housing",
                    "Auto & Transport",
                    "Health",
                    "Entertainment"
                )
            )
        )

        filterAdapter = FilterAdapter(filters) { filterItem, selectedOption ->
            selectedFilters[filterItem.title] = selectedOption
        }

        // Pre-select saved filters
        filterAdapter.selectedFilters = selectedFilters

        binding.rvFilters.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFilters.adapter = filterAdapter
        restoreSavedFilters()

    }

    private fun setupButtons() {
        binding.btnClearAll.setOnClickListener {
            if (selectedFilters.isNotEmpty() || savedAmountRange != listOf(0f, 10000f)) {
                selectedFilters.clear()
                savedAmountRange = listOf(0f, 10000f)
                Log.d("FilterFragment", "Cleared all filters and reset amount range")
                filterAdapter.selectedFilters = selectedFilters
                filterAdapter.notifyDataSetChanged()
                binding.sliderAmount.values = savedAmountRange
            }
        }

        binding.btnApply.setOnClickListener {
            val amountRangePair = savedAmountRange.let {
                if (it.size >= 2) Pair(it[0], it[1]) else Pair(0f, 10000f)
            }
            Log.d("FilterFragment", "Applying filters: $selectedFilters, Amount range: $amountRangePair")
            val result = FilterData(selectedFilters, amountRangePair)
            setFragmentResult(
                "FILTERS_RESULT",
                Bundle().apply { putParcelable("filterData", result) }
            )
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout
            val behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED

            dialog.window?.setDimAmount(0.9f)
        }
        return dialog
    }

    private fun restoreSavedFilters() {
        if (selectedFilters.isNotEmpty()) {
            filterAdapter.selectedFilters = selectedFilters
            filterAdapter.notifyDataSetChanged()
        }
        binding.sliderAmount.values = savedAmountRange
        Log.d("FilterFragment", "Restored filters: $selectedFilters, Amount range: $savedAmountRange")
    }



    companion object {
        fun newInstance(
            selectedFilters: Map<String, String>,
            amountRange: Pair<Float, Float>
        ): FilterFragment {
            return FilterFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(
                        "filterData",
                        FilterData(
                            selectedFilters = HashMap(selectedFilters),
                            amountRange = amountRange
                        )
                    )
                }
            }
        }
    }

}