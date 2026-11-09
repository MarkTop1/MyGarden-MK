package com.codegarden.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codegarden.app.data.model.MyPlant
import com.codegarden.app.data.weather.WeatherCondition
import com.codegarden.app.ui.theme.Amber
import com.codegarden.app.ui.theme.CardWarm
import com.codegarden.app.ui.theme.CloudBody
import com.codegarden.app.ui.theme.DropBlue
import com.codegarden.app.ui.theme.ErrorRed
import com.codegarden.app.ui.theme.FertilizeGreen
import com.codegarden.app.ui.theme.GrassBlade
import com.codegarden.app.ui.theme.GrassBottom
import com.codegarden.app.ui.theme.GrassTop
import com.codegarden.app.ui.theme.InkDark
import com.codegarden.app.ui.theme.InkSecondary
import com.codegarden.app.ui.theme.LocalGardenPalette
import com.codegarden.app.ui.theme.MintDeep
import com.codegarden.app.ui.theme.MoonCore
import com.codegarden.app.ui.theme.MoonGlow
import com.codegarden.app.ui.theme.PinkAccent
import com.codegarden.app.ui.theme.PlateSurface
import com.codegarden.app.ui.theme.SceneShadow
import com.codegarden.app.ui.theme.SnowBody
import com.codegarden.app.ui.theme.StarTint
import com.codegarden.app.ui.theme.SunCore
import com.codegarden.app.ui.theme.SunGlow
import com.codegarden.app.ui.theme.SunHalo
import com.codegarden.app.ui.theme.SunRay
import com.codegarden.app.ui.theme.WaterBlue
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * 「我的花园」橱窗：QQ 农场风格的展示框。
 * 上半部是天空（随天气变化），下半部是草地与土壤，
 * 每盆植物以默认品种插画站在草地上，随风轻摆；
 * 天气决定天空配色与特效：晴天有太阳、阴天有云、下雨有雨滴，
 * 白天的好天气里还有小鸟掠过天空、蝴蝶与蜜蜂在花间飞舞。
 */
