package com.finance.finvest.viewmodels

import CreditCardRepository
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.finance.finvest.datamodels.Category
import com.finance.finvest.datamodels.CreditCard
import com.finance.finvest.datamodels.Transaction
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

class CreditCardViewModel(private val repository: CreditCardRepository) : ViewModel() {


    private val _creditCards = MutableLiveData<List<CreditCard>>()
    val creditCards: LiveData<List<CreditCard>> = _creditCards

    private val _filteredTransactions = MutableLiveData<List<Transaction>>()
    val filteredTransactions: LiveData<List<Transaction>> = _filteredTransactions

    private val _totalPendingAmount = MutableLiveData<Double>()
    val totalPendingAmount: LiveData<Double> = _totalPendingAmount

    private val _creditCardDropdownList = MutableLiveData<List<String>>()
    val creditCardDropdownList: LiveData<List<String>> = _creditCardDropdownList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    val _selectedFilters = MutableLiveData<Map<String, String>>(emptyMap())
    val selectedFilters: LiveData<Map<String, String>> = _selectedFilters

    private val _currentPage = MutableLiveData<Int>(1)

    private val _filteredData = MutableLiveData<Map<String, Double>>()
    val filteredData: LiveData<Map<String, Double>> = _filteredData

    private var allTransactions: List<Transaction> =
        emptyList()
     var totalTransactionsCount: Int = 0
    private val _amountRange = MutableLiveData(Pair(0f, 10000f))
    val amountRange: LiveData<Pair<Float, Float>> = _amountRange

    init {
        loadCreditCards()
    }

