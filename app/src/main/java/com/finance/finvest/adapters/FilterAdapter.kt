package com.finance.finvest.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.finance.finvest.databinding.ItemFilterBinding
import com.finance.finvest.datamodels.FilterItem
import com.google.android.material.chip.Chip

class FilterAdapter(
    private val filters: List<FilterItem>,
    private val onOptionSelected: (FilterItem, String) -> Unit
) : RecyclerView.Adapter<FilterAdapter.FilterViewHolder>() {

    private val selectedOptions = mutableMapOf<String, String>()

    var selectedFilters: Map<String, String> = emptyMap()
        set(value) {
            field = value
            selectedOptions.clear()
            selectedOptions.putAll(value)
            notifyDataSetChanged()
        }

    inner class FilterViewHolder(val binding: ItemFilterBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FilterViewHolder {
        val binding = ItemFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FilterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FilterViewHolder, position: Int) {
        val filterItem = filters[position]
        with(holder.binding) {
            tvFilterTitle.text = filterItem.title

            chipGroupOptions.removeAllViews()
            filterItem.options.forEach { option ->
                val chip = Chip(chipGroupOptions.context).apply {
                    text = option
                    isCheckable = true
                    isChecked = selectedOptions[filterItem.title] == option
                }
                chipGroupOptions.addView(chip)

                chip.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedOptions[filterItem.title] = option
                        onOptionSelected(filterItem, option)
                        notifyDataSetChanged()
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = filters.size
}