@Composable
fun GardenShowcase(
    plants: List<MyPlant>,
    condition: WeatherCondition?,
    isDay: Boolean = true,
    needWater: Int = 0,
    needFertilize: Int = 0,
    overdue: Int = 0,
    modifier: Modifier = Modifier,
    onOpenPlant: (String) -> Unit
) {
    val isDark = LocalGardenPalette.current.isDark
    val palette = remember(condition, isDark) { skyPalette(condition, isDark) }
    val cardWarm = CardWarm
    val grassTop = GrassTop
    val grassBottom = GrassBottom
    val grassBlade = GrassBlade
    val flowerTint = PinkAccent
    val plateSurface = PlateSurface

    // 风的相位用 State 形式传给绘制阶段读取，避免每帧触发重组
    val wind = rememberInfiniteTransition(label = "wind")
    val windState = wind.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3800, easing = LinearEasing)),
        label = "windPhase"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(cardWarm)
    ) {
        // 场景：天空 / 远山 / 草地 与卡片底色无缝过渡，标题浮在画面上
        Box(modifier = Modifier.fillMaxWidth().height(196.dp)) {
            Spacer(
                Modifier
                    .fillMaxSize()
                    .drawWithCache {
                        val w = size.width
                        val h = size.height
                        val horizon = h * 0.72f

                        // 天空：顶部由卡片底色渐入，不留硬边
                        val skyBrush = Brush.verticalGradient(
                            0f to cardWarm,
                            0.11f to palette.skyTop,
                            0.64f to palette.skyBottom,
                            1f to palette.skyBottom,
                            startY = 0f,
                            endY = horizon
                        )
                        // 远山：极淡剪影，增加纵深
                        val hillPath = Path().apply {
                            moveTo(0f, horizon)
                            cubicTo(
                                w * 0.18f, horizon - h * 0.13f,
                                w * 0.35f, horizon - h * 0.03f,
                                w * 0.55f, horizon - h * 0.08f
                            )
                            cubicTo(
                                w * 0.72f, horizon - h * 0.12f,
                                w * 0.86f, horizon - h * 0.02f,
                                w, horizon - h * 0.07f
                            )
                            lineTo(w, horizon)
                            close()
                        }
                        // 草地：起伏地平线 + 底部渐入卡片底色
                        val grassPath = Path().apply {
                            moveTo(0f, horizon + h * 0.008f)
                            cubicTo(
                                w * 0.22f, horizon - h * 0.018f,
                                w * 0.45f, horizon + h * 0.024f,
                                w * 0.68f, horizon + h * 0.002f
                            )
                            cubicTo(
                                w * 0.82f, horizon - h * 0.010f,
                                w * 0.92f, horizon + h * 0.012f,
                                w, horizon + h * 0.006f
                            )
                            lineTo(w, h)
                            lineTo(0f, h)
                            close()
                        }
                        val grassBrush = Brush.verticalGradient(
                            0f to grassTop,
                            0.50f to grassBottom,
                            1f to grassBottom.copy(alpha = 0f),
                            startY = horizon,
                            endY = h
                        )
                        // 左右边缘溶入卡片，消除生硬边界
                        val edgeBrush = Brush.horizontalGradient(
                            0f to cardWarm,
                            0.05f to Color.Transparent,
                            0.95f to Color.Transparent,
                            1f to cardWarm
                        )

                        onDrawBehind {
                            drawRect(skyBrush, size = Size(w, horizon))
                            drawPath(hillPath, color = grassBottom.copy(alpha = 0.14f))
                            drawPath(grassPath, brush = grassBrush)

                            val phase = windState.value
                            val twoPi = (Math.PI * 2).toFloat()

                            // 草叶：随风轻摆
                            for (i in 0 until 28) {
                                val fx = ((i * 53) % 100) / 100f
                                val bladeH = 6f + (i % 4) * 2.6f
                                val x = w * fx
                                val y = horizon + 2f + (i % 3) * 2f
                                val sway = sin(phase * twoPi + i * 0.55f) * 3.2f
                                drawLine(
                                    color = grassBlade.copy(alpha = 0.7f),
                                    start = Offset(x, y),
                                    end = Offset(x + (if (i % 2 == 0) 2.2f else -2.2f) + sway, y - bladeH),
                                    strokeWidth = 2f,
                                    cap = StrokeCap.Round
                                )
                            }

                            // 星星点点的小花
                            val swayGlobal = sin(phase * twoPi) * 1.6f
                            for (i in 0 until 5) {
                                val fx = ((i * 37 + 11) % 96) / 100f + 0.02f
                                val fy = horizon + h * (0.05f + (i % 3) * 0.05f)
                                drawLine(
                                    color = grassBlade.copy(alpha = 0.8f),
                                    start = Offset(w * fx, fy + 5f),
                                    end = Offset(w * fx + swayGlobal, fy - 2f),
                                    strokeWidth = 1.6f,
                                    cap = StrokeCap.Round
                                )
                                val cx = w * fx + swayGlobal
                                val cy = fy - 3.5f
                                val petal = 2.1f
                                val petalColor = flowerTint.copy(alpha = 0.85f)
                                drawCircle(petalColor, petal, Offset(cx - petal, cy))
                                drawCircle(petalColor, petal, Offset(cx + petal, cy))
                                drawCircle(petalColor, petal, Offset(cx, cy - petal))
                                drawCircle(petalColor, petal, Offset(cx, cy + petal))
                                drawCircle(Color(0xFFFFF3D6), 1.4f, Offset(cx, cy))
                            }

                            drawRect(edgeBrush)
                        }
                    }
            )

            // 天气特效
            WeatherEffects(condition, isDark, isDay)

            // 标题浮于画面左上
            Text(
                "🌿 我的花园",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = InkDark,
                modifier = Modifier.align(Alignment.TopStart).padding(start = 18.dp, top = 14.dp)
            )

            // 植物
            if (plants.isEmpty()) {
                Text(
                    "橱窗还空着\n添加一盆植物，它就会摆在这里",
                    fontSize = 12.sp,
                    color = InkSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp,
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = 32.dp)
                )
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(start = 12.dp, end = 12.dp, bottom = 16.dp)
                ) {
                    plants.forEachIndexed { index, plant ->
                        ShowcasePlant(
                            plant = plant,
                            index = index,
                            plate = plateSurface,
                            modifier = Modifier.clickable { onOpenPlant(plant.id) }
                        )
                    }
                }
            }
        }

        // 养护状态条
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 4.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShowcaseStat(StatGlyph.Drop, DropBlue, "$needWater", "待浇水", Modifier.weight(1f))
            ShowcaseStat(StatGlyph.Sprout, FertilizeGreen, "$needFertilize", "待施肥", Modifier.weight(1f))
            ShowcaseStat(StatGlyph.Alert, Amber, "$overdue", "已逾期", Modifier.weight(1f), warn = overdue > 0)
        }
    }
}

/** 统计格徽标的图形种类 */
private enum class StatGlyph { Drop, Sprout, Alert }

