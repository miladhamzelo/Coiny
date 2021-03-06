package com.binarybricks.coiny.components.historicalchartmodule

import CoinDashboardContract
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import com.binarybricks.coiny.data.CoinyCache
import com.binarybricks.coiny.data.database.CoinyDatabase
import com.binarybricks.coiny.network.models.CoinPrice
import com.binarybricks.coiny.network.schedulers.BaseSchedulerProvider
import com.binarybricks.coiny.stories.BasePresenter
import com.binarybricks.coiny.stories.dashboard.DashboardRepository
import timber.log.Timber

/**
Created by Pranay Airan
 */

class CoinDashboardPresenter(private val schedulerProvider: BaseSchedulerProvider, private val coinyDatabase: CoinyDatabase?) :
    BasePresenter<CoinDashboardContract.View>(), CoinDashboardContract.Presenter,
    LifecycleObserver {

    private val dashboardRepository by lazy {
        DashboardRepository(schedulerProvider, coinyDatabase)
    }

    override fun loadWatchedCoins() {
        dashboardRepository.loadWatchedCoins()?.let {
            compositeDisposable.add(
                it.filter { it.isNotEmpty() }
                    .observeOn(schedulerProvider.ui())
                    .subscribe({ currentView?.onWatchedCoinsLoaded(it) }, { Timber.e(it.localizedMessage) })
            )
        }
    }

    override fun loadCoinsPrices(fromCurrencySymbol: String, toCurrencySymbol: String) {
        compositeDisposable.add(dashboardRepository.getCoinPriceFull(fromCurrencySymbol, toCurrencySymbol)
            .filter { it.size > 0 }
            .map { coinPriceList ->
                val coinPriceMap: HashMap<String, CoinPrice> = hashMapOf()
                coinPriceList.forEach { coinPrice ->
                    coinPrice.fromSymbol?.let { fromCurrencySymbol -> coinPriceMap.put(fromCurrencySymbol.toUpperCase(), coinPrice) }
                }
                CoinyCache.coinPriceMap.putAll(coinPriceMap)
                coinPriceMap
            }
            .observeOn(schedulerProvider.ui())
            .subscribe({ currentView?.onCoinPricesLoaded(it) }, { Timber.e(it.localizedMessage) }))
    }

    override fun loadAllSupportedCoins() {
        dashboardRepository.loadSupportedCoins()?.let {
            compositeDisposable.add(
                it.filter { it.isNotEmpty() }
                    .observeOn(schedulerProvider.ui())
                    .subscribe({ currentView?.onSupportedCoinsLoaded(it) }, { Timber.e(it.localizedMessage) })
            )
        }
    }

    // cleanup
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun cleanYourSelf() {
        detachView()
    }
}