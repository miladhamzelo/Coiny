package com.binarybricks.coiny.stories.settings

import SettingsContract
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.binarybricks.coiny.BuildConfig
import com.binarybricks.coiny.CoinyApplication
import com.binarybricks.coiny.R
import com.binarybricks.coiny.data.PreferenceHelper
import com.binarybricks.coiny.network.schedulers.SchedulerProvider
import com.binarybricks.coiny.stories.CryptoCompareRepository
import com.binarybricks.coiny.utils.*
import com.crashlytics.android.Crashlytics
import com.mynameismidori.currencypicker.CurrencyPicker
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.fragment_settings.view.*
import timber.log.Timber

class SettingsFragment : Fragment(), SettingsContract.View {

    companion object {
        const val TAG = "SettingsFragment"
    }

    private val schedulerProvider: SchedulerProvider by lazy {
        SchedulerProvider.instance
    }

    private val coinRepo by lazy {
        CryptoCompareRepository(schedulerProvider, CoinyApplication.database)
    }

    private val settingsPresenter: SettingsPresenter by lazy {
        SettingsPresenter(schedulerProvider, coinRepo)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val inflate = inflater.inflate(R.layout.fragment_settings, container, false)

        val toolbar = inflate.toolbar
        toolbar?.title = getString(R.string.settings)

        (activity as AppCompatActivity).setSupportActionBar(toolbar)

        settingsPresenter.attachView(this)

        lifecycle.addObserver(settingsPresenter)

        initializeUI(inflate)

        Crashlytics.log("SettingsFragment")

        return inflate
    }

    private fun initializeUI(inflatedView: View) {

        val currency = PreferenceHelper.getPreference(requireContext(), PreferenceHelper.DEFAULT_CURRENCY, defaultCurrency)

        inflatedView.tvCurrencyValue.text = currency

        inflatedView.clCurrency.setOnClickListener {
            openCurrencyPicker()
        }

        inflatedView.clCurrencyList.setOnClickListener {
            settingsPresenter.refreshCoinList(currency)
            inflatedView.ivCurrencyList.visibility = View.INVISIBLE
            inflatedView.pbLoadingCurrencyList.visibility = View.VISIBLE
        }

        inflatedView.clExchangeList.setOnClickListener {
            settingsPresenter.refreshExchangeList()
            inflatedView.ivExchangeList.visibility = View.INVISIBLE
            inflatedView.pbLoadingExchangeList.visibility = View.VISIBLE
        }

        inflatedView.clRate.setOnClickListener {
            rateCoiny(requireContext())
        }

        inflatedView.clShare.setOnClickListener {
            shareCoiny(requireContext())
        }

        inflatedView.clFeedback.setOnClickListener {
            sendEmail(requireContext(), getString(R.string.email_address), getString(R.string.feedback_coiny), "Hello, \n")
        }

        inflatedView.clPrivacy.setOnClickListener {
            openCustomTab(getString(R.string.privacyPolicyUrl), requireContext())
        }

        inflatedView.tvAppVersionValue.text = BuildConfig.VERSION_NAME

        inflatedView.clAttribution.setOnClickListener {
            openCustomTab(getString(R.string.attributionUrl), requireContext())
        }

        inflatedView.clCryptoCompare.setOnClickListener {
            openCustomTab(getString(R.string.crypto_compare_url), requireContext())
        }

        inflatedView.clCoinGecko.setOnClickListener {
            openCustomTab(getString(R.string.coin_gecko_url), requireContext())
        }

        inflatedView.clCryptoPanic.setOnClickListener {
            openCustomTab(getString(R.string.crypto_panic_url), requireContext())
        }
    }

    private fun openCurrencyPicker() {
        val picker = CurrencyPicker.newInstance(getString(R.string.select_currency)) // dialog title

        picker.setListener { name, code, _, _ ->
            Timber.d("Currency code selected $name,$code")
            PreferenceHelper.setPreference(requireContext(), PreferenceHelper.DEFAULT_CURRENCY, code)

            picker.dismiss() // Show currency that is picked.

            val currency = PreferenceHelper.getPreference(requireContext(), PreferenceHelper.DEFAULT_CURRENCY, defaultCurrency)

            tvCurrencyValue.text = currency

            // get list of all coins
            settingsPresenter.refreshCoinList(currency)
        }

        picker.show(fragmentManager, "CURRENCY_PICKER")
    }

    override fun onCoinListRefreshed() {
        ivCurrencyList.visibility = View.VISIBLE
        pbLoadingCurrencyList.visibility = View.GONE
    }

    override fun onExchangeListRefreshed() {
        ivExchangeList.visibility = View.VISIBLE
        pbLoadingExchangeList.visibility = View.GONE
    }

    override fun onNetworkError(errorMessage: String) {
        Snackbar.make(llSettings, errorMessage, Snackbar.LENGTH_LONG).show()
    }
}