/** 橱窗底部的统计格：左侧自绘图形（无气泡外圈，自然融入背景），右侧数值与名称同列 */
@Composable
private fun ShowcaseStat(
    glyph: StatGlyph,
    accent: Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    warn: Boolean = false
) {
    val tint = if (warn) ErrorRed else accent
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Canvas(Modifier.size(30.dp)) {
            val s = size.minDimension
            val cx = s / 2f
            val glyphBrush = Brush.verticalGradient(
                listOf(tint, tint.copy(alpha = 0.75f)),
                startY = 0f,
                endY = s
            )
            when (glyph) {
                // 水滴 + 高光点
                StatGlyph.Drop -> {
                    val r = s * 0.24f
                    val top = s * 0.08f
                    val cyc = s * 0.62f
                    val drop = Path().apply {
                        moveTo(cx, top)
                        cubicTo(cx + r * 1.05f, top + s * 0.30f, cx + r, cyc - r * 0.35f, cx + r * 0.88f, cyc + r * 0.30f)
                        cubicTo(cx + r * 0.55f, cyc + r * 1.05f, cx - r * 0.55f, cyc + r * 1.05f, cx - r * 0.88f, cyc + r * 0.30f)
                        cubicTo(cx - r, cyc - r * 0.35f, cx - r * 1.05f, top + s * 0.30f, cx, top)
                        close()
                    }
                    drawPath(drop, glyphBrush)
                    drawCircle(
                        Color.White.copy(alpha = 0.60f),
                        radius = r * 0.26f,
                        center = Offset(cx - r * 0.34f, cyc - r * 0.05f)
                    )
                }
                // 嫩芽：茎 + 两片叶子
                StatGlyph.Sprout -> {
                    drawLine(
                        tint,
                        Offset(cx, s * 0.90f),
                        Offset(cx, s * 0.40f),
                        strokeWidth = s * 0.07f,
                        cap = StrokeCap.Round
                    )
                    rotate(degrees = -34f, pivot = Offset(cx, s * 0.55f)) {
                        drawOval(
                            glyphBrush,
                            topLeft = Offset(cx - s * 0.40f, s * 0.34f),
                            size = Size(s * 0.40f, s * 0.24f)
                        )
                    }
                    rotate(degrees = 34f, pivot = Offset(cx, s * 0.48f)) {
                        drawOval(
                            glyphBrush,
                            topLeft = Offset(cx, s * 0.26f),
                            size = Size(s * 0.40f, s * 0.24f)
                        )
                    }
                }
                // 警示三角 + 感叹号
                StatGlyph.Alert -> {
                    val tri = Path().apply {
                        moveTo(cx, s * 0.10f)
                        lineTo(s * 0.90f, s * 0.86f)
                        lineTo(s * 0.10f, s * 0.86f)
                        close()
                    }
                    drawPath(tri, glyphBrush)
                    drawLine(
                        Color.White,
                        Offset(cx, s * 0.34f),
                        Offset(cx, s * 0.60f),
                        strokeWidth = s * 0.07f,
                        cap = StrokeCap.Round
                    )
                    drawCircle(Color.White, radius = s * 0.045f, center = Offset(cx, s * 0.74f))
                }
            }
        }
        Spacer(Modifier.width(9.dp))
        Column {
            Text(
                value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (warn) ErrorRed else InkDark
            )
            Text(label, fontSize = 10.sp, color = InkSecondary)
        }
    }
}

/** 橱窗里的一盆植物：默认品种插画 + 风吹轻摆 + 脚下柔和投影 */
@Composable
private fun ShowcasePlant(
    plant: MyPlant,
    index: Int,
    plate: Color,
    modifier: Modifier = Modifier
) {
    // 相位以 State 形式持有，在 graphicsLayer 内读取，只重绘不重组
    val transition = rememberInfiniteTransition(label = "sway$index")
    val swayState = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800 + index * 180, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "swayValue"
    )
    val shadowColor = SceneShadow

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.width(86.dp)
    ) {
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.fillMaxWidth().height(100.dp)
        ) {
            // 脚下投影：柔和径向渐变
            Spacer(
                Modifier
                    .align(Alignment.BottomCenter)
                    .size(width = 44.dp, height = 10.dp)
                    .drawWithCache {
                        val brush = Brush.verticalGradient(
                            0f to shadowColor.copy(alpha = 0.32f),
                            1f to Color.Transparent
                        )
                        onDrawBehind {
                            drawOval(brush, size = Size(size.width, size.height))
                        }
                    }
            )
            SpeciesImage(
                speciesId = plant.speciesId,
                emoji = plant.emoji,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationZ = sin((swayState.value + index * 0.19f) * 2f * Math.PI.toFloat()) * 2.2f
                    },
                contentScale = ContentScale.Fit,
                fallbackSize = 50.sp,
                alignment = Alignment.BottomCenter
            )
        }
        // 名牌
        Text(
            plant.displayName,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = InkDark,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .clip(RoundedCornerShape(7.dp))
                .background(plate.copy(alpha = 0.72f))
                .padding(horizontal = 6.dp, vertical = 1.dp)
        )
    }
}

