package com.finance.finvest.datamodels

import android.os.Parcel
import android.os.Parcelable

data class FilterItem(
    val title: String,
    val options: List<String>
)
data class FilterData(
    val selectedFilters: Map<String, String>,
    val amountRange: Pair<Float, Float>
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readHashMap(String::class.java.classLoader) as Map<String, String>,
        Pair(parcel.readFloat(), parcel.readFloat())
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeMap(selectedFilters)
        parcel.writeFloat(amountRange.first)
        parcel.writeFloat(amountRange.second)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<FilterData> {
        override fun createFromParcel(parcel: Parcel): FilterData {
            return FilterData(parcel)
        }

        override fun newArray(size: Int): Array<FilterData?> {
            return arrayOfNulls(size)
        }
    }
}