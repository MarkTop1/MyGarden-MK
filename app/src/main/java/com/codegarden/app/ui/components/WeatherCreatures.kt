package com.codegarden.app.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import com.codegarden.app.data.weather.WeatherCondition
import com.codegarden.app.ui.theme.InkDark
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private val TwoPi = (Math.PI * 2).toFloat()

// 蝴蝶与蜜蜂只在浅色白昼场景出现，用一套固定的柔和草间配色
private val ButterflyAWing = Color(0xFFF2A65A)
private val ButterflyALower = Color(0xFFE8896B)
private val ButterflyBWing = Color(0xFFE8A7B8)
private val ButterflyBLower = Color(0xFFD98BA0)
private val InsectBody = Color(0xFF5A4632)
private val BeeGold = Color(0xFFF5C542)
private val BeeDark = Color(0xFF4A3B28)

/**
 * 白昼花园里的小生灵：
 * 小鸟扇翅—滑翔交替着掠过天空，蝴蝶沿不规则曲线在花间漫游，蜜蜂嗡嗡地悬停打转。
 * 所有相位都以 State 形式在绘制阶段读取，只触发重绘、不触发重组。
 */
@Composable
fun DayCreatures(condition: WeatherCondition?, modifier: Modifier = Modifier) {
    val birds = when (condition) {
        WeatherCondition.CLEAR -> 3
        WeatherCondition.PARTLY_CLOUDY -> 2
        WeatherCondition.CLOUDY, WeatherCondition.UNKNOWN, null -> 1
        else -> 0
    }
    val insects = condition == WeatherCondition.CLEAR || condition == WeatherCondition.PARTLY_CLOUDY
    if (birds == 0 && !insects) return

    val ink = InkDark
    val transition = rememberInfiniteTransition(label = "creatures")

    // 三只鸟各自有不同的横越周期与扇翅节奏（阴天时只画第一只）
    val birdPath0 = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(17000, easing = LinearEasing)),
        label = "birdPath0"
    )
    val birdPath1 = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(23000, easing = LinearEasing)),
        label = "birdPath1"
    )
    val birdPath2 = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(29000, easing = LinearEasing)),
        label = "birdPath2"
    )
    val birdFlap0 = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(430, easing = LinearEasing)),
        label = "birdFlap0"
    )
    val birdFlap1 = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(520, easing = LinearEasing)),
        label = "birdFlap1"
    )
    val birdFlap2 = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(610, easing = LinearEasing)),
        label = "birdFlap2"
    )
    // 蝴蝶漫游 + 共用的高频扇翅；蜜蜂悬停 + 振翅
    val butterfly0 = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(13000, easing = LinearEasing)),
        label = "butterfly0"
    )
    val butterfly1 = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(17500, easing = LinearEasing)),
        label = "butterfly1"
    )
    val wingFlap = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(260, easing = LinearEasing)),
        label = "wingFlap"
    )
    val beeHover = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "beeHover"
    )
    val beeBuzz = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(90, easing = LinearEasing)),
        label = "beeBuzz"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val unit = size.minDimension

        // —— 小鸟：高空弧线横越，扇翅一阵、滑翔一阵 ——
        val birdPaths = listOf(birdPath0.value, birdPath1.value, birdPath2.value)
        val birdFlaps = listOf(birdFlap0.value, birdFlap1.value, birdFlap2.value)
        for (i in 0 until birds) {
            val t = birdPaths[i]
            val dir = if (i == 1) -1f else 1f
            val margin = 0.14f
            val rawX = -margin + (1f + 2f * margin) * t
            val x = w * (if (dir > 0) rawX else 1f - rawX)
            val baseY = h * (0.16f + 0.09f * i)
            val bob = sin(t * TwoPi * 2f + i * 1.7f) * h * 0.030f +
                sin(t * TwoPi * 5.3f + i) * h * 0.008f
            // 扇翅幅度被慢波调制：一阵猛扇、一阵平伸滑翔
            val burst = 0.35f + 0.65f * (0.5f + 0.5f * sin(t * TwoPi * 1.6f + i * 2.1f))
            val flap = sin(birdFlaps[i] * TwoPi) * burst
            // 进出画面边缘时淡入淡出，避免凭空出现
            val edgeFade = minOf(1f, t / 0.06f, (1f - t) / 0.06f)
            val birdSize = unit * (0.030f - 0.006f * i)
            val alpha = (if (i == 2) 0.45f else 0.72f) * edgeFade
            drawBird(Offset(x, baseY + bob), birdSize, flap, dir, ink.copy(alpha = alpha))
        }

        if (!insects) return@Canvas

        // —— 蝴蝶：双频叠加的不规则漫游，身体顺着飞势倾斜 ——
        val t0 = butterfly0.value
        val x0 = w * (0.30f + 0.20f * sin(t0 * TwoPi) + 0.06f * sin(t0 * TwoPi * 2.7f + 1.2f))
        val y0 = h * (0.70f + 0.07f * sin(t0 * TwoPi * 1.6f + 0.6f) + 0.02f * sin(t0 * TwoPi * 4.3f))
        val tilt0 = cos(t0 * TwoPi) * 18f
        val flap0 = abs(sin(wingFlap.value * TwoPi))
        drawButterfly(Offset(x0, y0), unit * 0.026f, flap0, tilt0, ButterflyAWing, ButterflyALower)

        val t1 = butterfly1.value
        val x1 = w * (0.68f + 0.17f * sin(t1 * TwoPi + 2.4f) + 0.05f * sin(t1 * TwoPi * 3.1f))
        val y1 = h * (0.64f + 0.06f * sin(t1 * TwoPi * 1.9f + 1.1f) + 0.018f * sin(t1 * TwoPi * 4.9f + 0.5f))
        val tilt1 = cos(t1 * TwoPi + 2.4f) * -16f
        val flap1 = abs(sin(wingFlap.value * TwoPi + 1.3f))
        drawButterfly(Offset(x1, y1), unit * 0.021f, flap1, tilt1, ButterflyBWing, ButterflyBLower)

        // —— 蜜蜂：绕着花丛快速打转，翅膀高频振动 ——
        val bt = beeHover.value
        val bx = w * (0.52f + 0.13f * sin(bt * TwoPi * 1.3f) + 0.04f * sin(bt * TwoPi * 3.7f + 0.8f))
        val by = h * (0.76f + 0.045f * sin(bt * TwoPi * 2.1f + 1.4f) + 0.015f * sin(bt * TwoPi * 5.9f))
        val buzz = sin(beeBuzz.value * TwoPi)
        val tiltB = cos(bt * TwoPi * 1.3f) * 14f
        drawBee(Offset(bx, by), unit * 0.022f, buzz, tiltB)
    }
}

