package com.binarybricks.coinhood.stories.coindetails

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.ViewGroup
import com.binarybricks.coinhood.adapterdelegates.AboutCoinAdapterDelegate
import com.binarybricks.coinhood.adapterdelegates.HistoricalChartAdapterDelegate
import com.binarybricks.coinhood.network.schedulers.BaseSchedulerProvider
import com.binarybricks.coinhood.utils.ResourceProvider
import com.hannesdorfmann.adapterdelegates3.AdapterDelegatesManager


/**
 * Created by pranay airan on 1/18/18.
 *
 * based on http://hannesdorfmann.com/android/adapter-delegates
 */

class CoinDetailsAdapter(private val context: Context,
                         private val coinDetailList: List<Any>,
                         schedulerProvider: BaseSchedulerProvider,
                         resourceProvider: ResourceProvider) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val HISTORICAL_CHART = 0
    private val ABOUT_COIN = 1

    val delegates: AdapterDelegatesManager<List<Any>> = AdapterDelegatesManager()

    init {
        delegates.addDelegate(HISTORICAL_CHART, HistoricalChartAdapterDelegate(schedulerProvider, resourceProvider))
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