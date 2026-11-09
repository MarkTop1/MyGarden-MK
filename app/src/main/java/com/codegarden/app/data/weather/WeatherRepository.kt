package com.codegarden.app.data.weather

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume

/** 天气状况（WMO weather code 归类） */
enum class WeatherCondition(val label: String) {
    CLEAR("晴"),
    PARTLY_CLOUDY("晴间多云"),
    CLOUDY("阴"),
    FOG("有雾"),
    DRIZZLE("毛毛雨"),
    RAIN("有雨"),
    SNOW("有雪"),
    THUNDER("雷阵雨"),
    UNKNOWN("天气未知")
}

/** 一次天气读取结果 */
data class WeatherInfo(
    val tempC: Double,
    val feelsLikeC: Double,
    val condition: WeatherCondition,
    val code: Int,
    val isDay: Boolean,
    val updatedAt: Long = System.currentTimeMillis()
)

/** 城市搜索命中项 */
data class CityHit(
    val name: String,
    val lat: Double,
    val lon: Double,
    val region: String = ""
)

/** 天气 UI 状态 */
data class WeatherUiState(
    val loading: Boolean = false,
    val info: WeatherInfo? = null,
    val cityName: String = "",
    val fromCurrentLocation: Boolean = false,
    val error: String? = null
)

/**
 * 天气数据源：Open-Meteo 公共接口（无需 Key）+ 系统定位 + 城市检索。
 * 网络失败一律返回 null，由上层降级到手动选择地区。
 */
class WeatherRepository(private val context: Context) {

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /** 查询当前天气 */
    suspend fun fetch(lat: Double, lon: Double): WeatherInfo? = withContext(Dispatchers.IO) {
        runCatching {
            val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
                "&current=temperature_2m,apparent_temperature,weather_code,is_day&timezone=auto"
            val cur = JSONObject(get(url)).getJSONObject("current")
            val temp = cur.getDouble("temperature_2m")
            val code = cur.getInt("weather_code")
            WeatherInfo(
                tempC = temp,
                feelsLikeC = cur.optDouble("apparent_temperature", temp),
                condition = conditionOf(code),
                code = code,
                isDay = cur.optInt("is_day", 1) == 1
            )
        }.onFailure { Log.w("WeatherRepo", "fetch failed: ${it.message}") }.getOrNull()
    }

    /** 城市名检索（Open-Meteo Geocoding） */
    suspend fun searchCity(query: String): List<CityHit> = withContext(Dispatchers.IO) {
        runCatching {
            val url = "https://geocoding-api.open-meteo.com/v1/search?name=$query&count=10&language=zh&format=json"
            val arr = JSONObject(get(url)).optJSONArray("results") ?: return@runCatching emptyList()
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                CityHit(
                    name = o.optString("name"),
                    lat = o.optDouble("latitude", 0.0),
                    lon = o.optDouble("longitude", 0.0),
                    region = listOf(
                        o.optString("admin1"),
                        o.optString("country")
                    ).filter { it.isNotBlank() }.joinToString(" ")
                )
            }.filter { it.name.isNotBlank() && it.lat != 0.0 }
        }.getOrDefault(emptyList())
    }

    /** 反查坐标所在城市名 */
    suspend fun reverseGeocode(lat: Double, lon: Double): String? = withContext(Dispatchers.IO) {
        runCatching {
            val url = "https://api.bigdatacloud.net/data/reverse-geocode-client" +
                "?latitude=$lat&longitude=$lon&localityLanguage=zh"
            val o = JSONObject(get(url))
            listOf("city", "locality", "principalSubdivision", "countryName")
                .map { o.optString(it) }
                .firstOrNull { it.isNotBlank() && it != "null" }
        }.getOrNull()
    }

    /** 读取系统定位（粗略即可，只用来查天气），整体 10 秒超时，避免无限等待 */
    suspend fun currentLocation(): Pair<Double, Double>? = withTimeoutOrNull(10_000) {
        if (!hasLocationPermission()) return@withTimeoutOrNull null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return@withTimeoutOrNull null
        val providers = listOf(
            LocationManager.NETWORK_PROVIDER,
            LocationManager.GPS_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )
        val last: Location? = providers
            .mapNotNull { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
        if (last != null) return@withTimeoutOrNull last.latitude to last.longitude

        val enabled = providers.filter { p ->
            p != LocationManager.PASSIVE_PROVIDER &&
                runCatching { lm.isProviderEnabled(p) }.getOrDefault(false)
        }
        for (provider in enabled) {
            val fix = requestFix(lm, provider)
            if (fix != null) return@withTimeoutOrNull fix
        }
        null
    }

    private suspend fun requestFix(lm: LocationManager, provider: String): Pair<Double, Double>? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            suspendCancellableCoroutine { cont ->
                val signal = CancellationSignal()
                try {
                    lm.getCurrentLocation(provider, signal, context.mainExecutor) { loc ->
                        if (cont.isActive) cont.resume(loc?.let { it.latitude to it.longitude })
                    }
                } catch (e: Throwable) {
                    if (cont.isActive) cont.resume(null)
                }
                cont.invokeOnCancellation { signal.cancel() }
            }
        } else {
            suspendCancellableCoroutine { cont ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(l: Location) {
                        if (cont.isActive) {
                            cont.resume(l.latitude to l.longitude)
                            lm.removeUpdates(this)
                        }
                    }

                    override fun onProviderEnabled(provider: String) = Unit
                    override fun onProviderDisabled(provider: String) = Unit

                    @Deprecated("Deprecated in Java")
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
                }
                try {
                    lm.requestLocationUpdates(provider, 0L, 0f, listener, Looper.getMainLooper())
                } catch (e: Throwable) {
                    if (cont.isActive) cont.resume(null)
                }
                cont.invokeOnCancellation { lm.removeUpdates(listener) }
            }
        }

    private fun get(url: String): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("Accept", "application/json")
        }
        return try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
            if (code !in 200..299) throw IllegalStateException("HTTP $code")
            text
        } finally {
            conn.disconnect()
        }
    }

    companion object {
        fun conditionOf(code: Int): WeatherCondition = when (code) {
            0 -> WeatherCondition.CLEAR
            1, 2 -> WeatherCondition.PARTLY_CLOUDY
            3 -> WeatherCondition.CLOUDY
            45, 48 -> WeatherCondition.FOG
            51, 53, 55, 56, 57 -> WeatherCondition.DRIZZLE
            in 61..67, in 80..82 -> WeatherCondition.RAIN
            in 71..77, in 85..86 -> WeatherCondition.SNOW
            in 95..99 -> WeatherCondition.THUNDER
            else -> WeatherCondition.UNKNOWN
        }
    }
}
