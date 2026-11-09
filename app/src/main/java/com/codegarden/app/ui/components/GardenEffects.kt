package com.codegarden.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.codegarden.app.ui.theme.AmberContainer
import com.codegarden.app.ui.theme.CardWarm
import com.codegarden.app.ui.theme.FertilizeGreen
import com.codegarden.app.ui.theme.GlowAmber
import com.codegarden.app.ui.theme.GlowMint
import com.codegarden.app.ui.theme.GlowPink
import com.codegarden.app.ui.theme.MintContainer
import com.codegarden.app.ui.theme.MintLight
import com.codegarden.app.ui.theme.MintPrimary
import com.codegarden.app.ui.theme.SoilBrown
import com.codegarden.app.ui.theme.WarmBg
import com.codegarden.app.ui.theme.WarmBgBottom
import com.codegarden.app.ui.theme.WaterBlue
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ======================================================================
// 一、背景层：暖米色渐变 + 微妙光晕 + 噪点纹理
// ======================================================================

/** 应用级背景：顶部柔和光晕 + 底部微纹理 */
@Composable
fun GardenBackground(modifier: Modifier = Modifier) {
    val bgTop = WarmBg
    val bgBottom = WarmBgBottom
    val glowMint = GlowMint
    val glowPink = GlowPink
    val glowAmber = GlowAmber
    Box(modifier = modifier.fillMaxSize()) {
        // 基础暖米色渐变
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // 主渐变：顶部稍亮，底部稍沉
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        bgTop,
                        bgBottom
                    ),
                    startY = 0f,
                    endY = h
                )
            )
            // 左上角光晕 — 薄荷绿
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowMint, Color.Transparent),
                    center = Offset(w * 0.12f, h * 0.08f),
                    radius = minOf(w, h) * 0.45f
                ),
                center = Offset(w * 0.12f, h * 0.08f),
                radius = minOf(w, h) * 0.45f
            )
            // 右下角光晕 — 暖粉
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowPink, Color.Transparent),
                    center = Offset(w * 0.88f, h * 0.92f),
                    radius = minOf(w, h) * 0.40f
                ),
                center = Offset(w * 0.88f, h * 0.92f),
                radius = minOf(w, h) * 0.40f
            )
            // 中部微琥珀光晕
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowAmber, Color.Transparent),
                    center = Offset(w * 0.70f, h * 0.35f),
                    radius = minOf(w, h) * 0.30f
                ),
                center = Offset(w * 0.70f, h * 0.35f),
                radius = minOf(w, h) * 0.30f
            )
        }
        // 噪点纹理叠层（极淡，增加纸质质感）
        NoiseOverlay(alpha = 0.035f)
    }
}

/** 极低透明度噪点，营造纸张质感（点数大幅收敛，避免绘制开销） */
@Composable
private fun NoiseOverlay(alpha: Float = 0.03f) {
    val seed = remember { Random(20241109) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .drawWithCache {
                val w = size.width
                val h = size.height
                // 每 9000 平方像素一个点，上限 260，足够产生纸感又不拖慢渲染
                val count = ((w * h) / 9000f).toInt().coerceAtMost(260)
                val dots = List(count) {
                    Offset(seed.nextFloat() * w, seed.nextFloat() * h)
                }
                val dotColor = Color.Black.copy(alpha = alpha)
                onDrawBehind {
                    dots.forEach { drawCircle(dotColor, 0.7f, it) }
                }
            }
    )
}

/** 页面顶部渐变条（在内容上方） */
@Composable
fun TopGlowBar(modifier: Modifier = Modifier) {
    val glowMint = GlowMint
    Box(modifier = modifier.fillMaxWidth().height(80.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        glowMint.copy(alpha = 0.25f),
                        Color.Transparent
                    )
                )
            )
        }
    }
}

// ======================================================================
// 二、入场动画：卡片 staggered 入场
// ======================================================================

