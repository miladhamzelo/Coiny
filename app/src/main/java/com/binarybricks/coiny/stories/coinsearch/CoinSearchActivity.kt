package com.binarybricks.coiny.stories.coinsearch

import CoinSearchContract
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import com.binarybricks.coiny.CoinyApplication
import com.binarybricks.coiny.R
import com.binarybricks.coiny.data.database.entities.WatchedCoin
import com.binarybricks.coiny.network.schedulers.SchedulerProvider
import com.binarybricks.coiny.stories.CryptoCompareRepository
import com.binarybricks.coiny.stories.coindetails.CoinDetailsActivity
import com.crashlytics.android.Crashlytics
import kotlinx.android.synthetic.main.activity_coin_search.*

class CoinSearchActivity : AppCompatActivity(), CoinSearchContract.View {

    private var coinSearchAdapter: CoinSearchAdapter? = null
    private var isCoinInfoChanged = false

    companion object {
        @JvmStatic
        fun buildLaunchIntent(context: Context): Intent {
            return Intent(context, CoinSearchActivity::class.java)
        }
    }

    private val schedulerProvider: SchedulerProvider by lazy {
        SchedulerProvider.instance
    }

    private val coinRepo by lazy {
        CryptoCompareRepository(schedulerProvider, CoinyApplication.database)
    }

    private val coinSearchPresenter: CoinSearchPresenter by lazy {
        CoinSearchPresenter(schedulerProvider, coinRepo)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coin_search)

        val toolbar = findViewById<View>(R.id.toolbar)
        setSupportActionBar(toolbar as Toolbar?)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        rvSearchList.layoutManager = LinearLayoutManager(this)

        coinSearchPresenter.attachView(this)

        lifecycle.addObserver(coinSearchPresenter)

        coinSearchPresenter.loadAllCoins()

        Crashlytics.log("CoinSearchActivity")
    }

    override fun showOrHideLoadingIndicator(showLoading: Boolean) {
        if (!showLoading) {
            pbLoading.hide()
        } else {
            pbLoading.show()
        }
    }

    override fun onNetworkError(errorMessage: String) {
        Snackbar.make(rvSearchList, errorMessage, Snackbar.LENGTH_LONG)
    }

    override fun onCoinsLoaded(coinList: List<WatchedCoin>) {

        if (coinSearchAdapter == null) {
            coinSearchAdapter = CoinSearchAdapter(coinList)
            rvSearchList.adapter = coinSearchAdapter

            etSearchBar.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(filterText: Editable?) {
                    coinSearchAdapter?.filter?.filter(filterText.toString())
                }

                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }
            })

            coinSearchAdapter?.setOnSearchItemClickListener(object : CoinSearchAdapter.OnSearchItemClickListener {
                override fun onItemWatchedTicked(view: View, position: Int, watchedCoin: WatchedCoin, watched: Boolean) {
                    coinSearchPresenter.updateCoinWatchedStatus(watched, watchedCoin.coin.id, watchedCoin.coin.symbol)
                    isCoinInfoChanged = true
                }

                override fun showPurchasedItemRemovedMessage() {
                    Snackbar.make(rvSearchList, getString(R.string.coin_already_purchased), Snackbar.LENGTH_LONG).show()
                }

                override fun onSearchItemClick(view: View, position: Int, watchedCoin: WatchedCoin) {
                    val coinDetailsIntent = CoinDetailsActivity.buildLaunchIntent(this@CoinSearchActivity, watchedCoin)
                    startActivity(coinDetailsIntent)
                }
            })
        } else {
            // update the list
            coinSearchAdapter?.updateCoinList(coinList)
        }
    }

    override fun onCoinWatchedStatusUpdated(watched: Boolean, coinSymbol: String) {

        val statusText = if (watched) {
            getString(R.string.coin_added_to_watchlist, coinSymbol)
        } else {
            getString(R.string.coin_removed_to_watchlist, coinSymbol)
        }

        Snackbar.make(rvSearchList, statusText, Snackbar.LENGTH_LONG).show()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        when (item?.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (isCoinInfoChanged) {
            setResult(Activity.RESULT_OK)
        }

        finish()
    }
}