// ======================================================================
// 天气特效
// ======================================================================

@Composable
private fun WeatherEffects(condition: WeatherCondition?, isDark: Boolean, isDay: Boolean) {
    val transition = rememberInfiniteTransition(label = "weather")
    // 各层持有独立节奏：云慢漂、雨快落、雪缓降，避免「一个时钟驱动全部」的机械感。
    // 全部保留 State 形式，在绘制阶段读取，只重绘不重组。
    val cloudDrift = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(30000, easing = LinearEasing)),
        label = "cloudDrift"
    )
    val sunSpin = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(36000, easing = LinearEasing)),
        label = "sunSpin"
    )
    val pulse = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3200, easing = LinearEasing), RepeatMode.Reverse),
        label = "weatherPulse"
    )
    val twinkle = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3600, easing = LinearEasing)),
        label = "starTwinkle"
    )
    val shoot = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "shootingStar"
    )
    val rainFall = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(950, easing = LinearEasing)),
        label = "rainFall"
    )
    val snowFall = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(6800, easing = LinearEasing)),
        label = "snowFall"
    )
    val stormCycle = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5600, easing = LinearEasing)),
        label = "stormCycle"
    )
    val mistDrift = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(13000, easing = LinearEasing), RepeatMode.Reverse),
        label = "mistDrift"
    )

    when (condition) {
        WeatherCondition.CLEAR -> if (isDark) {
            StarLayer(twinkle, shoot)
            MoonLayer(pulse)
        } else {
            SunLayer(sunSpin, pulse)
        }
        WeatherCondition.PARTLY_CLOUDY -> {
            if (isDark) {
                StarLayer(twinkle, shoot)
                MoonLayer(pulse)
            } else {
                SunLayer(sunSpin, pulse)
            }
            CloudLayer(cloudDrift, count = 2, storm = false)
        }
        WeatherCondition.CLOUDY -> {
            if (isDark) StarLayer(twinkle, shoot)
            CloudLayer(cloudDrift, count = 3, storm = false)
        }
        WeatherCondition.FOG -> {
            if (isDark) StarLayer(twinkle, shoot)
            CloudLayer(cloudDrift, count = 1, storm = false)
            MistLayer(mistDrift)
        }
        WeatherCondition.DRIZZLE -> {
            CloudLayer(cloudDrift, count = 2, storm = true)
            RainLayer(rainFall, light = true)
        }
        WeatherCondition.RAIN -> {
            CloudLayer(cloudDrift, count = 3, storm = true)
            RainLayer(rainFall, light = false)
        }
        WeatherCondition.THUNDER -> {
            CloudLayer(cloudDrift, count = 3, storm = true)
            RainLayer(rainFall, light = false)
            LightningLayer(stormCycle)
        }
        WeatherCondition.SNOW -> {
            if (isDark) StarLayer(twinkle, shoot)
            CloudLayer(cloudDrift, count = 2, storm = false)
            SnowLayer(snowFall)
        }
        null, WeatherCondition.UNKNOWN -> {
            if (isDark) StarLayer(twinkle, shoot)
            CloudLayer(cloudDrift, count = 1, storm = false)
        }
    }

    // 白天的小生灵：仅在白昼 + 浅色场景出现，雨雪雷雾天自动躲起来
    if (isDay && !isDark) {
        DayCreatures(condition)
    }
}

