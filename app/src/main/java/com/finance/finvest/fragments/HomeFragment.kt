package com.finance.finvest.fragments

import CreditCardRepository
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.finance.finvest.R
import com.finance.finvest.activities.TransactionsActivity
import com.finance.finvest.adapters.CreditCardAdapter
import com.finance.finvest.adapters.RecentTransactionsAdapter
import com.finance.finvest.adapters.TopCategoriesAdapter
import com.finance.finvest.databinding.FragmentHomeBinding
import com.finance.finvest.utils.AssetReader
import com.finance.finvest.viewmodels.CreditCardViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CreditCardViewModel by viewModels {
        CreditCardViewModel.Factory(CreditCardRepository(AssetReader(requireContext())))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setupObservers()
        setupRecyclerViews()
        setupRadioGroupListener()
        configureLineChart()
        viewModel.filterTransactionsForGraph("6M")
        binding.tvSeeAllCategories.setOnClickListener {
            val intent = Intent(requireContext(), TransactionsActivity::class.java)
            startActivity(intent)
        }
        binding.tvAddAccount.setOnClickListener {
            val intent = Intent(requireContext(), TransactionsActivity::class.java)
            startActivity(intent)
        }
        binding.tvSeeAllTransactions.setOnClickListener {
            val intent = Intent(requireContext(), TransactionsActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupObservers() {
        viewModel.totalPendingAmount.observe(viewLifecycleOwner) { totalPending ->
            binding.tvBalanceAmount.text = String.format("$%.2f", totalPending)
        }

        viewModel.filteredData.observe(viewLifecycleOwner) { filteredData ->
            if (filteredData.isNotEmpty()) {
                updateGraph(filteredData)
            }
            binding.progressBar.visibility = View.GONE
        }

        viewModel.creditCards.observe(viewLifecycleOwner) { creditCards ->
            binding.progressBar.visibility = if (creditCards.isEmpty()) View.VISIBLE else View.GONE

            val creditCardAdapter = CreditCardAdapter(creditCards)
            binding.rvCreditCardDetails.adapter = creditCardAdapter

            val topCategories = viewModel.getTopCategories()
            val topCategoriesAdapter = TopCategoriesAdapter(topCategories)
            binding.rvTopCategories.adapter = topCategoriesAdapter

            val recentTransactions = viewModel.getRecentTransactions()
            val recentTransactionsAdapter = RecentTransactionsAdapter(recentTransactions)
            binding.rvRecentTransactions.adapter = recentTransactionsAdapter
        }
    }

    private fun setupRadioGroupListener() {
        binding.filtersGroup.setOnCheckedChangeListener { _, checkedId ->
            val filter = when (checkedId) {
                R.id.rb_1w -> "1W"
                R.id.rb_1m -> "1M"
                R.id.rb_3m -> "3M"
                R.id.rb_6m -> "6M"
                R.id.rb_ytd -> "YTD"
                R.id.rb_1y -> "1Y"
                R.id.rb_all -> "ALL"
                else -> "6M"
            }
            binding.progressBar.visibility = View.VISIBLE
            viewModel.filterTransactionsForGraph(filter)
        }
    }

    private fun setupRecyclerViews() {
        binding.rvCreditCardDetails.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        binding.rvTopCategories.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)

        binding.rvRecentTransactions.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    private fun configureLineChart() {
        binding.lineChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            xAxis.isEnabled = false
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            setDrawGridBackground(false)
            setDrawBorders(false)

            setTouchEnabled(false)
            isDragEnabled = false
            setScaleEnabled(false)

            setViewPortOffsets(0f, 0f, 0f, 0f)
        }
    }

    private fun updateGraph(filteredData: Map<String, Double>) {
        val entries = filteredData.keys.sorted().mapIndexed { index, date ->
            Entry(index.toFloat(), filteredData[date]?.toFloat() ?: 0f)
        }

        val dataSet = LineDataSet(entries, "").apply {
            color = resources.getColor(R.color.blue_light, null)
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER

            setDrawFilled(true)
            fillDrawable = resources.getDrawable(R.drawable.gradient_chart_background, null)
        }

        binding.lineChart.apply {
            data = LineData(dataSet)
            setDrawBorders(false)
            description.isEnabled = false
            legend.isEnabled = false
            xAxis.isEnabled = false
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
            setDrawGridBackground(false)
            setTouchEnabled(false)
            isDragEnabled = false
            setScaleEnabled(false)
            setViewPortOffsets(0f, 0f, 0f, 0f)
        }
        binding.lineChart.invalidate()
    }

}