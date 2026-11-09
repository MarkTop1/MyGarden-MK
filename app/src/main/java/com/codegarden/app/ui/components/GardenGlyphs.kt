package com.codegarden.app.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.codegarden.app.data.model.CareType
import com.codegarden.app.data.model.PlantCategory
import com.codegarden.app.ui.theme.FertilizeGreen
import com.codegarden.app.ui.theme.MintDeep
import com.codegarden.app.ui.theme.WaterBlue

/** 统一的植物系线描头像 / 图标键位，替代系统 emoji */
object GardenGlyphs {
    val avatarKeys = listOf(
        "sprout", "leaf", "flower", "cactus", "succulent",
        "tree", "tulip", "lotus", "clover", "drop"
    )

    /** 旧版 emoji 头像 → 新版线描键位映射，保证老数据也能正常显示 */
    private val emojiToKey = mapOf(
        "🌿" to "sprout", "🌱" to "sprout", "🍀" to "clover", "🌾" to "sprout",
        "🌷" to "tulip", "🌸" to "flower", "🌻" to "flower", "🌼" to "flower",
        "🌵" to "cactus", "🪴" to "succulent", "🌳" to "tree", "🌲" to "tree",
        "🪻" to "lotus", "💧" to "drop", "🍃" to "leaf"
    )

    /** 把任意存档字符串（emoji 或键位）归一化为合法键位 */
    fun avatarKey(raw: String?): String {
        if (raw.isNullOrBlank()) return "sprout"
        if (raw in avatarKeys) return raw
        return emojiToKey[raw] ?: "sprout"
    }

    fun icon(key: String?): ImageVector = when (key) {
        "leaf" -> GardenIcons.Leaf
        "flower" -> GardenIcons.Flower
        "cactus" -> GardenIcons.Cactus
        "succulent" -> GardenIcons.Succulent
        "tree" -> GardenIcons.Tree
        "tulip" -> GardenIcons.Tulip
        "lotus" -> GardenIcons.Lotus
        "clover" -> GardenIcons.Clover
        "drop" -> GardenIcons.Drop
        else -> GardenIcons.Sprout
    }

    fun careIcon(type: CareType): ImageVector =
        if (type == CareType.WATER) GardenIcons.Drop else GardenIcons.Sprout

    fun categoryIcon(category: PlantCategory): ImageVector = when (category) {
        PlantCategory.SUCCULENT -> GardenIcons.Succulent
        PlantCategory.CACTUS -> GardenIcons.Cactus
        PlantCategory.FOLIAGE -> GardenIcons.Leaf
        PlantCategory.FLOWERING -> GardenIcons.Flower
        PlantCategory.HERB -> GardenIcons.Sprout
        PlantCategory.BONSAI -> GardenIcons.Tree
        PlantCategory.BULB -> GardenIcons.Tulip
        PlantCategory.AQUATIC -> GardenIcons.Lotus
    }
}

@Composable
fun CareIcon(type: CareType, size: Dp = 16.dp, tint: Color? = null) {
    Icon(
        GardenGlyphs.careIcon(type),
        contentDescription = type.label,
        tint = tint ?: if (type == CareType.WATER) WaterBlue else FertilizeGreen,
        modifier = Modifier.size(size)
    )
}

@Composable
fun CategoryIcon(category: PlantCategory, size: Dp = 14.dp, tint: Color? = null) {
    Icon(
        GardenGlyphs.categoryIcon(category),
        contentDescription = category.label,
        tint = tint ?: MintDeep,
        modifier = Modifier.size(size)
    )
}

@Composable
fun GlyphIcon(key: String?, size: Dp = 20.dp, tint: Color? = null) {
    Icon(
        GardenGlyphs.icon(key),
        contentDescription = null,
        tint = tint ?: MintDeep,
        modifier = Modifier.size(size)
    )
}