/** 夜晚：月亮 + 呼吸光晕 + 淡淡光环 */
@Composable
private fun MoonLayer(pulse: State<Float>) {
    val core = MoonCore
    val glow = MoonGlow
    Canvas(modifier = Modifier.fillMaxSize()) {
        val p = pulse.value
        val cx = size.width * 0.84f
        val cy = size.height * 0.14f
        val base = size.minDimension * 0.046f
        drawCircle(
            brush = Brush.radialGradient(
                listOf(glow.copy(alpha = 0.38f + 0.18f * p), Color.Transparent),
                center = Offset(cx, cy),
                radius = base * 4.8f
            ),
            center = Offset(cx, cy),
            radius = base * 4.8f
        )
        drawCircle(
            color = glow.copy(alpha = 0.16f + 0.10f * p),
            radius = base * (1.9f + 0.15f * p),
            center = Offset(cx, cy),
            style = Stroke(width = 1.2f)
        )
        drawCircle(
            brush = Brush.radialGradient(
                listOf(core, glow.copy(alpha = 0.85f)),
                center = Offset(cx - base * 0.3f, cy - base * 0.3f),
                radius = base * 1.6f
            ),
            radius = base,
            center = Offset(cx, cy)
        )
        // 环形山
        drawCircle(glow.copy(alpha = 0.50f), base * 0.22f, Offset(cx - base * 0.28f, cy - base * 0.24f))
        drawCircle(glow.copy(alpha = 0.40f), base * 0.14f, Offset(cx + base * 0.30f, cy + base * 0.22f))
        drawCircle(glow.copy(alpha = 0.32f), base * 0.10f, Offset(cx + base * 0.05f, cy - base * 0.45f))
    }
}

/** 夜晚：闪烁星星 + 十字闪光 + 偶发流星 */
@Composable
private fun StarLayer(twinkle: State<Float>, shoot: State<Float>) {
    val tint = StarTint
    Canvas(modifier = Modifier.fillMaxSize()) {
        val p = twinkle.value
        val twoPi = (Math.PI * 2).toFloat()
        for (i in 0 until 18) {
            val fx = ((i * 71) % 100) / 100f
            val fy = ((i * 37) % 62) / 100f
            val t = 0.35f + 0.65f * abs(sin(p * twoPi + i * 0.7f))
            val r = 0.9f + (i % 3) * 0.6f
            drawCircle(
                color = tint.copy(alpha = t * 0.9f),
                radius = r,
                center = Offset(size.width * fx, size.height * fy)
            )
        }
        // 三颗较大的十字闪光星
        for (i in 0 until 3) {
            val fx = 0.14f + i * 0.31f
            val fy = 0.10f + (i % 2) * 0.16f
            val t = 0.3f + 0.7f * abs(sin(p * twoPi * 0.8f + i * 1.9f))
            val c = Offset(size.width * fx, size.height * fy)
            val len = 3.2f + 1.6f * t
            val col = tint.copy(alpha = 0.85f * t)
            drawLine(col, Offset(c.x - len, c.y), Offset(c.x + len, c.y), 1f, StrokeCap.Round)
            drawLine(col, Offset(c.x, c.y - len), Offset(c.x, c.y + len), 1f, StrokeCap.Round)
            drawCircle(col, 1.1f, c)
        }
        // 流星：每 9 秒划过一次高空
        val sp = shoot.value
        if (sp in 0.05f..0.23f) {
            val f = (sp - 0.05f) / 0.18f
            val fade = sin(f * Math.PI).toFloat()
            val headX = size.width * (0.16f + 0.42f * f)
            val headY = size.height * (0.06f + 0.20f * f)
            val tail = Offset(headX - size.width * 0.10f, headY - size.height * 0.048f)
            val head = Offset(headX, headY)
            drawLine(
                brush = Brush.linearGradient(
                    listOf(Color.Transparent, tint.copy(alpha = 0.8f * fade)),
                    start = tail,
                    end = head
                ),
                start = tail,
                end = head,
                strokeWidth = 1.8f,
                cap = StrokeCap.Round
            )
            drawCircle(tint.copy(alpha = fade), 1.8f, head)
        }
    }
}

