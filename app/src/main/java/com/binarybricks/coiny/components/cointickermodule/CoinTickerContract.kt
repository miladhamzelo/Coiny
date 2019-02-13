import com.binarybricks.coiny.network.models.CryptoTicker
import com.binarybricks.coiny.stories.BaseView

/**
 * Created by Pranay Airan
 */

interface CoinTickerContract {

    interface View : BaseView {
        fun showOrHideLoadingIndicator(showLoading: Boolean = true)
        fun onPriceTickersLoaded(tickerData: List<CryptoTicker>)
    }

    interface Presenter {
        fun getCryptoTickers(coinName: String)
    }
}