package com.binarybricks.coiny.stories.coin

import CoinContract
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.*
import com.binarybricks.coiny.CoinyApplication
import com.binarybricks.coiny.R
import com.binarybricks.coiny.components.*
import com.binarybricks.coiny.components.cointickermodule.CoinTickerModule
import com.binarybricks.coiny.components.cryptonewsmodule.CoinNewsModule
import com.binarybricks.coiny.components.historicalchartmodule.HistoricalChartModule
import com.binarybricks.coiny.data.PreferenceHelper
import com.binarybricks.coiny.data.database.entities.CoinTransaction
import com.binarybricks.coiny.data.database.entities.WatchedCoin
import com.binarybricks.coiny.network.models.CoinPrice
import com.binarybricks.coiny.network.schedulers.SchedulerProvider
import com.binarybricks.coiny.stories.CryptoCompareRepository
import com.binarybricks.coiny.stories.coindetails.CoinDetailsActivity
import com.binarybricks.coiny.stories.coindetails.CoinDetailsPagerActivity
import com.binarybricks.coiny.utils.ResourceProvider
import com.binarybricks.coiny.utils.ResourceProviderImpl
import com.binarybricks.coiny.utils.defaultExchange
import com.binarybricks.coiny.utils.dpToPx
import com.binarybricks.coiny.utils.ui.OnVerticalScrollListener
import com.crashlytics.android.Crashlytics
import kotlinx.android.synthetic.main.activity_pager_coin_details.*
import kotlinx.android.synthetic.main.fragment_coin_details.*
import kotlinx.android.synthetic.main.fragment_coin_details.view.*
import java.math.BigDecimal

class CoinFragment : Fragment(), CoinContract.View {

    private val coinDetailList: MutableList<ModuleItem> = mutableListOf()
    private var coinAdapter: CoinAdapter? = null
    private var coinPrice: CoinPrice? = null
    private var watchedMenuItem: MenuItem? = null
    private var isCoinWatched = false
    private var isCoinedPurchased = false
    private var watchedCoin: WatchedCoin? = null

    private val schedulerProvider: SchedulerProvider by lazy {
        SchedulerProvider.instance
    }

    private val coinRepo by lazy {
        CryptoCompareRepository(schedulerProvider, CoinyApplication.database)
    }

    private val coinPresenter: CoinPresenter by lazy {
        CoinPresenter(schedulerProvider, coinRepo)
    }

    val resourceProvider: ResourceProvider by lazy {
        ResourceProviderImpl(requireContext())
    }
    val toCurrency: String by lazy {
        PreferenceHelper.getDefaultCurrency(context?.applicationContext)
    }

