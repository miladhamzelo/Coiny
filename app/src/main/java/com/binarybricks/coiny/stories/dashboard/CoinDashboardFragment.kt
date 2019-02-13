package com.binarybricks.coiny.stories.dashboard

import CoinDashboardContract
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.binarybricks.coiny.CoinyApplication
import com.binarybricks.coiny.R
import com.binarybricks.coiny.components.*
import com.binarybricks.coiny.data.PreferenceHelper
import com.binarybricks.coiny.data.database.entities.CoinTransaction
import com.binarybricks.coiny.data.database.entities.WatchedCoin
import com.binarybricks.coiny.network.models.CoinPrice
import com.binarybricks.coiny.network.models.CryptoCompareNews
import com.binarybricks.coiny.network.schedulers.SchedulerProvider
import com.binarybricks.coiny.stories.CryptoCompareRepository
import com.binarybricks.coiny.stories.coindetails.CoinDetailsPagerActivity
import com.binarybricks.coiny.stories.coinsearch.CoinSearchActivity
import com.binarybricks.coiny.utils.ResourceProvider
import com.binarybricks.coiny.utils.ResourceProviderImpl
import com.crashlytics.android.Crashlytics
import kotlinx.android.synthetic.main.fragment_dashboard.*
import kotlinx.android.synthetic.main.fragment_dashboard.view.*
import java.util.HashMap
import kotlin.collections.ArrayList

class CoinDashboardFragment : Fragment(), CoinDashboardContract.View {

    companion object {
        const val TAG = "CoinDashboardFragment"
        private const val COIN_SEARCH_CODE = 100
        private const val COIN_DETAILS_CODE = 101
    }

    private var coinDashboardList: MutableList<ModuleItem> = ArrayList()
    private var coinDashboardAdapter: CoinDashboardAdapter? = null
    private var watchedCoinList: List<WatchedCoin> = emptyList()
    private var coinTransactionList: List<CoinTransaction> = emptyList()

    private val schedulerProvider: SchedulerProvider by lazy {
        SchedulerProvider.instance
    }

    private val resourceProvider: ResourceProvider by lazy {
        ResourceProviderImpl(requireContext())
    }

    private val dashboardRepository by lazy {
        DashboardRepository(schedulerProvider, CoinyApplication.database)
    }

    private val coinRepo by lazy {
        CryptoCompareRepository(schedulerProvider, CoinyApplication.database)
    }

    private val coinDashboardPresenter: CoinDashboardPresenter by lazy {
        CoinDashboardPresenter(schedulerProvider, dashboardRepository, coinRepo)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val inflate = inflater.inflate(R.layout.fragment_dashboard, container, false)

        val toolbar = inflate.toolbar
        toolbar?.title = getString(R.string.market)

        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        coinDashboardPresenter.attachView(this)

        lifecycle.addObserver(coinDashboardPresenter)

        // empty existing list
        coinDashboardList = ArrayList()
        coinDashboardList.add(0, CarousalModule.CarousalModuleData(null))
        coinDashboardList.add(1, DashboardNewsModule.DashboardNewsModuleData(null))

        initializeUI(inflate)

        // get top coins
        coinDashboardPresenter.getTopCoinsByTotalVolume24hours(PreferenceHelper.getDefaultCurrency(context))

        // get news
        coinDashboardPresenter.getLatestNewsFromCryptoCompare()

        // get prices for watched coin
        coinDashboardPresenter.loadWatchedCoinsAndTransactions()

        Crashlytics.log("CoinDashboardFragment")

        return inflate
    }

    private fun initializeUI(inflatedView: View) {

        inflatedView.rvDashboard.layoutManager = LinearLayoutManager(context)

        coinDashboardAdapter = CoinDashboardAdapter(PreferenceHelper.getDefaultCurrency(context), resourceProvider,
                coinDashboardList, inflatedView.toolbarTitle)
        inflatedView.rvDashboard.adapter = coinDashboardAdapter

        inflatedView.swipeContainer.setOnRefreshListener {
            getAllWatchedCoinsPrice()
            inflatedView.swipeContainer.isRefreshing = false
        }
    }

    override fun onWatchedCoinsAndTransactionsLoaded(watchedCoinList: List<WatchedCoin>, coinTransactionList: List<CoinTransaction>) {

        this.watchedCoinList = watchedCoinList
        this.coinTransactionList = coinTransactionList

        getAllWatchedCoinsPrice()

        setupDashBoardAdapter(watchedCoinList, coinTransactionList)
    }

