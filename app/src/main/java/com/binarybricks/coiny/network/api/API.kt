package com.binarybricks.coiny.network.api

import com.binarybricks.coiny.network.models.CryptoCompareHistoricalResponse
import com.binarybricks.coiny.network.models.CryptoPanicNews
import com.google.gson.JsonObject
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface API {

    @GET("all/coinlist")
    fun getCoinList(): Single<JsonObject>

    @GET("pricemultifull")
    fun getPricesFull(@Query("fsyms") fromSymbol: String, @Query("tsyms") toSymbol: String): Single<JsonObject>

    @GET("{period}")
    fun getCryptoHistoricalData(@Path("period") period: String,
                                @Query("fsym") fromCurrencySymbol: String?,
                                @Query("tsym") toCurrencySymbol: String?,
                                @Query("limit") limit: Int,
                                @Query("aggregate") aggregate: Int): Single<CryptoCompareHistoricalResponse>

//    @GET("top/pairs")
//    fun getPairs(@Query("fsym") from: String): Single<JsonObject>

    @GET("all/exchanges")
    fun getExchangeList(): Single<JsonObject>


    @GET("https://cryptopanic.com/api/posts/?auth_token=cd529bae09d5c505248fe05618da96ffb35ecffc")
    fun getCryptoNewsForCurrency(@Query("currencies") coinSymbol: String,
                                 @Query("filter") filter: String,
                                 @Query("public") public: Boolean): Single<CryptoPanicNews>

}