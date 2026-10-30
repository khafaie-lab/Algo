package com.example.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit Service Interface for Nobitex Public REST APIs.
 * Documented at https://apidocs.nobitex.ir/
 */
interface NobitexApiService {

    /**
     * Retrieves 24h market statistics and real-time spot rates.
     * Example: /market/stats?srcCurrency=usdt&dstCurrency=rls
     */
    @GET("market/stats")
    suspend fun getMarketStats(
        @Query("srcCurrency") srcCurrency: String? = null,
        @Query("dstCurrency") dstCurrency: String? = null
    ): Response<NobitexStatsResponse>

    /**
     * Retrieves market order book depth.
     * Example: /v2/orderbook/USDTIRT or /v2/orderbook/BTCIRT
     */
    @GET("v2/orderbook/{symbol}")
    suspend fun getOrderBook(
        @Path("symbol") symbol: String
    ): Response<NobitexOrderBookResponse>

    /**
     * Retrieves TradingView UDF Candlestick History.
     * Example: /market/udf/history?symbol=USDTIRT&resolution=15&to=1730000000&countback=160
     */
    @GET("market/udf/history")
    suspend fun getUdfHistory(
        @Query("symbol") symbol: String,
        @Query("resolution") resolution: String,
        @Query("to") to: Long,
        @Query("from") from: Long? = null,
        @Query("countback") countback: Int? = null
    ): Response<NobitexUdfHistoryResponse>
}
