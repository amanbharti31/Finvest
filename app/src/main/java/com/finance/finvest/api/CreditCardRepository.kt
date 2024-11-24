
import com.finance.finvest.datamodels.CreditCard
import com.finance.finvest.datamodels.CreditCardResponse
import com.finance.finvest.utils.AssetReader
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreditCardRepository(private val assetReader: AssetReader) {
    suspend fun getCreditCardData(): List<CreditCard> {
        return withContext(Dispatchers.IO) {
            val jsonFileString = assetReader.getJsonDataFromAsset("creditcard.json")
            jsonFileString?.let {
                val gson = Gson()
                val creditCardResponse = gson.fromJson(it, CreditCardResponse::class.java)
                return@withContext creditCardResponse.credit_cards
            }
            emptyList()
        }
    }
}
