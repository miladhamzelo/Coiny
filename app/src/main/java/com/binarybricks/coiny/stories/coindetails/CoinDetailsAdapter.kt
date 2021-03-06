package com.binarybricks.coiny.stories.coindetails

import android.arch.lifecycle.Lifecycle
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.binarybricks.coiny.adapterdelegates.*
import com.binarybricks.coiny.network.schedulers.BaseSchedulerProvider
import com.binarybricks.coiny.utils.ResourceProvider
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager


/**
 Created by Pranay Airan 1/18/18.
 *
 * based on http://hannesdorfmann.com/android/adapter-delegates
 */

class CoinDetailsAdapter(fromCurrency: String,
                         toCurrency: String,
                         coinName: String,
                         lifecycle: Lifecycle,
                         private val coinDetailList: List<Any>,
                         schedulerProvider: BaseSchedulerProvider,
                         resourceProvider: ResourceProvider) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val HISTORICAL_CHART = 0
    private val ADD_COIN = 1
    private val COIN_POSITION = 2
    private val COIN_INFO = 3
    private val COIN_NEWS = 4
    private val COIN_STATS = 5
    private val ABOUT_COIN = 6

    private val delegates: AdapterDelegatesManager<List<Any>> = AdapterDelegatesManager()

    init {
        delegates.addDelegate(HISTORICAL_CHART, HistoricalChartAdapterDelegate(fromCurrency, toCurrency, schedulerProvider, lifecycle, resourceProvider))
        delegates.addDelegate(ADD_COIN, AddCoinAdapterDelegate())
        delegates.addDelegate(COIN_POSITION, CoinPositionAdapterDelegate())
        delegates.addDelegate(COIN_INFO, CoinInfoAdapterDelegate())
        delegates.addDelegate(COIN_NEWS, CoinNewsAdapterDelegate(fromCurrency, coinName, schedulerProvider, lifecycle))
        delegates.addDelegate(COIN_STATS, CoinStatsAdapterDelegate())
        delegates.addDelegate(ABOUT_COIN, AboutCoinAdapterDelegate())
    }

    override fun getItemViewType(position: Int): Int {
        return delegates.getItemViewType(coinDetailList, position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return delegates.onCreateViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
        delegates.onBindViewHolder(coinDetailList, position, viewHolder)
    }

    override fun getItemCount(): Int {
        return coinDetailList.size
    }
}