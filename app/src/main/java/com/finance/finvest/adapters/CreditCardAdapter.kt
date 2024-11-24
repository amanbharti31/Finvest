package com.finance.finvest.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.finance.finvest.R
import com.finance.finvest.databinding.ItemCreditCardBinding
import com.finance.finvest.datamodels.CreditCard

class CreditCardAdapter (
    private val creditCards: List<CreditCard>
) : RecyclerView.Adapter<CreditCardAdapter.CreditCardViewHolder>() {

    inner class CreditCardViewHolder(val binding: ItemCreditCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreditCardViewHolder {
        val binding = ItemCreditCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CreditCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CreditCardViewHolder, position: Int) {
        val card = creditCards[position]
        with(holder.binding) {
            ivLogo.setImageResource(getLogoForBank(card.bankName))
            tvCardName.text = "${card.bankName} ${card.cardType}"
            tvCardNumber.text = "Asset ***${card.id}"
            tvBalance.text = String.format("$%.2f", card.balance)
        }
    }

    override fun getItemCount() = creditCards.size

    private fun getLogoForBank(bankName: String): Int {
        return when (bankName) {
            "Wells Fargo" -> R.drawable.wells
            "Citi" -> R.drawable.citi
            else -> R.drawable.citi
        }
    }
}