/** 带延迟的渐入+上滑入场，用于 LazyColumn item 逐个出现 */
@Composable
fun StaggeredItem(
    index: Int,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = 420,
                delayMillis = (index * 55).coerceAtMost(330),
                easing = FastOutSlowInEasing
            )
        ) + slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = tween(
                durationMillis = 420,
                delayMillis = (index * 55).coerceAtMost(330),
                easing = FastOutSlowInEasing
            )
        ),
        exit = fadeOut()
    ) {
        content()
    }
}

/** 整体入场（用于页面顶层容器） */
@Composable
fun PageEntrance(content: @Composable () -> Unit) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(300)) +
            slideInVertically(initialOffsetY = { 24 }, animationSpec = tween(350, easing = FastOutSlowInEasing)),
    ) {
        content()
    }
}

// ======================================================================
// 三、浇水 / 施肥打卡粒子动效
// ======================================================================

/** 打卡粒子效果：在触发位置喷出水滴 / 星光，播放一次后自动消失 */
@Composable
fun CareBurst(
    trigger: Int,           // 递增即可重新触发
    careType: com.codegarden.app.data.model.CareType,
    modifier: Modifier = Modifier.size(34.dp)
) {
    if (trigger <= 0) return
    val progress = remember { Animatable(0f) }
    var playing by remember { mutableStateOf(false) }
    LaunchedEffect(trigger) {
        playing = true
        progress.snapTo(0f)
        progress.animateTo(1f, tween(durationMillis = 620, easing = FastOutSlowInEasing))
        playing = false
    }
    if (!playing) return

    val isWater = careType == com.codegarden.app.data.model.CareType.WATER
    val particleColor = when (careType) {
        com.codegarden.app.data.model.CareType.WATER -> WaterBlue
        com.codegarden.app.data.model.CareType.FERTILIZE -> FertilizeGreen
        com.codegarden.app.data.model.CareType.PEST -> SoilBrown
        com.codegarden.app.data.model.CareType.PRUNE -> MintPrimary
    }
    val p = progress.value
    val fade = 1f - p
    // 与父容器同尺寸，绘制时向外扩散（不参与布局、不改变按钮大小）
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val base = size.minDimension / 2
        val r = base * 2.6f
        // 中心扩散环：向外扩散并淡出
        drawCircle(
            color = particleColor.copy(alpha = 0.34f * fade),
            radius = base * (1.0f + 1.6f * p),
            center = center,
            style = Stroke(width = 1.5f + 2f * fade)
        )
        // 散射粒子：向外飞散并缩小
        val count = if (isWater) 8 else 6
        repeat(count) { i ->
            val angle = (i * 360f / count) * (Math.PI.toFloat() / 180f)
            val dist = r * (0.75f + 0.45f * p) * (0.85f + (i % 3) * 0.12f)
            val px = center.x + cos(angle) * dist
            val py = center.y + sin(angle) * dist - base * 0.25f * p
            drawCircle(
                color = particleColor.copy(alpha = 0.9f * fade),
                radius = (if (isWater) 3.2f else 2.6f) * fade + 0.6f,
                center = Offset(px, py)
            )
        }
        // 中心亮点：先亮后灭
        drawCircle(
            color = particleColor.copy(alpha = fade),
            radius = base * 0.42f * (1f - p * 0.55f),
            center = center
        )
    }
}

/** 按钮点击脉冲（缩放 + 发光） */
@Composable
fun PulseOnTap(
    active: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (active) 0.90f else 1f,
        animationSpec = tween(durationMillis = 120, easing = FastOutSlowInEasing),
        label = "pulseScale"
    )
    Box(
        modifier = modifier.scale(scale),
        contentAlignment = Alignment.Center
    ) {
        content()
        if (active) {
            val glowScale by animateFloatAsState(
                targetValue = 1.6f,
                animationSpec = tween(200),
                label = "glowScale"
            )
            val glowAlpha by animateFloatAsState(
                targetValue = 0f,
                animationSpec = tween(240),
                label = "glowAlpha"
            )
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .graphicsLayer {
                        scaleX = glowScale; scaleY = glowScale
                        alpha = glowAlpha
                    }
                    .clip(CircleShape)
                    .background(MintLight.copy(alpha = 0.5f))
            )
        }
    }
}

