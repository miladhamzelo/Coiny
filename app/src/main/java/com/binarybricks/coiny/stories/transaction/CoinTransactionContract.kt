import com.binarybricks.coiny.data.database.entities.CoinTransaction
import com.binarybricks.coiny.network.models.ExchangePair
import com.binarybricks.coiny.stories.BaseView
import java.math.BigDecimal
import java.util.HashMap

/**
Created by Pranay Airan 2/3/18.
 */

interface CoinTransactionContract {

    interface View : BaseView {
        fun onAllSupportedExchangesLoaded(exchangeCoinMap: HashMap<String, MutableList<ExchangePair>>)
        fun onCoinPriceLoaded(prices: MutableMap<String, BigDecimal>)
        fun onTransactionAdded()
    }

    interface Presenter {
        fun getAllSupportedExchanges()
        fun getPriceForPair(fromCoin: String, toCoin: String, exchange: String, timeStamp: String)
        fun addTransaction(transaction: CoinTransaction)
    }
}