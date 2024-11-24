package com.finance.finvest.utils

import android.content.Context
import java.io.InputStream

class AssetReader (private val context: Context) {
    fun getJsonDataFromAsset(fileName: String): String? {
        return try {
            val inputStream: InputStream = context.assets.open(fileName)
            inputStream.bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}