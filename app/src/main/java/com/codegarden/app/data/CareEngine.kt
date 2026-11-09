package com.codegarden.app.data

import com.codegarden.app.data.model.MyPlant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** 今日养护汇总 */
data class DueSummary(
    val needWater: Int,
    val needFertilize: Int,
    val overdue: Int
) {
    val total: Int get() = needWater + needFertilize
}

/** 植物健康状态：按最严重的逾期程度判定 */
enum class PlantHealthState(
    val label: String,
    val emoji: String
) {
    THRIVING("生机盎然", "✨"),
    OK("状态良好", "🌱"),
    NEEDS_ATTENTION("需要照料", "💧"),
    WILTING("叶子微垂", "🌿"),
    YELLOWING("叶尖泛黄", "🍂"),
    WILTED("枯萎了", "🥀")
}

/** 养护引擎：纯函数，按「上次打卡日期 + 间隔天数」推算下次浇水/施肥时间 + 健康状态 */
object CareEngine {

    fun nextWaterDate(plant: MyPlant): LocalDate =
        plant.lastWatered?.plusDays(plant.waterDays.toLong()) ?: plant.addedDate

    fun nextFertilizeDate(plant: MyPlant): LocalDate =
        plant.lastFertilized?.plusDays(plant.fertilizeDays.toLong()) ?: plant.addedDate

    fun daysUntilWater(plant: MyPlant, today: LocalDate): Int =
        ChronoUnit.DAYS.between(today, nextWaterDate(plant)).toInt()

    fun daysUntilFertilize(plant: MyPlant, today: LocalDate): Int =
        ChronoUnit.DAYS.between(today, nextFertilizeDate(plant)).toInt()

    fun isWaterDue(plant: MyPlant, today: LocalDate): Boolean = daysUntilWater(plant, today) <= 0

    fun isFertilizeDue(plant: MyPlant, today: LocalDate): Boolean = daysUntilFertilize(plant, today) <= 0

    fun dueSummary(plants: List<MyPlant>, today: LocalDate): DueSummary = DueSummary(
        needWater = plants.count { isWaterDue(it, today) },
        needFertilize = plants.count { isFertilizeDue(it, today) },
        overdue = plants.count { daysUntilWater(it, today) < 0 || daysUntilFertilize(it, today) < 0 }
    )

    /** 植物整体健康状态（按最严重的一项逾期） */
    fun healthState(plant: MyPlant, today: LocalDate): PlantHealthState {
        val waterOverdue = -daysUntilWater(plant, today)  // 逾期天数（负数=未逾期，正数=逾期）
        val fertOverdue = -daysUntilFertilize(plant, today)
        val worst = maxOf(waterOverdue, fertOverdue)
        return when {
            worst >= 7 -> PlantHealthState.WILTED          // 逾期 7 天以上
            worst >= 4 -> PlantHealthState.YELLOWING      // 逾期 4~6 天
            worst >= 2 -> PlantHealthState.WILTING        // 逾期 2~3 天
            worst >= 1 -> PlantHealthState.NEEDS_ATTENTION // 逾期 1 天
            worst == 0 -> PlantHealthState.OK              // 今天到期
            worst >= -1 -> PlantHealthState.OK             // 1 天后到期
            worst >= -3 -> PlantHealthState.THRIVING       // 3 天以上富余
            else -> PlantHealthState.THRIVING
        }
    }

    /** 到期文案：已逾期 N 天 / 今天 / N 天后 */
    fun dueText(days: Int): String = when {
        days < 0 -> "已逾期 ${-days} 天"
        days == 0 -> "今天"
        else -> "$days 天后"
    }
}
