package com.codegarden.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.codegarden.app.data.PlantRepository
import com.codegarden.app.data.local.PhotoStore
import com.codegarden.app.data.model.AppState
import com.codegarden.app.data.model.CareLog
import com.codegarden.app.data.model.CareType
import com.codegarden.app.data.model.MyPlant
import com.codegarden.app.data.model.PlantSpecies
import com.codegarden.app.data.model.Settings
import com.codegarden.app.data.model.TRASH_RETENTION_DAYS
import com.codegarden.app.data.model.TrashedPlant
import com.codegarden.app.data.model.UserProfile
import com.codegarden.app.data.weather.CityHit
import com.codegarden.app.data.weather.WeatherRepository
import com.codegarden.app.data.weather.WeatherUiState
import com.codegarden.app.notify.Reminders
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.UUID

/** 主 ViewModel：单一数据源，所有页面共享。离线单机，无需登录 */
class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = PlantRepository(app)
    private val saved = repo.load()

    /** 内置 1000 种盆栽图鉴 */
    val catalog: List<PlantSpecies> get() = repo.catalog

    private val _state = MutableStateFlow(saved ?: AppState(user = defaultUser()))
    val state: StateFlow<AppState> = _state.asStateFlow()

    init {
        if (saved == null) persist()
        purgeExpiredTrash()
        Reminders.createChannel(app)
        Reminders.scheduleAll(app, _state.value.settings)
    }

    private fun defaultUser() = UserProfile("花友", "sprout")

    private fun persist() = repo.save(_state.value)

    fun speciesById(id: Int?): PlantSpecies? = repo.speciesById(id)

    fun plantById(id: String): MyPlant? = _state.value.plants.firstOrNull { it.id == id }

    // ---------- 本地资料 ----------

    /** 确保有默认资料（离线单机，无需登录） */
    fun ensureUser() {
        if (_state.value.user == null) {
            _state.update { it.copy(user = defaultUser()) }
            persist()
        }
    }

    fun updateProfile(nickname: String, avatar: String) {
        _state.update { s ->
            s.copy(user = (s.user ?: defaultUser()).copy(nickname = nickname.trim(), avatar = avatar))
        }
        persist()
    }

    /** 清除所有数据：重置为初始状态，保留在本页 */
    fun clearAllData() {
        repo.clear()
        PhotoStore.deleteAll(getApplication())
        _state.value = AppState(user = defaultUser())
        persist()
        Reminders.scheduleAll(getApplication(), _state.value.settings)
    }

    // ---------- 我的植物 ----------

    /** 从图鉴添加植物，返回新植物 id */
    fun addFromSpecies(species: PlantSpecies, nickname: String, photoPath: String?, note: String): String {
        val plant = MyPlant(
            id = UUID.randomUUID().toString(),
            speciesId = species.id,
            speciesName = species.name,
            emoji = species.emoji,
            nickname = nickname.trim(),
            photoPath = photoPath,
            addedDate = LocalDate.now(),
            waterDays = species.waterDays,
            fertilizeDays = species.fertilizeDays,
            note = note.trim()
        )
        _state.update { it.copy(plants = it.plants + plant) }
        persist()
        return plant.id
    }

    /** 自定义植物（图鉴里没有的品种），返回新植物 id */
    fun addCustom(
        name: String, emoji: String, nickname: String, photoPath: String?,
        waterDays: Int, fertilizeDays: Int, note: String
    ): String {
        val plant = MyPlant(
            id = UUID.randomUUID().toString(),
            speciesId = null,
            speciesName = name.trim().ifBlank { "我的植物" },
            emoji = emoji.ifBlank { "🪴" },
            nickname = nickname.trim(),
            photoPath = photoPath,
            addedDate = LocalDate.now(),
            waterDays = waterDays.coerceIn(1, 365),
            fertilizeDays = fertilizeDays.coerceIn(1, 365),
            note = note.trim()
        )
        _state.update { it.copy(plants = it.plants + plant) }
        persist()
        return plant.id
    }

    fun updatePlant(plant: MyPlant) {
        _state.update { s -> s.copy(plants = s.plants.map { if (it.id == plant.id) plant else it }) }
        persist()
    }

    /** 删除植物：移入回收站，30 天内可恢复（照片与养护记录一并保留） */
    fun deletePlant(plantId: String) {
        val plant = plantById(plantId) ?: return
        val now = System.currentTimeMillis()
        val item = TrashedPlant(
            plant = plant,
            deletedAt = now,
            expiresAt = now + TRASH_RETENTION_DAYS * 24 * 60 * 60 * 1000L
        )
        _state.update { s ->
            s.copy(
                plants = s.plants.filterNot { it.id == plantId },
                trash = listOf(item) + s.trash.filterNot { it.plant.id == plantId }
            )
        }
        persist()
    }

    /** 从回收站恢复植物（养护记录原样回来） */
    fun restorePlant(plantId: String) {
        val item = _state.value.trash.firstOrNull { it.plant.id == plantId } ?: return
        _state.update { s ->
            s.copy(
                plants = s.plants + item.plant,
                trash = s.trash.filterNot { it.plant.id == plantId }
            )
        }
        persist()
    }

    /** 彻底删除：照片与养护记录一并清除，不可恢复 */
    fun purgePlant(plantId: String) {
        val item = _state.value.trash.firstOrNull { it.plant.id == plantId } ?: return
        deletePhotosOf(item.plant)
        _state.update { s ->
            s.copy(
                trash = s.trash.filterNot { it.plant.id == plantId },
                logs = s.logs.filterNot { it.plantId == plantId }
            )
        }
        persist()
    }

    /** 清理回收站里超过 30 天的植物（启动 / 回到前台时调用） */
    fun purgeExpiredTrash() {
        val now = System.currentTimeMillis()
        val expired = _state.value.trash.filter { it.expiresAt <= now }
        if (expired.isEmpty()) return
        expired.forEach { deletePhotosOf(it.plant) }
        val ids = expired.map { it.plant.id }.toSet()
        _state.update { s ->
            s.copy(
                trash = s.trash.filterNot { it.plant.id in ids },
                logs = s.logs.filterNot { it.plantId in ids }
            )
        }
        persist()
    }

    /** 删除这盆植物名下的所有照片（主图 + 打卡照片） */
    private fun deletePhotosOf(plant: MyPlant) {
        PhotoStore.deletePhoto(plant.photoPath)
        _state.value.logs
            .filter { it.plantId == plant.id && it.photoPath != null }
            .forEach { PhotoStore.deletePhoto(it.photoPath) }
    }

    // ---------- 养护打卡 ----------

    /** 浇水 / 施肥 / 除虫 / 修剪打卡：记一条今天的日志，可带用量、照片与备注 */
    fun care(
        plantId: String,
        type: CareType,
        amount: String = "",
        photoPath: String? = null,
        note: String = ""
    ) = addLog(plantId, type, LocalDate.now(), note, amount, photoPath)

    /** 新增一条养护记录（可指定日期，用于日历补录） */
    fun addLog(
        plantId: String,
        type: CareType,
        date: LocalDate,
        note: String = "",
        amount: String = "",
        photoPath: String? = null
    ) {
        val log = CareLog(
            id = UUID.randomUUID().toString(),
            plantId = plantId,
            type = type,
            date = date,
            epochMillis = epochFor(date),
            note = note.trim(),
            amount = amount.trim(),
            photoPath = photoPath
        )
        _state.update { s ->
            val logs = (listOf(log) + s.logs).take(2000)
            s.copy(plants = recomputeCare(s.plants, logs), logs = logs)
        }
        persist()
    }

    /** 修改一条养护记录 */
    fun updateLog(logId: String, type: CareType, date: LocalDate, note: String) {
        _state.update { s ->
            val logs = s.logs.map {
                if (it.id != logId) it
                else it.copy(type = type, date = date, epochMillis = epochFor(date, timeOf(it.epochMillis)), note = note.trim())
            }
            s.copy(plants = recomputeCare(s.plants, logs), logs = logs)
        }
        persist()
    }

    /** 删除一条养护记录 */
    fun deleteLog(logId: String) {
        _state.update { s ->
            val logs = s.logs.filterNot { it.id == logId }
            s.copy(plants = recomputeCare(s.plants, logs), logs = logs)
        }
        persist()
    }

    /** 从日志推导每盆植物的最近浇水 / 施肥日期，保证日历与详情一致 */
    private fun recomputeCare(plants: List<MyPlant>, logs: List<CareLog>): List<MyPlant> =
        plants.map { p ->
            val mine = logs.filter { it.plantId == p.id }
            p.copy(
                lastWatered = mine.filter { it.type == CareType.WATER }.maxOfOrNull { it.date },
                lastFertilized = mine.filter { it.type == CareType.FERTILIZE }.maxOfOrNull { it.date }
            )
        }

    private fun epochFor(date: LocalDate, time: LocalTime = LocalTime.now()): Long =
        date.atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

    private fun timeOf(epochMillis: Long): LocalTime =
        Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalTime()

    fun logsFor(plantId: String): List<CareLog> =
        _state.value.logs.filter { it.plantId == plantId }

    fun logsOn(date: LocalDate): List<CareLog> {
        val active = _state.value.plants.mapTo(HashSet()) { it.id }
        return _state.value.logs
            .filter { it.date == date && it.plantId in active }
            .sortedByDescending { it.epochMillis }
    }

    // ---------- 设置 ----------

    private fun updateSettings(transform: (Settings) -> Settings) {
        _state.update { it.copy(settings = transform(it.settings)) }
        persist()
        Reminders.scheduleAll(getApplication(), _state.value.settings)
    }

    fun setDailyReminder(enabled: Boolean) = updateSettings { it.copy(dailyReminder = enabled) }
    fun setOverdueReminder(enabled: Boolean) = updateSettings { it.copy(overdueReminder = enabled) }
    fun setDailyReminderTime(time: String) = updateSettings { it.copy(dailyReminderTime = time) }
    fun setOverdueReminderTime(time: String) = updateSettings { it.copy(overdueReminderTime = time) }
    fun setDarkMode(enabled: Boolean) = updateSettings { it.copy(darkMode = enabled) }

    /** App 回到前台：清理过期回收站，确保提醒已调度 */
    fun onAppResume() {
        purgeExpiredTrash()
        Reminders.scheduleAll(getApplication(), _state.value.settings)
    }

    // ---------- 天气 ----------

    private val weatherRepo = WeatherRepository(app)

    private val _weather = MutableStateFlow(WeatherUiState())
    val weather: StateFlow<WeatherUiState> = _weather.asStateFlow()

    /** 是否已授予定位权限 */
    fun hasLocationPermission(): Boolean = weatherRepo.hasLocationPermission()

    /**
     * 刷新天气：自动模式优先用手机定位（失败则回落到上次地区），
     * 手动模式直接按已选地区查询。
     */
    fun refreshWeather() {
        if (_weather.value.loading) return
        viewModelScope.launch {
            _weather.update { it.copy(loading = true, error = null) }
            val s = _state.value.settings
            var lat = s.weatherLat
            var lon = s.weatherLon
            var city = s.weatherCity
            var fromLocation = false

            if (s.weatherAuto) {
                val loc = weatherRepo.currentLocation()
                if (loc != null) {
                    lat = loc.first
                    lon = loc.second
                    fromLocation = true
                    city = weatherRepo.reverseGeocode(lat, lon) ?: city
                }
            }
            if (lat == 0.0 || lon == 0.0) {
                _weather.update { it.copy(loading = false, error = "还没有设置地区，点这里选择") }
                return@launch
            }
            val info = weatherRepo.fetch(lat, lon)
            if (info == null) {
                _weather.update { it.copy(loading = false, error = "天气获取失败，请检查网络") }
                return@launch
            }
            _weather.update {
                it.copy(
                    loading = false,
                    info = info,
                    cityName = city.ifBlank { "当前位置" },
                    fromCurrentLocation = fromLocation,
                    error = null
                )
            }
            if (fromLocation && (city.isNotBlank() || s.weatherCity.isBlank())) {
                updateSettings { it.copy(weatherCity = city, weatherLat = lat, weatherLon = lon) }
            }
        }
    }

    /** 手动选择地区 */
    fun setWeatherRegion(name: String, lat: Double, lon: Double) {
        updateSettings { it.copy(weatherCity = name, weatherLat = lat, weatherLon = lon, weatherAuto = false) }
        refreshWeather()
    }

    /** 切回自动定位 */
    fun useCurrentLocation() {
        updateSettings { it.copy(weatherAuto = true) }
        refreshWeather()
    }

    /** 记录已请求过定位权限，避免每次进入主页都弹窗 */
    fun markWeatherAsked() {
        updateSettings { it.copy(weatherAsked = true) }
    }

    /** 城市检索 */
    fun searchCities(query: String, onResult: (List<CityHit>) -> Unit) {
        viewModelScope.launch { onResult(weatherRepo.searchCity(query)) }
    }
}
