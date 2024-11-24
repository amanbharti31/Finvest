package com.finance.finvest.activities

import CreditCardRepository
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.finance.finvest.R
import com.finance.finvest.adapters.RecentTransactionsAdapter
import com.finance.finvest.databinding.ActivityTransactionsBinding
import com.finance.finvest.datamodels.FilterData
import com.finance.finvest.fragments.FilterFragment
import com.finance.finvest.utils.AssetReader
import com.finance.finvest.viewmodels.CreditCardViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class TransactionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTransactionsBinding
    private lateinit var transactionsAdapter: RecentTransactionsAdapter

    private val viewModel: CreditCardViewModel by viewModels {
        CreditCardViewModel.Factory(CreditCardRepository(AssetReader(this)))
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTransactionsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupToolbar()
        setupDropdown()
        setupRecyclerView()
        setupFilterAndSortButtons()
        observeViewModel()
        setupFilterResultObserver()
        setupPaginationListener()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }
    }

    private fun setupDropdown() {
        viewModel.creditCardDropdownList.observe(this) { dropdownList ->
            val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, dropdownList)
            binding.spinnerCreditCards.setAdapter(adapter)
            binding.spinnerCreditCards.setText(dropdownList[0], false)
            viewModel.filterTransactionsByCard(0)
        }

        binding.spinnerCreditCards.setOnItemClickListener { _, _, position, _ ->
            viewModel.filterTransactionsByCard(position)
        }
    }



    private fun setupRecyclerView() {
        transactionsAdapter = RecentTransactionsAdapter(listOf())
        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvTransactions.adapter = transactionsAdapter
        Log.e("TransactionsActivity", "RecyclerView adapter set")

        binding.rvTransactions.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1)) {
                    binding.progressBar.visibility = View.VISIBLE // Show progress bar
                    viewModel.loadMoreTransactions() // Notify ViewModel to load more transactions
                }
            }
        })
    }

    private fun setupFilterAndSortButtons() {
        binding.btnSort.setOnClickListener {
            showSortDialog()
        }

        binding.btnFilters.setOnClickListener {
            showFilterFragment() // Show the filter fragment
        }
    }

    private fun showSortDialog() {
        val sortOptions = arrayOf("Low to High Amount", "High to Low Amount", "Date New to Old")
        MaterialAlertDialogBuilder(this,R.style.CustomAlertDialogTheme)
            .setTitle("Sort By")
            .setItems(sortOptions) { _, which ->
                when (which) {
                    0 -> viewModel.sortTransactions("amount_asc")
                    1 -> viewModel.sortTransactions("amount_desc")
                    2 -> viewModel.sortTransactions("date_desc")
                }
            }
            .show()
    }

    private fun showFilterFragment() {
        val selectedFilters = viewModel.selectedFilters.value ?: emptyMap()
        val amountRange = viewModel.amountRange.value ?: Pair(0f, 10000f)

        val filterFragment = FilterFragment.newInstance(
            selectedFilters,
            amountRange
        )
        filterFragment.show(supportFragmentManager, "FilterFragment")
    }

    private fun observeViewModel() {
        Log.e("TransactionsActivity", "Setting up LiveData observers")

        viewModel.filteredTransactions.observe(this) { transactions ->
            Log.d("TransactionsActivity", "Filtered Transactions: $transactions")
            transactionsAdapter.updateData(transactions)
            Log.d("TransactionsActivity", "Filtered Transactions: ${transactions.size} items")
        }

        viewModel.selectedFilters.observe(this) { filters ->
            binding.layoutSelectedFilters.removeAllViews()
            filters.forEach { filter ->
                addFilterChip(filter.toString())
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            Log.d("TransactionsActivity", "Loading state: $isLoading")

        }

    }

    private fun setupPaginationListener() {
        binding.rvTransactions.clearOnScrollListeners()

        binding.rvTransactions.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                Log.d("ScrollListener", "FirstVisible: $firstVisibleItemPosition, TotalItems: $totalItemCount, VisibleItems: $visibleItemCount")

                if (!viewModel.isLoading.value!!) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount &&
                        firstVisibleItemPosition >= 0 &&
                        totalItemCount >= 10
                    ) {
                        viewModel.loadMoreTransactions()
                    }
                }
            }
        })
    }


    private fun setupFilterResultObserver() {
        supportFragmentManager.setFragmentResultListener("FILTERS_RESULT", this) { _, bundle ->
            val filterData = bundle.getParcelable<FilterData>("filterData")
            filterData?.let {
                viewModel.applyFilters(it.selectedFilters, it.amountRange)
                viewModel._selectedFilters.postValue(it.selectedFilters)
                viewModel.updateAmountRange(it.amountRange.first, it.amountRange.second)

                binding.layoutSelectedFilters.removeAllViews()
                it.selectedFilters.forEach { filter ->
                    addFilterChip(filter.toString())
                }
            }
        }
    }


    private fun addFilterChip(filter: String) {
        val chip = Chip(this).apply {
            text = filter
            isCloseIconVisible = true
            setOnCloseIconClickListener {
                viewModel.removeFilter(filter)
            }
        }
        binding.layoutSelectedFilters.addView(chip)
    }
}