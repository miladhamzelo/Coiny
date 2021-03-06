package com.binarybricks.coiny.adapterdelegates

import android.arch.lifecycle.Lifecycle
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.binarybricks.coiny.components.cryptonewsmodule.CoinNewsModule
import com.binarybricks.coiny.network.schedulers.BaseSchedulerProvider
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate


/**
 * Created by Pranay Airan
 */

class CoinNewsAdapterDelegate(private val coinSymbol: String,
                              private val coinName: String,
                              private val schedulerProvider: BaseSchedulerProvider,
                              private val lifecycle: Lifecycle) : AdapterDelegate<List<Any>>() {

    override fun isForViewType(items: List<Any>, position: Int): Boolean {
        return items[position] is CoinNewsModule.CoinNewsModuleData
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val coinNewsModule = CoinNewsModule(schedulerProvider, coinSymbol, coinName)
        lifecycle.addObserver(coinNewsModule)
        val coinNewsModuleView = coinNewsModule.init(LayoutInflater.from(parent.context), parent)
        return CoinNewsViewHolder(coinNewsModuleView, coinNewsModule)
    }

    override fun onBindViewHolder(items: List<Any>, position: Int, holder: RecyclerView.ViewHolder, payloads: List<Any>) {
        // load data
        val historicalChartViewHolder = holder as CoinNewsViewHolder
        historicalChartViewHolder.loadCoinNewsData()
    }

    class CoinNewsViewHolder(itemView: View, private val coinNewsModule: CoinNewsModule) : RecyclerView.ViewHolder(itemView) {
        fun loadCoinNewsData() {
            coinNewsModule.loadNewsData(itemView)
        }
    }
}