/** 小鸟：两道弧形翅膀 + 小小身体，flap ∈ [-1,1] 驱动翼尖上下 */
private fun DrawScope.drawBird(center: Offset, s: Float, flap: Float, dir: Float, color: Color) {
    translate(center.x, center.y) {
        scale(dir, 1f) {
            val tipY = -s * 0.62f * flap
            val ctrlY = -s * 0.18f - s * 0.34f * flap
            val wings = Path().apply {
                moveTo(-s, tipY)
                quadraticBezierTo(-s * 0.45f, ctrlY, 0f, 0f)
                quadraticBezierTo(s * 0.45f, ctrlY, s, tipY)
            }
            drawPath(wings, color, style = Stroke(width = s * 0.18f, cap = StrokeCap.Round))
            drawOval(
                color,
                topLeft = Offset(-s * 0.16f, -s * 0.10f),
                size = Size(s * 0.32f, s * 0.20f)
            )
        }
    }
}

/** 蝴蝶：四片翅膀随 flap ∈ [0,1] 开合，白色斑点 + 细触角 */
private fun DrawScope.drawButterfly(
    center: Offset,
    s: Float,
    flap: Float,
    tilt: Float,
    wingUpper: Color,
    wingLower: Color
) {
    translate(center.x, center.y) {
        rotate(tilt) {
            val open = 0.30f + 0.70f * flap
            val wx = s * 0.92f * open
            // 上翅（大）
            drawOval(wingUpper, topLeft = Offset(-wx - s * 0.10f, -s * 0.78f), size = Size(wx, s * 0.72f))
            drawOval(wingUpper, topLeft = Offset(s * 0.10f, -s * 0.78f), size = Size(wx, s * 0.72f))
            // 下翅（小）
            drawOval(wingLower, topLeft = Offset(-wx * 0.82f - s * 0.06f, -s * 0.10f), size = Size(wx * 0.82f, s * 0.56f))
            drawOval(wingLower, topLeft = Offset(s * 0.06f, -s * 0.10f), size = Size(wx * 0.82f, s * 0.56f))
            // 翅面斑点
            val spot = Color.White.copy(alpha = 0.75f)
            drawCircle(spot, s * 0.13f * open, Offset(-s * 0.10f - wx * 0.5f, -s * 0.44f))
            drawCircle(spot, s * 0.13f * open, Offset(s * 0.10f + wx * 0.5f, -s * 0.44f))
            // 身体与触角
            drawRoundRect(
                InsectBody,
                topLeft = Offset(-s * 0.07f, -s * 0.55f),
                size = Size(s * 0.14f, s * 0.95f),
                cornerRadius = CornerRadius(s * 0.07f)
            )
            drawLine(InsectBody, Offset(0f, -s * 0.52f), Offset(-s * 0.22f, -s * 0.78f), s * 0.05f, StrokeCap.Round)
            drawLine(InsectBody, Offset(0f, -s * 0.52f), Offset(s * 0.22f, -s * 0.78f), s * 0.05f, StrokeCap.Round)
        }
    }
}

/** 蜜蜂：条纹腹部 + 高频振动的半透明翅膀 */
private fun DrawScope.drawBee(center: Offset, s: Float, buzz: Float, tilt: Float) {
    translate(center.x, center.y) {
        rotate(tilt) {
            val wing = Color.White.copy(alpha = 0.60f)
            val wy = s * (0.30f + 0.16f * buzz)
            drawOval(wing, topLeft = Offset(-s * 0.55f, -wy - s * 0.34f), size = Size(s * 0.52f, s * 0.38f))
            drawOval(wing, topLeft = Offset(s * 0.03f, -wy - s * 0.34f), size = Size(s * 0.52f, s * 0.38f))
            // 腹部
            drawOval(BeeGold, topLeft = Offset(-s * 0.62f, -s * 0.30f), size = Size(s * 1.24f, s * 0.60f))
            // 条纹
            val stripe = BeeDark.copy(alpha = 0.85f)
            drawLine(stripe, Offset(-s * 0.10f, -s * 0.22f), Offset(-s * 0.10f, s * 0.22f), s * 0.13f, StrokeCap.Round)
            drawLine(stripe, Offset(s * 0.20f, -s * 0.18f), Offset(s * 0.20f, s * 0.18f), s * 0.12f, StrokeCap.Round)
            // 头
            drawCircle(BeeDark, s * 0.22f, Offset(-s * 0.62f, 0f))
        }
    }
}