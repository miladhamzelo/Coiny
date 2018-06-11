package com.binarybricks.coiny.stories.dashboard

import CoinDashboardContract
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.binarybricks.coiny.CoinyApplication
import com.binarybricks.coiny.R
import com.binarybricks.coiny.components.DashboardCoinModule
import com.binarybricks.coiny.components.DashboardHeaderModule
import com.binarybricks.coiny.components.GenericFooterModule
import com.binarybricks.coiny.components.ModuleItem
import com.binarybricks.coiny.components.historicalchartmodule.CoinDashboardPresenter
import com.binarybricks.coiny.data.PreferenceHelper
import com.binarybricks.coiny.data.database.entities.CoinTransaction
import com.binarybricks.coiny.data.database.entities.WatchedCoin
import com.binarybricks.coiny.network.models.CoinPrice
import com.binarybricks.coiny.network.schedulers.SchedulerProvider
import com.binarybricks.coiny.stories.CryptoCompareRepository
import com.binarybricks.coiny.stories.coinsearch.CoinSearchActivity
import com.binarybricks.coiny.utils.OnVerticalScrollListener
import com.binarybricks.coiny.utils.dpToPx
import kotlinx.android.synthetic.main.activity_dashboard.*
import java.util.HashMap
import kotlin.collections.ArrayList


class CoinDashboardActivity : AppCompatActivity(), CoinDashboardContract.View {

    companion object {
        @JvmStatic
        fun buildLaunchIntent(context: Context): Intent {
            return Intent(context, CoinDashboardActivity::class.java)
        }
    }

    private var nextMenuItem: MenuItem? = null

    private var coinDashboardList: MutableList<ModuleItem> = ArrayList()
    private var coinDashboardAdapter: CoinDashboardAdapter? = null
    private var watchedCoinList: List<WatchedCoin> = emptyList()
    private var coinTransactionList: List<CoinTransaction> = emptyList()

    private val schedulerProvider: SchedulerProvider by lazy {
        SchedulerProvider.getInstance()
    }

    private val dashboardRepository by lazy {
        DashboardRepository(schedulerProvider, CoinyApplication.database)
    }

    private val coinRepo by lazy {
        CryptoCompareRepository(schedulerProvider)
    }

    private val coinDashboardPresenter: CoinDashboardPresenter by lazy {
        CoinDashboardPresenter(schedulerProvider, dashboardRepository, coinRepo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val toolbar = findViewById<View>(R.id.toolbar)
        setSupportActionBar(toolbar as Toolbar?)
        supportActionBar?.title = ""


        initializeUI()

        coinDashboardPresenter.attachView(this)

        lifecycle.addObserver(coinDashboardPresenter)

        coinDashboardPresenter.loadWatchedCoinsAndTransactions()

        // get list of all exchanges
        coinDashboardPresenter.getAllSupportedExchanges()
    }

    private fun initializeUI() {
        val toolBarDefaultElevation = dpToPx(this, 8) // default elevation of toolbar

        rvDashboard.layoutManager = LinearLayoutManager(this)
        coinDashboardAdapter = CoinDashboardAdapter(PreferenceHelper.getDefaultCurrency(this), coinDashboardList, toolbarTitle)
        rvDashboard.adapter = coinDashboardAdapter
        rvDashboard.addOnScrollListener(object : OnVerticalScrollListener() {
            override fun onScrolled(offset: Int) {
                super.onScrolled(offset)
                toolbar.elevation = Math.min(toolBarDefaultElevation.toFloat(), offset.toFloat())
                toolbarTitle.alpha = Math.min(1.0f, offset / 60f) // approx height of header module
            }
        })
    }

    override fun onWatchedCoinsAndTransactionsLoaded(watchedCoinList: List<WatchedCoin>, coinTransactionList: List<CoinTransaction>) {

        this.watchedCoinList = watchedCoinList
        this.coinTransactionList = coinTransactionList

        getAllWatchedCoinsPrice()

        setupDashBoardAdapter(watchedCoinList, coinTransactionList)
    }

    private fun setupDashBoardAdapter(watchedCoinList: List<WatchedCoin>, coinTransactionList: List<CoinTransaction>) {

        // empty existing list
        coinDashboardList = ArrayList()

        // Add Dashboard Header with empty data
        coinDashboardList.add(DashboardHeaderModule.DashboardHeaderModuleData(watchedCoinList, coinTransactionList, hashMapOf()))

        watchedCoinList.forEach { watchedCoin ->
            coinDashboardList.add(DashboardCoinModule.DashboardCoinModuleData(watchedCoin, null, coinTransactionList))
        }

        coinDashboardList.add(GenericFooterModule.FooterModuleData(getString(R.string.crypto_compare), getString(R.string.crypto_compare_url)))

        showOrHideLoadingIndicator(false)

        coinDashboardAdapter?.coinDashboardList = coinDashboardList
        coinDashboardAdapter?.notifyDataSetChanged()
    }

    private fun getAllWatchedCoinsPrice() {
        // we have all the watched coins now get price for all the coins
        var fromSymbol = ""
        watchedCoinList.forEachIndexed { index, watchedCoin ->
            if (index != watchedCoinList.size - 1) {
                fromSymbol = fromSymbol + watchedCoin.coin.symbol + ","
            } else {
                fromSymbol += watchedCoin.coin.symbol
            }
        }
        coinDashboardPresenter.loadCoinsPrices(fromSymbol, PreferenceHelper.getDefaultCurrency(this))
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

    // Menu icons are inflated just as they were with actionbar
    override fun onCreateOptionsMenu(menu: Menu): Boolean { // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home_menu, menu)

        nextMenuItem = menu.findItem(R.id.action_search)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_search -> {
                startActivity(CoinSearchActivity.buildLaunchIntent(this))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onNetworkError(errorMessage: String) {
    }

    override fun showOrHideLoadingIndicator(showLoading: Boolean) {
        if (!showLoading) {
            loadingAnimation.cancelAnimation()
            loadingAnimation.visibility = View.GONE
        } else {
            loadingAnimation.visibility = View.VISIBLE
            loadingAnimation.playAnimation()
        }
    }
}