/** 太阳：双层呼吸光晕 + 缓慢旋转的长短圆头光芒 + 渐变日核 */
@Composable
private fun SunLayer(spin: State<Float>, pulse: State<Float>) {
    val core = SunCore
    val glow = SunGlow
    val ray = SunRay
    val halo = SunHalo
    Canvas(modifier = Modifier.fillMaxSize()) {
        val s = spin.value
        val p = pulse.value
        val cx = size.width * 0.86f
        val cy = size.height * 0.14f
        val base = size.minDimension * 0.050f
        val twoPi = (Math.PI * 2).toFloat()
        // 外层呼吸光晕
        drawCircle(
            brush = Brush.radialGradient(
                listOf(glow.copy(alpha = 0.50f + 0.22f * p), Color.Transparent),
                center = Offset(cx, cy),
                radius = base * 5f
            ),
            center = Offset(cx, cy),
            radius = base * 5f
        )
        drawCircle(
            brush = Brush.radialGradient(
                listOf(halo.copy(alpha = 0.30f + 0.15f * p), Color.Transparent),
                center = Offset(cx, cy),
                radius = base * 2.6f
            ),
            center = Offset(cx, cy),
            radius = base * 2.6f
        )
        // 细细的光环圈
        drawCircle(
            color = halo.copy(alpha = 0.22f + 0.14f * p),
            radius = base * (2.05f + 0.18f * p),
            center = Offset(cx, cy),
            style = Stroke(width = 1.4f)
        )
        // 12 根圆头光芒，长短相间，整体缓慢旋转
        val rays = 12
        for (i in 0 until rays) {
            val a = (i * (360f / rays) + s * 360f) * (Math.PI / 180f).toFloat()
            val r0 = base * 1.5f
            val breathe = 0.22f * sin(s * twoPi * 3f + i * 0.9f)
            val r1 = base * ((if (i % 2 == 0) 2.45f else 2.0f) + breathe)
            drawLine(
                color = ray.copy(alpha = if (i % 2 == 0) 0.7f else 0.45f),
                start = Offset(cx + r0 * cos(a), cy + r0 * sin(a)),
                end = Offset(cx + r1 * cos(a), cy + r1 * sin(a)),
                strokeWidth = if (i % 2 == 0) 2.8f else 2.2f,
                cap = StrokeCap.Round
            )
        }
        // 日核：偏心径向渐变 + 高光点
        drawCircle(
            brush = Brush.radialGradient(
                listOf(halo, core),
                center = Offset(cx - base * 0.25f, cy - base * 0.25f),
                radius = base * 1.5f
            ),
            radius = base,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.38f),
            radius = base * 0.30f,
            center = Offset(cx - base * 0.30f, cy - base * 0.34f)
        )
    }
}

/** 云：远近错层、速度各异的漂移云朵；雷雨时压暗成乌云 */
@Composable
private fun CloudLayer(drift: State<Float>, count: Int, storm: Boolean) {
    val cloudBody = CloudBody
    val body = if (storm) lerp(cloudBody, Color(0xFF77828E), 0.55f) else cloudBody
    Canvas(modifier = Modifier.fillMaxSize()) {
        val p = drift.value
        val w = size.width
        val h = size.height
        for (i in 0 until count) {
            val far = i % 2 == 1
            val speed = if (far) 0.55f else 0.85f + 0.15f * (i % 3) / 2f
            val raw = (p * speed + i * 0.37f) % 1f
            val x = -w * 0.28f + w * 1.56f * raw
            val y = h * (if (far) 0.09f + 0.05f * (i % 3) else 0.15f + 0.07f * (i % 2))
            val scale = size.minDimension * (if (far) 0.038f else 0.055f + 0.010f * (i % 2))
            val alpha = if (far) 0.55f else 0.92f
            drawCloud(Offset(x, y), scale, body.copy(alpha = alpha))
        }
    }
}

private fun DrawScope.drawCloud(center: Offset, scale: Float, color: Color) {
    val shade = color.copy(alpha = color.alpha * 0.5f)
    // 底部阴影层（向下错位），营造体积感
    drawCircle(color = shade, radius = scale * 0.95f, center = center + Offset(-scale * 0.8f, scale * 0.46f))
    drawCircle(color = shade, radius = scale * 1.12f, center = center + Offset(0f, scale * 0.42f))
    drawCircle(color = shade, radius = scale * 0.75f, center = center + Offset(scale * 0.9f, scale * 0.48f))
    // 主体
    drawCircle(color = color, radius = scale, center = center + Offset(-scale * 0.85f, scale * 0.15f))
    drawCircle(color = color, radius = scale * 1.15f, center = center + Offset(0f, -scale * 0.12f))
    drawCircle(color = color, radius = scale * 0.78f, center = center + Offset(scale * 0.95f, scale * 0.2f))
    drawRoundRect(
        color = color,
        topLeft = Offset(center.x - scale * 1.4f, center.y + scale * 0.1f),
        size = Size(scale * 2.8f, scale * 0.95f),
        cornerRadius = CornerRadius(scale * 0.45f)
    )
    // 云顶高光
    drawCircle(
        color = Color.White.copy(alpha = color.alpha * 0.35f),
        radius = scale * 0.5f,
        center = center + Offset(-scale * 0.2f, -scale * 0.5f)
    )
}

