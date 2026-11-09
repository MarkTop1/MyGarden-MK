package com.codegarden.app.data.model

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** 养护动作类型 */
enum class CareType(val label: String, val emoji: String, val verb: String) {
    WATER("浇水", "💧", "浇了水"),
    FERTILIZE("施肥", "🌾", "施了肥"),
    PEST("除虫", "🐛", "除过虫"),
    PRUNE("修剪", "✂️", "修剪了")
}

/** 盆栽类别（与 plants.json 的 category 字段一致） */
enum class PlantCategory(val label: String, val emoji: String) {
    SUCCULENT("多肉植物", "🪴"),
    CACTUS("仙人掌类", "🌵"),
    FOLIAGE("观叶植物", "🌿"),
    FLOWERING("开花植物", "🌸"),
    HERB("香草植物", "🌱"),
    BONSAI("盆景木本", "🌳"),
    BULB("球根花卉", "🌷"),
    AQUATIC("水生植物", "🪷");

    companion object {
        fun fromKey(key: String): PlantCategory =
            entries.firstOrNull { it.name == key } ?: FOLIAGE
    }
}

/** 图鉴条目：内置 1000 种常见盆栽 */
data class PlantSpecies(
    val id: Int,
    val name: String,
    val category: PlantCategory,
    val emoji: String,
    val waterDays: Int,
    val fertilizeDays: Int,
    val light: String,
    val difficulty: String,
    val description: String
)

/** 我养的一盆植物 */
data class MyPlant(
    val id: String,
    val speciesId: Int?,
    val speciesName: String,
    val emoji: String,
    val nickname: String,
    val photoPath: String? = null,
    val addedDate: LocalDate,
    val lastWatered: LocalDate? = null,
    val lastFertilized: LocalDate? = null,
    val waterDays: Int,
    val fertilizeDays: Int,
    val note: String = ""
) {
    val displayName: String get() = nickname.ifBlank { speciesName }
}

/** 一条养护打卡记录 */
data class CareLog(
    val id: String,
    val plantId: String,
    val type: CareType,
    val date: LocalDate,
    val epochMillis: Long,
    val note: String = "",
    /** 浇水量 / 施肥量，自由文本，如「200ml」「1 小勺」 */
    val amount: String = "",
    /** 当日照片存档路径 */
    val photoPath: String? = null
) {
    val timeText: String get() = formatRelativeTime(epochMillis)
}

/** 回收站里的一盆植物：保留植物资料与养护记录，30 天内可以恢复 */
data class TrashedPlant(
    val plant: MyPlant,
    val deletedAt: Long,
    val expiresAt: Long
) {
    /** 距离自动彻底删除还剩多少毫秒（不会为负） */
    fun remainingMillis(now: Long = System.currentTimeMillis()): Long =
        (expiresAt - now).coerceAtLeast(0L)
}

/** 回收站保留天数：超过后自动彻底删除 */
const val TRASH_RETENTION_DAYS = 30L

/** 本地账号 */
data class UserProfile(
    val nickname: String,
    val avatar: String
)

/** 提醒设置 */
data class Settings(
    val dailyReminder: Boolean = true,
    val dailyReminderTime: String = "09:00",
    val overdueReminder: Boolean = true,
    val overdueReminderTime: String = "18:00",
    val darkMode: Boolean = false,
    val weatherCity: String = "",
    val weatherLat: Double = 0.0,
    val weatherLon: Double = 0.0,
    val weatherAuto: Boolean = true,
    val weatherAsked: Boolean = false
)

/** 全局应用状态（全部存本机，离线单机，无需登录） */
data class AppState(
    val user: UserProfile? = null,
    val plants: List<MyPlant> = emptyList(),
    val logs: List<CareLog> = emptyList(),
    val settings: Settings = Settings(),
    /** 已删除但尚未彻底清除的植物（保留 30 天） */
    val trash: List<TrashedPlant> = emptyList()
) {
    val plantCount: Int get() = plants.size
}

/** 把时间戳格式化为「今天 14:22 / 昨天 21:47 / 11-03 10:00」 */
fun formatRelativeTime(epochMillis: Long): String {
    val dt = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault())
    val date = dt.toLocalDate()
    val today = LocalDate.now()
    val hm = dt.format(DateTimeFormatter.ofPattern("HH:mm"))
    return when (date) {
        today -> "今天 $hm"
        today.minusDays(1) -> "昨天 $hm"
        today.minusDays(2) -> "前天 $hm"
        else -> "${date.format(DateTimeFormatter.ofPattern("MM-dd"))} $hm"
    }
}

fun formatDate(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
fun formatShortDate(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("MM-dd"))