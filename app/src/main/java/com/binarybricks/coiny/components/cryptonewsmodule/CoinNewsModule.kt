package com.binarybricks.coiny.components.cryptonewsmodule

import CryptoNewsContract
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.OnLifecycleEvent
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.binarybricks.coiny.R
import com.binarybricks.coiny.network.models.CryptoPanicNews
import com.binarybricks.coiny.network.schedulers.BaseSchedulerProvider
import com.binarybricks.coiny.stories.newslist.NewsListActivity
import com.binarybricks.coiny.utils.Formatters
import com.binarybricks.coiny.utils.getBrowserIntent
import kotlinx.android.synthetic.main.coin_news_module.view.*

/**
 * Created by Pragya Agrawal
 * A compound layout to see coin news
 */
class CoinNewsModule(private val schedulerProvider: BaseSchedulerProvider, private val coinSymbol: String, private val coinName: String) : LifecycleObserver, CryptoNewsContract.View {

    private lateinit var inflatedView: View

    private var cryptoPanicNews: CryptoPanicNews? = null

    private val cryptoNewsPresenter: CryptoNewsPresenter by lazy {
        CryptoNewsPresenter(schedulerProvider)
    }

    private val formatters: Formatters by lazy {
        Formatters()
    }

    fun init(layoutInflater: LayoutInflater, parent: ViewGroup?): View {

        val inflatedView = layoutInflater.inflate(R.layout.coin_news_module, parent, false)

        cryptoNewsPresenter.attachView(this)

        return inflatedView
    }

    fun loadNewsData(inflatedView: View) {
        this.inflatedView = inflatedView
        cryptoNewsPresenter.getCryptoNews(coinSymbol)
    }

    // cleanup
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun cleanYourSelf() {
        cryptoNewsPresenter.detachView()
        cryptoPanicNews = null
    }

    override fun showOrHideLoadingIndicator(showLoading: Boolean) {
        if (!showLoading) {
            inflatedView.loadingAnimation.cancelAnimation()
            inflatedView.loadingAnimation.visibility = View.GONE
        } else {
            inflatedView.loadingAnimation.visibility = View.VISIBLE
            inflatedView.loadingAnimation.playAnimation()
        }
    }

    override fun onNewsLoaded(cryptoPanicNews: CryptoPanicNews) {
        // show the first news
        val newsResult = cryptoPanicNews.results
        if (newsResult != null && newsResult.isNotEmpty()) {
            inflatedView.tvFirstArticleTitle.text = newsResult[0].title
            inflatedView.tvFirstArticleTime.text = formatters.parseAndFormatIsoDate(newsResult[0].created_at, true)
            inflatedView.clFirstArticle.setOnClickListener {
                inflatedView.context.startActivity(getBrowserIntent(newsResult[0].url))
            }

            if (newsResult.size > 1) {
                inflatedView.tvSecondArticleTitle.text = newsResult[1].title
                inflatedView.tvSecondArticleTime.text = formatters.parseAndFormatIsoDate(newsResult[1].created_at, true)
                inflatedView.clSecondArticle.setOnClickListener {
                    inflatedView.context.startActivity(getBrowserIntent(newsResult[1].url))
                }
            }

            if (newsResult.size > 2) {
                inflatedView.tvThirdArticleTitle.text = newsResult[2].title
                inflatedView.tvThirdArticleTime.text = formatters.parseAndFormatIsoDate(newsResult[2].created_at, true)
                inflatedView.clThirdArticle.setOnClickListener {
                    inflatedView.context.startActivity(getBrowserIntent(newsResult[2].url))
                }
            }

            inflatedView.tvMore.setOnClickListener {
                inflatedView.context.startActivity(NewsListActivity.buildLaunchIntent(inflatedView.context, coinName, cryptoPanicNews))
            }
        }
    }

    override fun onNetworkError(errorMessage: String) {
        Snackbar.make(inflatedView, errorMessage, Snackbar.LENGTH_LONG).show()
    }

    class CoinNewsModuleData
}