package com.finance.finvest.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.finance.finvest.R
import com.finance.finvest.databinding.ItemRecentTransactionBinding
import com.finance.finvest.datamodels.Transaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

class  RecentTransactionsAdapter(
    private var transactions: List<Transaction>
) : RecyclerView.Adapter<RecentTransactionsAdapter.TransactionViewHolder>() {

    inner class TransactionViewHolder(val binding: ItemRecentTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(transaction: Transaction) {
            with(binding) {
                ivTransactionLogo.setImageResource(getLogoForMerchant(transaction.merchant))
                tvMerchant.text = transaction.merchant
                tvDate.text = formatDate(transaction.date)
                tvAmount.text = formatAmount(transaction.amount)
                tvStatus.text = transaction.status
                tvStatus.setTextColor(getStatusColor(transaction.status))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val binding = ItemRecentTransactionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount() = transactions.size


    fun updateData(newTransactions: List<Transaction>) {
        transactions = newTransactions
        Log.e("RecentTransactionsAdapter", "Adapter data updated: $transactions")
        notifyDataSetChanged()
    }

    private fun getLogoForMerchant(merchant: String): Int {
        return when (merchant) {
            "Uber" -> R.drawable.uber
            "Starbucks" -> R.drawable.starbucks
            "Mc Donalds" -> R.drawable.mcd
            "Ikea" -> R.drawable.ikea
            "JBL Technologies" -> R.drawable.jbl
            else -> R.drawable.round_home_24
        }
    }

    private fun formatDate(date: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
            val parsedDate = inputFormat.parse(date)
            outputFormat.format(parsedDate ?: Date())
        } catch (e: Exception) {
            "Invalid Date"
        }
    }

    private fun formatAmount(amount: Double): String {
        return String.format("$%.2f", amount.absoluteValue)
    }

    private fun getStatusColor(status: String): Int {
        return when (status.lowercase(Locale.getDefault())) {
            "completed" -> R.color.colorPrimary
            "pending" -> R.color.colorPrimary
            else -> R.color.colorPrimary
        }
    }
}