    private fun setupDashBoardAdapter(watchedCoinList: List<WatchedCoin>, coinTransactionList: List<CoinTransaction>) {

        // when we are refreshing the data we need to clear the coinDashboardList
        if (coinDashboardList.size > 2) {
            coinDashboardList = coinDashboardList.subList(0, 2)
        }

        // Add Dashboard Header with empty data
        // coinDashboardList.add(DashboardHeaderModule.DashboardHeaderModuleData(watchedCoinList, coinTransactionList, hashMapOf()))

        watchedCoinList.forEach { watchedCoin ->
            coinDashboardList.add(DashboardCoinModule.DashboardCoinModuleData(watchedCoin, null,
                    coinTransactionList, object : DashboardCoinModule.OnCoinItemClickListener {
                override fun onCoinClicked(watchedCoin: WatchedCoin) {
                    startActivityForResult(CoinDetailsPagerActivity.buildLaunchIntent(requireContext(), watchedCoin)
                            , COIN_DETAILS_CODE)
                }
            }))
        }

        coinDashboardList.add(DashboardAddNewCoinModule.DashboardAddNewCoinModuleData(object : DashboardAddNewCoinModule.OnAddItemClickListener {
            override fun onAddNewCoinClicked() {
                startActivityForResult(CoinSearchActivity.buildLaunchIntent(requireContext()), COIN_SEARCH_CODE)
            }
        }))

        coinDashboardList.add(GenericFooterModule.FooterModuleData(getString(R.string.crypto_compare), getString(R.string.crypto_compare_url)))

        coinDashboardAdapter?.coinDashboardList = coinDashboardList
        coinDashboardAdapter?.notifyDataSetChanged()
    }

    private fun getAllWatchedCoinsPrice() {
        // cryptocompare support only 100 coins in 1 shot. For safety we will support 95 and paginate
        val chunkedWatchedList = watchedCoinList.chunked(95)

        chunkedWatchedList.forEach {

            // we have all the watched coins now get price for all the coins
            var fromSymbol = ""
            it.forEachIndexed { index, watchedCoin ->
                if (index != it.size - 1) {
                    fromSymbol = fromSymbol + watchedCoin.coin.symbol + ","
                } else {
                    fromSymbol += watchedCoin.coin.symbol
                }
            }
            coinDashboardPresenter.loadCoinsPrices(fromSymbol, PreferenceHelper.getDefaultCurrency(context))
        }
    }

    override fun onCoinPricesLoaded(coinPriceListMap: HashMap<String, CoinPrice>) {

        val adapterDashboardList = coinDashboardAdapter?.coinDashboardList

        adapterDashboardList?.forEachIndexed { index, item ->
            if (item is DashboardCoinModule.DashboardCoinModuleData && coinPriceListMap.contains(item.watchedCoin.coin.symbol.toUpperCase())) {
                item.coinPrice = coinPriceListMap[item.watchedCoin.coin.symbol.toUpperCase()]
                coinDashboardAdapter?.notifyItemChanged(index)
            } else if (item is DashboardHeaderModule.DashboardHeaderModuleData) {
                item.coinPriceListMap = coinPriceListMap
                coinDashboardAdapter?.notifyItemChanged(index)
            }
        }

        // update dashboard card
    }

    override fun onTopCoinsByTotalVolumeLoaded(topCoins: List<CoinPrice>) {

        val topCardList = mutableListOf<TopCardModule.TopCardsModuleData>()
        topCoins.forEach {
            topCardList.add(TopCardModule.TopCardsModuleData("${it.fromSymbol}/${it.toSymbol}", it.price
                    ?: "0", it.changePercentage24Hour ?: "0", it.marketCap ?: "0",
                    it.fromSymbol ?: ""))
        }

        coinDashboardAdapter?.let {
            if (!it.coinDashboardList.isNullOrEmpty()) {
                it.coinDashboardList[0] = CarousalModule.CarousalModuleData(topCardList)
                coinDashboardAdapter?.notifyItemChanged(0)
            }
        }
    }

    override fun onCoinNewsLoaded(coinNews: List<CryptoCompareNews>) {

        coinDashboardAdapter?.let {
            if (!it.coinDashboardList.isNullOrEmpty() && it.coinDashboardList.size >= 1) {
                it.coinDashboardList[1] = DashboardNewsModule.DashboardNewsModuleData(coinNews)
                coinDashboardAdapter?.notifyItemChanged(1)
            }
        }
    }

    override fun onNetworkError(errorMessage: String) {
        Snackbar.make(rvDashboard, errorMessage, Snackbar.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (COIN_SEARCH_CODE == requestCode || COIN_DETAILS_CODE == requestCode) {
            if (resultCode == Activity.RESULT_OK) {
                coinDashboardPresenter.loadWatchedCoinsAndTransactions()
            }
        }

        super.onActivityResult(requestCode, resultCode, data)
    }
}
