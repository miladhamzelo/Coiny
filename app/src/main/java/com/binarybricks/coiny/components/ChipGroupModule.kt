package com.binarybricks.coiny.components

import android.content.Context
import android.support.design.chip.Chip
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.binarybricks.coiny.R
import com.binarybricks.coiny.network.models.CoinPair
import com.binarybricks.coiny.stories.coindetails.CoinDetailsActivity
import kotlinx.android.synthetic.main.chip_group_module.view.*
import timber.log.Timber

/**
 * Created by Pranay Airan
 *
 * Simple class wrapping UI for top card
 */
class ChipGroupModule : Module() {

    override fun init(layoutInflater: LayoutInflater, parent: ViewGroup?): View {
        return layoutInflater.inflate(R.layout.chip_group_module, parent, false)
    }

    fun showAllChips(inflatedView: View, chipGroupModuleData: ChipGroupModuleData) {

        val chunkedList = chipGroupModuleData.data.chunked(15)

        var i = 0
        chunkedList.forEach {
            val chipGroup = when (i) {
                1 -> inflatedView.chipGroupFirst
                2 -> inflatedView.chipGroupSecond
                else -> inflatedView.chipGroupThird
            }
            it.forEach { coinPair ->
                chipGroup.addView(getChip(chipGroup.context, coinPair))
            }
            i++
        }
    }

    private fun getChip(context: Context, coinPair: CoinPair): Chip {
        val chip = Chip(context)
        chip.text = coinPair.fullName
        chip.setTextColor(ContextCompat.getColor(context, R.color.primaryTextColor))
        chip.isClickable = true
        chip.isCheckable = false
        chip.chipBackgroundColor = ContextCompat.getColorStateList(context, R.color.chip_color)

        chip.setOnClickListener {
            if (coinPair.symbol != null) {
                context.startActivity(CoinDetailsActivity.buildLaunchIntent(context,
                        coinPair.symbol))
            }
        }
        return chip
    }

    override fun cleanUp() {
        Timber.d("Clean up empty TopCardModule module")
    }

    data class ChipGroupModuleData(val data: List<CoinPair>) : ModuleItem
}