    companion object {
        private const val WATCHED_COIN = "WATCHED_COIN"
        @JvmStatic
        fun getArgumentBundle(watchedCoin: WatchedCoin): Bundle {
            val bundle = Bundle()
            bundle.putParcelable(WATCHED_COIN, watchedCoin)
            return bundle
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val inflate = inflater.inflate(R.layout.fragment_coin_details, container, false)

        watchedCoin = arguments?.getParcelable(WATCHED_COIN)

        setHasOptionsMenu(true)

        watchedCoin?.let {

            coinPresenter.attachView(this)

            lifecycle.addObserver(coinPresenter)

            inflate.rvCoinDetails.layoutManager = LinearLayoutManager(context)

            val toolBarDefaultElevation = dpToPx(context, 12) // default elevation of toolbar

            inflate.rvCoinDetails.addOnScrollListener(object : OnVerticalScrollListener() {
                override fun onScrolled(offset: Int) {
                    super.onScrolled(offset)
                    (activity as? CoinDetailsPagerActivity)?.toolbar?.elevation = Math.min(toolBarDefaultElevation.toFloat(), offset.toFloat())
                    (activity as? CoinDetailsActivity)?.toolbar?.elevation = Math.min(toolBarDefaultElevation.toFloat(), offset.toFloat())
                }
            })

            // load data
            coinPresenter.loadCurrentCoinPrice(it, toCurrency)

            if (it.purchaseQuantity > BigDecimal.ZERO) {
                isCoinedPurchased = true
                isCoinWatched = true
            } else if (it.watched) {
                isCoinWatched = true
            }
        }

        Crashlytics.log("CoinFragment")

        return inflate
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {

        inflater?.inflate(R.menu.coin_details_menu, menu)

        watchedMenuItem = menu?.findItem(R.id.action_watch)

        changeCoinMenu(isCoinWatched, isCoinedPurchased)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_watch -> {
                isCoinWatched = !isCoinWatched
                changeCoinMenu(isCoinWatched, isCoinedPurchased)
                changeCoinWatchedStatus(isCoinWatched)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun changeCoinWatchedStatus(isCoinWatched: Boolean) {
        watchedCoin?.let {
            coinPresenter.updateCoinWatchedStatus(isCoinWatched, it.coin.id, it.coin.symbol)

            (activity as? CoinDetailsPagerActivity)?.isCoinInfoChanged = true
        }
    }

    private fun changeCoinMenu(isCoinWatched: Boolean, isCoinPurchased: Boolean) {
        if (!isCoinPurchased) {
            if (isCoinWatched) {
                watchedMenuItem?.icon = context?.getDrawable(R.drawable.ic_watching)
                watchedMenuItem?.title = context?.getString(R.string.remove_to_watchlist)
            } else {
                watchedMenuItem?.icon = context?.getDrawable(R.drawable.ic_watch)
                watchedMenuItem?.title = context?.getString(R.string.add_to_watchlist)
            }
        } else {
            watchedMenuItem?.isVisible = false
        }
    }

    override fun onCoinWatchedStatusUpdated(watched: Boolean, coinSymbol: String) {

        val statusText = if (watched) {
            getString(R.string.coin_added_to_watchlist, coinSymbol)
        } else {
            getString(R.string.coin_removed_to_watchlist, coinSymbol)
        }

        Snackbar.make(rvCoinDetails, statusText, Snackbar.LENGTH_LONG).show()
    }

    override fun onNetworkError(errorMessage: String) {
        Snackbar.make(rvCoinDetails, errorMessage, Snackbar.LENGTH_LONG).show()
    }

    override fun onCoinPriceLoaded(coinPrice: CoinPrice?, watchedCoin: WatchedCoin) {

        this.coinPrice = coinPrice

        coinDetailList.add(HistoricalChartModule.HistoricalChartModuleData(coinPrice))

        // coinDetailList.add(AddCoinModule.AddCoinModuleData(watchedCoin.coin))

        if (coinPrice != null) {
            coinDetailList.add(CoinStatsticsModule.CoinStatisticsModuleData(coinPrice))

            coinDetailList.add(CoinInfoModule.CoinInfoModuleData(coinPrice.market
                    ?: defaultExchange, watchedCoin.coin.algorithm, watchedCoin.coin.proofType))
        }

        coinDetailList.add(CoinTickerModule.CoinTickerModuleData())

        coinDetailList.add(CoinNewsModule.CoinNewsModuleData())

        coinDetailList.add(AboutCoinModule.AboutCoinModuleData(watchedCoin.coin))
        coinAdapter = CoinAdapter(watchedCoin.coin.symbol, toCurrency, watchedCoin.coin.coinName,
                coinDetailList, CoinyApplication.database, schedulerProvider, resourceProvider)

        view?.rvCoinDetails?.adapter = coinAdapter
        coinAdapter?.notifyDataSetChanged()

        coinPresenter.loadRecentTransaction(watchedCoin.coin.symbol)

        coinDetailList.add(GenericFooterModule.FooterModuleData())
    }

    override fun onRecentTransactionLoaded(coinTransactionList: List<CoinTransaction>) {
        if (!coinTransactionList.isEmpty()) {
            coinPrice?.let {
                // add position module
                coinDetailList.add(2, CoinPositionCard.CoinPositionCardModuleData(it, coinTransactionList))
                coinAdapter?.coinDetailList = coinDetailList
                coinAdapter?.notifyItemChanged(2)
            }

            // add transaction module
            coinDetailList.add(4, CoinTransactionHistoryModule.CoinTransactionHistoryModuleData(coinTransactionList))
            coinAdapter?.coinDetailList = coinDetailList
            coinAdapter?.notifyItemChanged(4)
        }
    }

    override fun onDestroy() {
        coinAdapter?.cleanup()
        super.onDestroy()
    }
}
