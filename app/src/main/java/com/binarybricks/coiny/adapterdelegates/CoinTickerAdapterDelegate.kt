package com.binarybricks.coiny.adapterdelegates

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.binarybricks.coiny.components.ModuleItem
import com.binarybricks.coiny.components.cointickermodule.CoinTickerModule
import com.binarybricks.coiny.data.database.CoinyDatabase
import com.binarybricks.coiny.network.schedulers.BaseSchedulerProvider
import com.binarybricks.coiny.utils.ResourceProvider
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import kotlinx.android.extensions.LayoutContainer

/**
 * Created by Pranay Airan
 */

class CoinTickerAdapterDelegate(
        private val coinName: String,
        private val schedulerProvider: BaseSchedulerProvider,
        private val coinyDatabase: CoinyDatabase?,
        private val resourceProvider: ResourceProvider
) : AdapterDelegate<List<ModuleItem>>() {

    private val coinTickerModule by lazy {
        CoinTickerModule(coinyDatabase, schedulerProvider, resourceProvider, coinName)
    }

    override fun isForViewType(items: List<ModuleItem>, position: Int): Boolean {
        return items[position] is CoinTickerModule.CoinTickerModuleData
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        val coinTickerModuleView = coinTickerModule.init(LayoutInflater.from(parent.context), parent)
        return CoinTickerViewHolder(coinTickerModuleView, coinTickerModule)
    }

    override fun onBindViewHolder(items: List<ModuleItem>, position: Int, holder: RecyclerView.ViewHolder, payloads: List<Any>) {
        // load data
        val coinTickerViewHolder = holder as CoinTickerViewHolder
        coinTickerViewHolder.loadTickerData()
    }

    class CoinTickerViewHolder(override val containerView: View, private val coinTickerModule: CoinTickerModule)
        : RecyclerView.ViewHolder(containerView), LayoutContainer {
        fun loadTickerData() {
            coinTickerModule.loadTickerData(containerView)
        }
    }

    fun cleanup() {
        coinTickerModule.cleanUp()
    }
}