// ======================================================================
// 四、启动页背景：叶子飘动
// ======================================================================

/** 启动页装饰：几片叶子轻微漂浮 */
@Composable
fun FloatingLeaves(modifier: Modifier = Modifier) {
    val infinite = rememberInfiniteTransition(label = "float")
    val offsetY1 by infinite.animateFloat(
        initialValue = -8f, targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(3500, easing = LinearEasing), RepeatMode.Reverse),
        label = "leaf1y"
    )
    val offsetY2 by infinite.animateFloat(
        initialValue = 6f, targetValue = -10f,
        animationSpec = infiniteRepeatable(tween(4200, easing = LinearEasing), RepeatMode.Reverse),
        label = "leaf2y"
    )
    val rot1 by infinite.animateFloat(
        initialValue = -6f, targetValue = 6f,
        animationSpec = infiniteRepeatable(tween(5000, easing = LinearEasing), RepeatMode.Reverse),
        label = "leaf1r"
    )
    val rot2 by infinite.animateFloat(
        initialValue = 4f, targetValue = -4f,
        animationSpec = infiniteRepeatable(tween(4500, easing = LinearEasing), RepeatMode.Reverse),
        label = "leaf2r"
    )
    val leafMint = MintLight.copy(alpha = 0.28f)
    val leafPink = com.codegarden.app.ui.theme.PinkAccent.copy(alpha = 0.22f)
    val leafContainer = MintContainer.copy(alpha = 0.35f)
    Canvas(modifier = modifier.fillMaxSize()) {
        drawLeaf(
            cx = size.width * 0.15f, cy = size.height * 0.22f + offsetY1,
            size = 62f, rotation = rot1,
            color = leafMint
        )
        drawLeaf(
            cx = size.width * 0.86f, cy = size.height * 0.78f + offsetY2,
            size = 52f, rotation = rot2,
            color = leafPink
        )
        drawLeaf(
            cx = size.width * 0.78f, cy = size.height * 0.18f + offsetY1 * 0.6f,
            size = 40f, rotation = -rot1 * 0.8f,
            color = leafContainer
        )
    }
}

private fun DrawScope.drawLeaf(cx: Float, cy: Float, size: Float, rotation: Float, color: Color) {
    translate(cx, cy) {
        scale(size / 240f) {
            rotate(rotation) {
                val path = Path().apply {
                    moveTo(0f, -110f)
                    cubicTo(70f, -90f, 110f, -30f, 95f, 40f)
                    cubicTo(80f, 100f, 10f, 120f, -5f, 90f)
                    cubicTo(-20f, 60f, -25f, -20f, 0f, -110f)
                    close()
                }
                drawPath(path, color = color)
                drawLine(
                    color = color.copy(alpha = 0.6f),
                    start = Offset(0f, -110f),
                    end = Offset(0f, 90f),
                    strokeWidth = 3f
                )
            }
        }
    }
}

// ======================================================================
// 五、日历页丰富化：今天脉冲指示器
// ======================================================================

/** 今天的脉冲光圈 */
@Composable
fun TodayPulseRing(
    modifier: Modifier = Modifier,
    color: Color? = null,
    baseSize: Dp = 36.dp
) {
    val ringColor = color ?: com.codegarden.app.ui.theme.MintDeep
    val infinite = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infinite.animateFloat(
        initialValue = 1f, targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infinite.animateFloat(
        initialValue = 0.55f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    Canvas(modifier = modifier.size(baseSize * 1.4f)) {
        drawCircle(
            color = ringColor.copy(alpha = pulseAlpha),
            radius = (baseSize.toPx() / 2f) * pulseScale,
            center = Offset(size.width / 2, size.height / 2)
        )
    }
}

// ======================================================================
// 六、通用：按钮按压缩放反馈
// ======================================================================

@Composable
fun PressScale(
    pressed: Boolean,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.95f else 1f,
        animationSpec = tween(110, easing = FastOutSlowInEasing),
        label = "pressScale"
    )
    Box(Modifier.graphicsLayer { scaleX = scale; scaleY = scale }) {
        content()
    }
}