/** 雨：粗细渐变的斜雨丝 + 草线处绽开的小水花 */
@Composable
private fun RainLayer(progress: State<Float>, light: Boolean) {
    val dropColor = WaterBlue
    Canvas(modifier = Modifier.fillMaxSize()) {
        val p = progress.value
        val w = size.width
        val h = size.height
        val horizon = h * 0.72f
        val drops = if (light) 14 else 24
        val alpha = if (light) 0.34f else 0.55f
        val slant = if (light) 0.12f else 0.20f
        for (i in 0 until drops) {
            val fx = ((i * 47) % 100) / 100f
            val speed = 0.8f + (i % 4) * 0.14f
            val phase = (p * speed + i / drops.toFloat()) % 1f
            val y = phase * (horizon + h * 0.06f)
            val x = w * fx + y * slant
            val len = h * (0.045f + 0.02f * (i % 3))
            drawLine(
                brush = Brush.verticalGradient(
                    listOf(dropColor.copy(alpha = alpha * 0.25f), dropColor.copy(alpha = alpha)),
                    startY = y,
                    endY = y + len
                ),
                start = Offset(x, y),
                end = Offset(x + len * slant, y + len),
                strokeWidth = if (light) 1.6f else 2.2f,
                cap = StrokeCap.Round
            )
        }
        // 落地水花：草线上一圈圈扩散的小水环
        val splashes = if (light) 4 else 7
        for (i in 0 until splashes) {
            val fx = ((i * 31 + 9) % 92) / 100f + 0.04f
            val phase = (p * 1.6f + i * 0.41f) % 1f
            val fade = 1f - phase
            val rx = 2.5f + 5.5f * phase
            drawOval(
                color = dropColor.copy(alpha = 0.4f * fade),
                topLeft = Offset(w * fx - rx, horizon + 3f - rx * 0.32f),
                size = Size(rx * 2f, rx * 0.64f),
                style = Stroke(width = 1.1f)
            )
        }
    }
}

/** 雷阵雨：周期性劈下的闪电 + 瞬间被照亮的天空 */
@Composable
private fun LightningLayer(cycle: State<Float>) {
    val boltCore = Color(0xFFFFF8D0)
    val boltGlow = Color(0xFFFFDE7A)
    Canvas(modifier = Modifier.fillMaxSize()) {
        val p = cycle.value
        val w = size.width
        val h = size.height
        // 天空被闪电瞬间照亮（主闪 + 余闪）
        val flash = when {
            p in 0.62f..0.70f -> sin((p - 0.62f) / 0.08f * Math.PI).toFloat()
            p in 0.72f..0.76f -> 0.5f * sin((p - 0.72f) / 0.04f * Math.PI).toFloat()
            else -> 0f
        }
        if (flash > 0f) {
            drawRect(Color.White.copy(alpha = 0.16f * flash))
        }
        val boltAlpha = when {
            p in 0.62f..0.70f -> sin((p - 0.62f) / 0.08f * Math.PI).toFloat()
            p in 0.72f..0.78f -> sin((p - 0.72f) / 0.06f * Math.PI).toFloat()
            else -> 0f
        }
        if (boltAlpha > 0.02f) {
            val bolt = Path().apply {
                moveTo(w * 0.55f, h * 0.14f)
                lineTo(w * 0.45f, h * 0.38f)
                lineTo(w * 0.52f, h * 0.38f)
                lineTo(w * 0.43f, h * 0.60f)
                lineTo(w * 0.58f, h * 0.34f)
                lineTo(w * 0.50f, h * 0.34f)
                lineTo(w * 0.58f, h * 0.14f)
                close()
            }
            drawPath(
                bolt,
                color = boltGlow.copy(alpha = 0.40f * boltAlpha),
                style = Stroke(width = 6f, join = StrokeJoin.Round)
            )
            drawPath(bolt, color = boltCore.copy(alpha = 0.95f * boltAlpha))
        }
    }
}

/** 雾：几条缓缓左右漂移的半透明雾带 */
@Composable
private fun MistLayer(drift: State<Float>) {
    val cloudBody = CloudBody
    Canvas(modifier = Modifier.fillMaxSize()) {
        val p = drift.value
        val w = size.width
        val h = size.height
        val twoPi = (Math.PI * 2).toFloat()
        for (i in 0 until 4) {
            val y = h * (0.30f + i * 0.11f)
            val offset = sin((p + i * 0.25f) * twoPi) * w * 0.06f
            val bandW = w * (0.62f + 0.10f * (i % 2))
            val x = w * (0.08f + 0.06f * (i % 3)) + offset
            val bandH = h * 0.035f
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    listOf(
                        Color.Transparent,
                        cloudBody.copy(alpha = 0.30f),
                        cloudBody.copy(alpha = 0.30f),
                        Color.Transparent
                    ),
                    startX = x,
                    endX = x + bandW
                ),
                topLeft = Offset(x, y),
                size = Size(bandW, bandH),
                cornerRadius = CornerRadius(bandH / 2f)
            )
        }
    }
}