    /**
     * Load credit card data and initialize caches, dropdowns, and graph data.
     */
    private fun loadCreditCards() {
        viewModelScope.launch {
            _isLoading.postValue(true)
            try {
                val cards = repository.getCreditCardData()
                _creditCards.postValue(cards)

                allTransactions = cards.flatMap { it.transactions }
                _filteredTransactions.postValue(allTransactions)

                _creditCardDropdownList.postValue(buildDropdownList(cards))
                calculateTotalPendingAmount(cards)

                filterTransactionsForGraph("6M")
            } catch (e: Exception) {
                _creditCards.postValue(emptyList())
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private fun buildDropdownList(cards: List<CreditCard>): List<String> {
        val dropdownList = mutableListOf("All Credit Cards")
        dropdownList.addAll(cards.map { "${it.bankName} ${it.cardType}" })
        return dropdownList
    }


    private fun calculateTotalPendingAmount(creditCards: List<CreditCard>) {
        val totalPending = creditCards.flatMap { it.transactions }
            .filter { it.status.equals("Pending", ignoreCase = true) }
            .sumOf { it.amount }
        _totalPendingAmount.postValue(totalPending.absoluteValue)
    }


    fun filterTransactionsForGraph(filter: String) {
        if (_creditCards.value.isNullOrEmpty()) {
            _creditCards.observeForever { creditCards ->
                if (!creditCards.isNullOrEmpty()) {
                    val dailyTransactions = parseDailyTransactions(creditCards)
                    _filteredData.postValue(applyFilterForGraph(dailyTransactions, filter))
                    _creditCards.removeObserver { }
                }
            }
        } else {
            val dailyTransactions = parseDailyTransactions(_creditCards.value ?: emptyList())
            _filteredData.postValue(applyFilterForGraph(dailyTransactions, filter))
        }
    }



    private fun applyFilterForGraph(
        dailyTransactions: Map<String, Double>,
        filter: String
    ): Map<String, Double> {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = Calendar.getInstance()

        val filteredDate: Calendar? = when (filter) {
            "1W" -> today.apply { add(Calendar.DAY_OF_YEAR, -7) }
            "1M" -> today.apply { add(Calendar.MONTH, -1) }
            "3M" -> today.apply { add(Calendar.MONTH, -3) }
            "6M" -> today.apply { add(Calendar.MONTH, -6) }
            "YTD" -> today.apply { set(Calendar.DAY_OF_YEAR, 1) }
            "1Y" -> today.apply { add(Calendar.YEAR, -1) }
            else -> null
        }

        return if (filteredDate != null) {
            val filteredDateString = formatter.format(filteredDate.time)
            dailyTransactions.filterKeys { date -> date >= filteredDateString }
        } else {
            dailyTransactions
        }
    }

    private fun parseDailyTransactions(creditCards: List<CreditCard>): Map<String, Double> {
        return creditCards.flatMap { it.transactions }
            .groupBy { it.date.substring(0, 10) }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
    }


    fun applyFilters(selectedFilters: Map<String, String>, amountRange: Pair<Float, Float>) {
        val (minAmount, maxAmount) = amountRange

        val filtered = allTransactions.filter { transaction ->
            val matchesAmount = transaction.amount.absoluteValue in minAmount..maxAmount
            val matchesCategory = selectedFilters["Categories"]?.let { it == "All" || transaction.category == it } ?: true
            val matchesStatus = selectedFilters["Status"]?.let { it == "All" || transaction.status.equals(it, ignoreCase = true) } ?: true
            val matchesDateRange = selectedFilters["Date Range"]?.let { it == "All time" || matchesDate(transaction.date, it) } ?: true

            matchesAmount && matchesCategory && matchesStatus && matchesDateRange
        }

        _filteredTransactions.postValue(filtered.take(10))
        totalTransactionsCount = filtered.size
        _currentPage.postValue(1)

        Log.d("CreditCardViewModel", "Filtered Transactions: ${filtered.size}")
    }

    fun removeFilter(filterKey: String) {
        val currentFilters = _selectedFilters.value?.toMutableMap() ?: mutableMapOf()
        currentFilters.remove(filterKey)
        _selectedFilters.postValue(currentFilters)
        applyFilters(currentFilters, Pair(0f, 10000f))
    }

    fun filterTransactionsByCard(position: Int) {
        _creditCards.value?.let { creditCards ->
            val transactions = if (position == 0) {
                creditCards.flatMap { it.transactions }
            } else {
                creditCards.getOrNull(position - 1)?.transactions ?: emptyList()
            }
            _filteredTransactions.postValue(transactions)
        }
    }


    fun loadMoreTransactions() {
        if (_isLoading.value == true || totalTransactionsCount == 0) return

        val currentPage = _currentPage.value ?: 1
        val pageSize = 10
        val startIndex = currentPage * pageSize
        val endIndex = (startIndex + pageSize).coerceAtMost(totalTransactionsCount)

        val currentList = _filteredTransactions.value ?: emptyList()
        val newItems = allTransactions.subList(startIndex, endIndex)

        _filteredTransactions.postValue(currentList + newItems)
        _currentPage.postValue(currentPage + 1)
    }


    fun sortTransactions(sortOrder: String) {
        _filteredTransactions.value?.let { transactions ->
            val sortedTransactions = when (sortOrder) {
                "amount_asc" -> transactions.sortedByDescending { it.amount }
                "amount_desc" -> transactions.sortedBy { it.amount }
                "date_asc" -> transactions.sortedBy { parseDate(it.date) }
                "date_desc" -> transactions.sortedByDescending { parseDate(it.date) }
                else -> transactions
            }
            _filteredTransactions.postValue(sortedTransactions)
        }
    }

    private fun parseDate(dateString: String): Date {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return dateFormat.parse(dateString) ?: Date()
    }

    fun getRecentTransactions(): List<Transaction> {
        return allTransactions.sortedByDescending { parseDate(it.date) }.take(5)
    }

    fun getTopCategories(): List<Category> {
        val totalSpend = allTransactions.sumOf { it.amount.absoluteValue }
        val categorySpend = allTransactions.groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount.absoluteValue } }

        return categorySpend.map { (category, spend) ->
            Category(
                name = category,
                percentage = if (totalSpend > 0) (spend / totalSpend * 100).toFloat() else 0f,
                amount = spend
            )
        }.sortedByDescending { it.amount }.take(3)
    }

    fun updateAmountRange(min: Float, max: Float) {
        _amountRange.value = Pair(min, max)
    }

    private fun matchesDate(transactionDate: String, dateRange: String): Boolean {
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val calendar = Calendar.getInstance()

        return try {
            val transactionDateParsed = formatter.parse(transactionDate) ?: return false

            when (dateRange) {
                "All time" -> true
                "Current month" -> {
                    val currentMonth = calendar.get(Calendar.MONTH)
                    val currentYear = calendar.get(Calendar.YEAR)
                    calendar.time = transactionDateParsed
                    calendar.get(Calendar.MONTH) == currentMonth && calendar.get(Calendar.YEAR) == currentYear
                }

                "Last month" -> {
                    calendar.add(Calendar.MONTH, -1)
                    val lastMonth = calendar.get(Calendar.MONTH)
                    val lastYear = calendar.get(Calendar.YEAR)
                    calendar.time = transactionDateParsed
                    calendar.get(Calendar.MONTH) == lastMonth && calendar.get(Calendar.YEAR) == lastYear
                }

                "This year" -> {
                    val currentYear = calendar.get(Calendar.YEAR)
                    calendar.time = transactionDateParsed
                    calendar.get(Calendar.YEAR) == currentYear
                }

                "Previous year" -> {
                    val previousYear = calendar.get(Calendar.YEAR) - 1
                    calendar.time = transactionDateParsed
                    calendar.get(Calendar.YEAR) == previousYear
                }

                else -> true
            }
        } catch (e: Exception) {
            Log.e("CreditCardViewModel", "Error parsing date: $transactionDate", e)
            false
        }
    }

    class Factory(private val repository: CreditCardRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CreditCardViewModel::class.java)) {
                return CreditCardViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}