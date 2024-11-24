package com.finance.finvest.datamodels

data class CreditCard(
    val id: Int,
    val bankName: String,
    val cardType: String,
    val balance: Double,
    val currency: String,
    val transactions: List<Transaction>
)

data class Transaction(
    val id: Int,
    val merchant: String,
    val amount: Double,
    val currency: String,
    val date: String,
    val status: String,
    val category: String
)
data class CreditCardResponse(
    val credit_cards: List<CreditCard>
)
data class Category(
    val name: String,
    val percentage: Float,
    val amount: Double
)