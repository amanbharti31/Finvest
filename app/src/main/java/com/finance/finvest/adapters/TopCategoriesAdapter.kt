package com.finance.finvest.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.finance.finvest.R
import com.finance.finvest.databinding.ItemTopCategoryBinding
import com.finance.finvest.datamodels.Category

class TopCategoriesAdapter(
    private val categories: List<Category>
) : RecyclerView.Adapter<TopCategoriesAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(val binding: ItemTopCategoryBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemTopCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        with(holder.binding) {
            ivCategoryLogo.setImageResource(getLogoForCategory(category.name))
            tvCategoryName.text = category.name
            tvPercentage.text = String.format("%.2f%% of spends", category.percentage)
            tvAmount.text = String.format("$%.2f", category.amount)
        }
    }

    override fun getItemCount() = categories.size

    private fun getLogoForCategory(categoryName: String): Int {
        Log.d("Category", categoryName)
        return when (categoryName) {
            "Foods & Dining" -> R.drawable.round_fastfood_24
            "Apps & software" -> R.drawable.round_phonelink_24
            "Health & wellness" -> R.drawable.round_health_and_safety_24
            "Entertainment" -> R.drawable.round_tv_24
            "Housing" -> R.drawable.round_house_24
            else -> R.drawable.round_fastfood_24
        }
    }
}