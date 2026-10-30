package com.example.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Data models for Nobitex official public APIs (apidocs.nobitex.ir)
 */

@JsonClass(generateAdapter = true)
data class NobitexStatsResponse(
    @field:Json(name = "status") val status: String? = null,
    @field:Json(name = "stats") val stats: Map<String, NobitexPairStat>? = null,
    @field:Json(name = "global") val global: Map<String, Any>? = null
)

@JsonClass(generateAdapter = true)
data class NobitexPairStat(
    @field:Json(name = "latest") val latest: String? = null,
    @field:Json(name = "bestBuy") val bestBuy: String? = null,
    @field:Json(name = "bestSell") val bestSell: String? = null,
    @field:Json(name = "dayChange") val dayChange: String? = null,
    @field:Json(name = "dayHigh") val dayHigh: String? = null,
    @field:Json(name = "dayLow") val dayLow: String? = null,
    @field:Json(name = "dayOpen") val dayOpen: String? = null,
    @field:Json(name = "dayClose") val dayClose: String? = null,
    @field:Json(name = "volumeSrc") val volumeSrc: String? = null,
    @field:Json(name = "volumeDst") val volumeDst: String? = null,
    @field:Json(name = "isClosed") val isClosed: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class NobitexOrderBookResponse(
    @field:Json(name = "status") val status: String? = null,
    @field:Json(name = "bids") val bids: List<List<String>>? = null,
    @field:Json(name = "asks") val asks: List<List<String>>? = null,
    @field:Json(name = "lastUpdate") val lastUpdate: String? = null
)

@JsonClass(generateAdapter = true)
data class NobitexUdfHistoryResponse(
    @field:Json(name = "s") val status: String? = null,
    @field:Json(name = "t") val timestamps: List<Long>? = null,
    @field:Json(name = "o") val opens: List<Double>? = null,
    @field:Json(name = "h") val highs: List<Double>? = null,
    @field:Json(name = "l") val lows: List<Double>? = null,
    @field:Json(name = "c") val closes: List<Double>? = null,
    @field:Json(name = "v") val volumes: List<Double>? = null
)

enum class NobitexStatusType {
    LIVE_CONNECTED,
    CACHED_OFFLINE,
    ERROR_CONNECTING
}

data class NobitexConnectionState(
    val status: NobitexStatusType = NobitexStatusType.LIVE_CONNECTED,
    val lastUpdatedTime: String = "",
    val lastUpdatedDate: String = "",
    val isLive: Boolean = true,
    val endpointUsed: String = "apiv2.nobitex.ir",
    val statusMessageFa: String = "متصل به نوبیتکس"
)
