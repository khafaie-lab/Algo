package com.example.network

import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Nobitex Repository implementing network resilient queries across official endpoints:
 * 1. Primary: https://apiv2.nobitex.ir/
 * 2. Fallback: https://api.nobitex.ir/
 *
 * Adheres strictly to the user requirement:
 * - If data cannot be fetched, do NOT invent or randomize prices.
 * - Always show the last successfully received price alongside exact date and time.
 */
class NobitexRepository {

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val request = original.newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Linux; Android 14; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .header("Accept", "application/json")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .build()

    private val primaryService: NobitexApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://apiv2.nobitex.ir/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(NobitexApiService::class.java)
    }

    private val fallbackService: NobitexApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.nobitex.ir/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(NobitexApiService::class.java)
    }

    // In-Memory Persistent Cache of Last Legitimate Market Data (Zero Mock)
    private var cachedStats: NobitexStatsResponse? = null
    private var cachedOrderBooks: MutableMap<String, NobitexOrderBookResponse> = mutableMapOf()
    private var cachedCandles: MutableMap<String, NobitexUdfHistoryResponse> = mutableMapOf()

    // Last successful sync record
    private var lastSuccessfulTimestamp: String = ""
    private var lastSuccessfulDate: String = ""

    private fun getCurrentFormattedTime(): String {
        val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }

    private fun getCurrentFormattedDate(): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Fetch Market Statistics (Price, Volume, 24h High/Low, Day Change)
     * e.g. srcCurrency=usdt, dstCurrency=rls
     */
    suspend fun getMarketStats(src: String? = null, dst: String? = null): Pair<NobitexStatsResponse?, NobitexConnectionState> {
        val services = listOf(
            "apiv2.nobitex.ir" to primaryService,
            "api.nobitex.ir" to fallbackService
        )

        for ((endpointName, service) in services) {
            try {
                val response = service.getMarketStats(src, dst)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status == "ok" && !body.stats.isNullOrEmpty()) {
                        cachedStats = body
                        lastSuccessfulTimestamp = getCurrentFormattedTime()
                        lastSuccessfulDate = getCurrentFormattedDate()
                        return Pair(
                            body,
                            NobitexConnectionState(
                                status = NobitexStatusType.LIVE_CONNECTED,
                                lastUpdatedTime = lastSuccessfulTimestamp,
                                lastUpdatedDate = lastSuccessfulDate,
                                isLive = true,
                                endpointUsed = endpointName,
                                statusMessageFa = "🟢 نوبیتکس زنده ($lastSuccessfulTimestamp)"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w("NobitexRepo", "Stats fetch error on $endpointName: ${e.message}")
            }
        }

        // Network connection failed or blocked.
        // Return cached stats with explicit last recorded time (Zero Mock Policy)
        val timeDisplay = if (lastSuccessfulTimestamp.isNotEmpty()) lastSuccessfulTimestamp else "آخرین استعلام معتبر"
        val dateDisplay = if (lastSuccessfulDate.isNotEmpty()) lastSuccessfulDate else "امروز"
        return Pair(
            cachedStats,
            NobitexConnectionState(
                status = if (cachedStats != null) NobitexStatusType.CACHED_OFFLINE else NobitexStatusType.ERROR_CONNECTING,
                lastUpdatedTime = timeDisplay,
                lastUpdatedDate = dateDisplay,
                isLive = false,
                endpointUsed = "کش محلی نوبیتکس",
                statusMessageFa = "🟡 آخرین داده معتبر: $timeDisplay (بدون جعل قیمت)"
            )
        )
    }

    /**
     * Fetch Market Order Book
     * e.g. symbol = "USDTIRT"
     */
    suspend fun getOrderBook(symbol: String): Pair<NobitexOrderBookResponse?, NobitexConnectionState> {
        val sym = symbol.uppercase()
        val services = listOf(
            "apiv2.nobitex.ir" to primaryService,
            "api.nobitex.ir" to fallbackService
        )

        for ((endpointName, service) in services) {
            try {
                val response = service.getOrderBook(sym)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status == "ok") {
                        cachedOrderBooks[sym] = body
                        lastSuccessfulTimestamp = getCurrentFormattedTime()
                        lastSuccessfulDate = getCurrentFormattedDate()
                        return Pair(
                            body,
                            NobitexConnectionState(
                                status = NobitexStatusType.LIVE_CONNECTED,
                                lastUpdatedTime = lastSuccessfulTimestamp,
                                lastUpdatedDate = lastSuccessfulDate,
                                isLive = true,
                                endpointUsed = endpointName,
                                statusMessageFa = "🟢 اردر بوک زنده ($lastSuccessfulTimestamp)"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w("NobitexRepo", "Orderbook fetch error on $endpointName for $sym: ${e.message}")
            }
        }

        val cached = cachedOrderBooks[sym]
        val timeDisplay = if (lastSuccessfulTimestamp.isNotEmpty()) lastSuccessfulTimestamp else "آخرین استعلام"
        return Pair(
            cached,
            NobitexConnectionState(
                status = if (cached != null) NobitexStatusType.CACHED_OFFLINE else NobitexStatusType.ERROR_CONNECTING,
                lastUpdatedTime = timeDisplay,
                lastUpdatedDate = lastSuccessfulDate,
                isLive = false,
                endpointUsed = "کش محلی نوبیتکس",
                statusMessageFa = "🟡 اردر بوک آفلاین: $timeDisplay"
            )
        )
    }

    /**
     * Fetch TradingView UDF Candlestick History
     * e.g. symbol = "USDTIRT", resolution = "15", countback = 160
     */
    suspend fun getUdfHistory(
        symbol: String,
        resolution: String,
        toSec: Long,
        fromSec: Long? = null,
        countback: Int = 160
    ): Pair<NobitexUdfHistoryResponse?, NobitexConnectionState> {
        val sym = symbol.uppercase()
        val key = "$sym-$resolution"
        val services = listOf(
            "apiv2.nobitex.ir" to primaryService,
            "api.nobitex.ir" to fallbackService
        )

        for ((endpointName, service) in services) {
            try {
                val response = service.getUdfHistory(
                    symbol = sym,
                    resolution = resolution,
                    to = toSec,
                    from = fromSec,
                    countback = countback
                )
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.status == "ok" && !body.timestamps.isNullOrEmpty()) {
                        cachedCandles[key] = body
                        lastSuccessfulTimestamp = getCurrentFormattedTime()
                        lastSuccessfulDate = getCurrentFormattedDate()
                        return Pair(
                            body,
                            NobitexConnectionState(
                                status = NobitexStatusType.LIVE_CONNECTED,
                                lastUpdatedTime = lastSuccessfulTimestamp,
                                lastUpdatedDate = lastSuccessfulDate,
                                isLive = true,
                                endpointUsed = endpointName,
                                statusMessageFa = "🟢 کندل‌های زنده نوبیتکس ($lastSuccessfulTimestamp)"
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w("NobitexRepo", "UDF history error on $endpointName for $sym: ${e.message}")
            }
        }

        val cached = cachedCandles[key]
        val timeDisplay = if (lastSuccessfulTimestamp.isNotEmpty()) lastSuccessfulTimestamp else "داده‌های قبلی"
        return Pair(
            cached,
            NobitexConnectionState(
                status = if (cached != null) NobitexStatusType.CACHED_OFFLINE else NobitexStatusType.ERROR_CONNECTING,
                lastUpdatedTime = timeDisplay,
                lastUpdatedDate = lastSuccessfulDate,
                isLive = false,
                endpointUsed = "کش محلی چارت",
                statusMessageFa = "🟡 چارت قبلی نوبیتکس: $timeDisplay"
            )
        )
    }
}
