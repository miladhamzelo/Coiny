package com.binarybricks.coiny.components.cointickermodule

import CoinTickerContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.binarybricks.coiny.R
import com.binarybricks.coiny.components.Module
import com.binarybricks.coiny.components.ModuleItem
import com.binarybricks.coiny.data.PreferenceHelper
import com.binarybricks.coiny.data.database.CoinyDatabase
import com.binarybricks.coiny.network.BASE_CRYPTOCOMPARE_IMAGE_URL
import com.binarybricks.coiny.network.models.CryptoTicker
import com.binarybricks.coiny.network.schedulers.BaseSchedulerProvider
import com.binarybricks.coiny.stories.ticker.CoinTickerActivity
import com.binarybricks.coiny.utils.Formatters
import com.binarybricks.coiny.utils.ResourceProvider
import com.binarybricks.coiny.utils.getUrlWithoutParameters
import com.binarybricks.coiny.utils.openCustomTab
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import kotlinx.android.synthetic.main.coin_ticker_module.view.*
import java.util.*

/**
 * Created by Pranay Airan
 * A compound layout to see coin ticker
 */
class CoinTickerModule(
        private val coinyDatabase: CoinyDatabase?,
        private val schedulerProvider: BaseSchedulerProvider,
        private val resourceProvider: ResourceProvider,
        private val coinName: String
) : Module(), CoinTickerContract.View {

    private lateinit var inflatedView: View

    private val coinTickerRepository by lazy {
        CoinTickerRepository(schedulerProvider, coinyDatabase)
    }
    private val coinTickerPresenter: CoinTickerPresenter by lazy {
        CoinTickerPresenter(schedulerProvider, coinTickerRepository, resourceProvider)
    }

    private val formatter: Formatters by lazy {
        Formatters(resourceProvider)
    }

    private val cropCircleTransformation by lazy {
        CropCircleTransformation()
    }

    override fun init(layoutInflater: LayoutInflater, parent: ViewGroup?): View {

        val inflatedView = layoutInflater.inflate(R.layout.coin_ticker_module, parent, false)

        coinTickerPresenter.attachView(this)

        return inflatedView
    }

    fun loadTickerData(inflatedView: View) {
        this.inflatedView = inflatedView
        coinTickerPresenter.getCryptoTickers(coinName.toLowerCase())
    }

    override fun showOrHideLoadingIndicator(showLoading: Boolean) {
        if (!showLoading) {
            inflatedView.pbLoading.visibility = View.GONE
        } else {
            inflatedView.pbLoading.visibility = View.VISIBLE
        }
    }

    override fun onPriceTickersLoaded(tickerData: List<CryptoTicker>) {
        val currency = Currency.getInstance(PreferenceHelper.getDefaultCurrency(inflatedView.context))
        // show the first news
        if (tickerData.isNotEmpty()) {

            inflatedView.tvFirstFromCoin.text = tickerData[0].base
            inflatedView.tvFirstToPrice.text = tickerData[0].target
            inflatedView.tvFirstPrice.text = formatter.formatAmount(tickerData[0].last, currency, true)
            inflatedView.tvFirstExchange.text = tickerData[0].marketName
            inflatedView.tvFirstVolume.text = formatter.formatAmount(tickerData[0].convertedVolumeUSD, currency, true)
            inflatedView.ivFirstExchange.visibility = View.VISIBLE
            Picasso.get().load(BASE_CRYPTOCOMPARE_IMAGE_URL + tickerData[0].imageUrl).error(R.mipmap.ic_launcher_round)
                    .transform(cropCircleTransformation)
                    .into(inflatedView.ivFirstExchange)

            inflatedView.clFirstMarket.setOnClickListener {
                if (tickerData[0].exchangeUrl.isNotBlank()) {
                    openCustomTab(getUrlWithoutParameters(tickerData[0].exchangeUrl), inflatedView.context)
                }
            }

            if (tickerData.size > 1) {
                inflatedView.tvSecondFromCoin.text = tickerData[1].base
                inflatedView.tvSecondToPrice.text = tickerData[1].target
                inflatedView.tvSecondPrice.text = formatter.formatAmount(tickerData[1].last, currency, true)
                inflatedView.tvSecondExchange.text = tickerData[1].marketName
                inflatedView.tvSecondVolume.text = formatter.formatAmount(tickerData[1].convertedVolumeUSD, currency, true)
                inflatedView.clSecondMarket.setOnClickListener {
                    if (tickerData[1].exchangeUrl.isNotBlank()) {
                        openCustomTab(getUrlWithoutParameters(tickerData[1].exchangeUrl), inflatedView.context)
                    }
                }
                inflatedView.ivSecondExchange.visibility = View.VISIBLE
                Picasso.get().load(BASE_CRYPTOCOMPARE_IMAGE_URL + tickerData[1].imageUrl).error(R.mipmap.ic_launcher_round)
                        .transform(cropCircleTransformation)
                        .into(inflatedView.ivSecondExchange)
            }

            if (tickerData.size > 2) {
                inflatedView.tvThirdFromCoin.text = tickerData[0].base
                inflatedView.tvThirdToPrice.text = tickerData[0].target
                inflatedView.tvThirdPrice.text = formatter.formatAmount(tickerData[2].last, currency, true)
                inflatedView.tvThirdExchange.text = tickerData[2].marketName
                inflatedView.tvThirdVolume.text = formatter.formatAmount(tickerData[2].convertedVolumeUSD, currency, true)
                inflatedView.clThirdMarket.setOnClickListener {
                    if (tickerData[2].exchangeUrl.isNotBlank()) {
                        openCustomTab(getUrlWithoutParameters(tickerData[2].exchangeUrl), inflatedView.context)
                    }
                }
                inflatedView.ivThirdExchange.visibility = View.VISIBLE
                Picasso.get().load(BASE_CRYPTOCOMPARE_IMAGE_URL + tickerData[2].imageUrl).error(R.mipmap.ic_launcher_round)
                        .transform(cropCircleTransformation)
                        .into(inflatedView.ivThirdExchange)
            }

            inflatedView.tvMore.setOnClickListener {
                inflatedView.context.startActivity(CoinTickerActivity.buildLaunchIntent(inflatedView.context, coinName))
            }
        } else {
            inflatedView.tvTickerError.visibility = View.VISIBLE
            inflatedView.tickerContentGroup.visibility = View.GONE
        }
    }

    override fun onNetworkError(errorMessage: String) {
        inflatedView.tvTickerError.visibility = View.VISIBLE
        inflatedView.tickerContentGroup.visibility = View.GONE
    }

    override fun cleanUp() {
        coinTickerPresenter.detachView()
    }

    class CoinTickerModuleData : ModuleItem
}