/** 雪：大小不一的雪花摇曳飘落，大雪花是缓缓旋转的六出冰晶 */
@Composable
private fun SnowLayer(progress: State<Float>) {
    val snow = SnowBody
    Canvas(modifier = Modifier.fillMaxSize()) {
        val p = progress.value
        val w = size.width
        val h = size.height
        val twoPi = (Math.PI * 2).toFloat()
        for (i in 0 until 20) {
            val fx = ((i * 61) % 100) / 100f
            val speed = 0.75f + (i % 4) * 0.12f
            val phase = (p * speed + i / 20f) % 1f
            val y = phase * h
            val sway = sin((phase * 1.5f + i * 0.4f) * twoPi) * (8f + (i % 3) * 5f)
            val x = w * fx + sway
            val r = 1.6f + (i % 4) * 0.7f
            if (i % 5 == 0) {
                // 大雪花：三道交叉手臂，随下落缓慢旋转
                val rot = phase * twoPi * 0.5f + i
                val col = snow.copy(alpha = 0.9f)
                for (arm in 0 until 3) {
                    val a = rot + arm * (Math.PI / 3f).toFloat()
                    val dx = cos(a) * r * 2.1f
                    val dy = sin(a) * r * 2.1f
                    drawLine(col, Offset(x - dx, y - dy), Offset(x + dx, y + dy), 1.2f, StrokeCap.Round)
                }
                drawCircle(col, r * 0.5f, Offset(x, y))
            } else {
                val alpha = 0.5f + 0.4f * ((i % 3) / 2f)
                drawCircle(snow.copy(alpha = alpha), r, Offset(x, y))
            }
        }
    }
}

// ======================================================================
// 天空配色
// ======================================================================

private data class SkyPalette(
    val skyTop: Color,
    val skyBottom: Color,
    val labelColor: Color
)

private fun skyPalette(condition: WeatherCondition?, isDark: Boolean): SkyPalette = if (isDark) {
    when (condition) {
        WeatherCondition.CLEAR -> SkyPalette(Color(0xFF101C2E), Color(0xFF1C2C42), Color(0xFFCFDCEC))
        WeatherCondition.PARTLY_CLOUDY -> SkyPalette(Color(0xFF131F30), Color(0xFF20303F), Color(0xFFBCC8D6))
        WeatherCondition.CLOUDY, WeatherCondition.FOG -> SkyPalette(Color(0xFF18222E), Color(0xFF26313C), Color(0xFFB6C0CB))
        WeatherCondition.DRIZZLE, WeatherCondition.RAIN -> SkyPalette(Color(0xFF101A26), Color(0xFF1C2836), Color(0xFF9DB4CC))
        WeatherCondition.THUNDER -> SkyPalette(Color(0xFF141824), Color(0xFF232839), Color(0xFFA9A2C4))
        WeatherCondition.SNOW -> SkyPalette(Color(0xFF16202E), Color(0xFF26333F), Color(0xFFA8C0D6))
        null, WeatherCondition.UNKNOWN -> SkyPalette(Color(0xFF15202B), Color(0xFF222E38), Color(0xFFB4BFC9))
    }
} else {
    when (condition) {
        WeatherCondition.CLEAR -> SkyPalette(Color(0xFFBFE3F2), Color(0xFFEAF6F6), Color(0xFFB07A24))
        WeatherCondition.PARTLY_CLOUDY -> SkyPalette(Color(0xFFC9E3EF), Color(0xFFEFF4F1), Color(0xFF6E7A74))
        WeatherCondition.CLOUDY, WeatherCondition.FOG -> SkyPalette(Color(0xFFD2DAE0), Color(0xFFEDEEEA), Color(0xFF6E7A74))
        WeatherCondition.DRIZZLE, WeatherCondition.RAIN -> SkyPalette(Color(0xFFBFCEDA), Color(0xFFE4EAEC), Color(0xFF4F6B84))
        WeatherCondition.THUNDER -> SkyPalette(Color(0xFFB4BFCB), Color(0xFFE0E2E6), Color(0xFF5A5468))
        WeatherCondition.SNOW -> SkyPalette(Color(0xFFDCE6EE), Color(0xFFF3F6F7), Color(0xFF5C7A91))
        null, WeatherCondition.UNKNOWN -> SkyPalette(Color(0xFFDCE4E2), Color(0xFFF1F2EC), Color(0xFF6E7A74))
    }
}
