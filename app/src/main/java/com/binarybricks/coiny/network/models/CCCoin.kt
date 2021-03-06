package com.binarybricks.coiny.network.models

import com.binarybricks.coiny.data.database.entities.Coin
import com.google.gson.annotations.SerializedName

/**
 Created by Pranay Airan 1/15/18.
 *
 * Network object representing Coin from crypto compare
 */
data class CCCoin(

    @field:SerializedName("Id") val id: String = "",

    @field:SerializedName("Url") val url: String = "",

    @field:SerializedName("ImageUrl") val imageUrl: String = "",

    @field:SerializedName("Name") val name: String = "",

    @field:SerializedName("Symbol") val symbol: String = "",

    @field:SerializedName("CoinName") val coinName: String = "",

    @field:SerializedName("FullName") val fullName: String = "",

    @field:SerializedName("Algorithm") val algorithm: String = "",

    @field:SerializedName("ProofType") val proofType: String = "",

    @field:SerializedName("FullyPremined") val fullyPremined: String = "",

    @field:SerializedName("TotalCoinSupply") val totalCoinSupply: String = "",

    @field:SerializedName("PreMinedValue") val preMinedValue: String = "",

    @field:SerializedName("TotalCoinsFreeFloat") val totalCoinsFreeFloat: String = "",

    @field:SerializedName("SortOrder") val sortOrder: String = "",

    @field:SerializedName("Sponsored") val sponsored: Boolean = false)

fun getCoinFromCCCoin(ccCoin: CCCoin): Coin {

    return Coin(ccCoin.id, ccCoin.url, ccCoin.imageUrl, ccCoin.name, ccCoin.symbol, ccCoin.coinName,
        ccCoin.fullName, ccCoin.algorithm, ccCoin.proofType, ccCoin.fullyPremined,
        ccCoin.totalCoinSupply, ccCoin.preMinedValue, ccCoin.totalCoinsFreeFloat, ccCoin.sortOrder,
        ccCoin.sponsored, false)
}