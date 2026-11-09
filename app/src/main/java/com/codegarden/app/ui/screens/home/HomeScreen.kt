package com.codegarden.app.ui.screens.home

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codegarden.app.data.CareEngine
import com.codegarden.app.data.PlantHealthState
import com.codegarden.app.data.model.AppState
import com.codegarden.app.data.model.CareType
import com.codegarden.app.data.model.MyPlant
import com.codegarden.app.ui.components.Avatar
import com.codegarden.app.ui.components.CareBurst
import com.codegarden.app.ui.components.CareLogDialog
import com.codegarden.app.ui.components.GardenIcons
import com.codegarden.app.ui.components.GardenShowcase
import com.codegarden.app.ui.components.PlantPhoto
import com.codegarden.app.ui.components.WeatherChip
import com.codegarden.app.ui.components.WeatherRegionDialog
import com.codegarden.app.ui.theme.Amber
import com.codegarden.app.ui.theme.AmberContainer
import com.codegarden.app.ui.theme.BadgeWarnFg
import com.codegarden.app.ui.theme.BadgeYellowFg
import com.codegarden.app.ui.theme.CriticSurface
import com.codegarden.app.ui.theme.DropBlue
import com.codegarden.app.ui.theme.ErrorRed
import com.codegarden.app.ui.theme.FertilizeGreen
import com.codegarden.app.ui.theme.InkDark
import com.codegarden.app.ui.theme.InkSecondary
import com.codegarden.app.ui.theme.MintContainer
import com.codegarden.app.ui.theme.MintDeep
import com.codegarden.app.ui.theme.MintLight
import com.codegarden.app.ui.theme.MintPrimary
import com.codegarden.app.ui.theme.PinkContainer
import com.codegarden.app.ui.theme.SoilBrown
import com.codegarden.app.ui.theme.WarnSurface
import com.codegarden.app.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.LocalTime
import kotlin.math.cos
import kotlin.math.sin

/** Tab1 我的花园：天气橱窗 + 植物列表（到期优先），右下角添加 */
@Composable
fun HomeScreen(
    state: AppState,
    vm: MainViewModel,
    onAddPlant: () -> Unit,
    onOpenPlant: (String) -> Unit
) {
    val today = LocalDate.now()
    val summary = CareEngine.dueSummary(state.plants, today)
    val sorted = state.plants.sortedBy {
        minOf(CareEngine.daysUntilWater(it, today), CareEngine.daysUntilFertilize(it, today))
    }
    val context = LocalContext.current
    // 待确认的打卡：点快捷按钮后先弹窗，确认后才落库
    var pendingCare by remember { mutableStateOf<Pair<MyPlant, CareType>?>(null) }

    // 进入主页自动刷新天气；首次进入且为自动模式时申请定位权限
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (granted.values.any { it }) vm.useCurrentLocation() else vm.refreshWeather()
    }
    LaunchedEffect(Unit) {
        vm.refreshWeather()
        val s = state.settings
        if (s.weatherAuto && !s.weatherAsked && !vm.hasLocationPermission()) {
            vm.markWeatherAsked()
            permLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header（最先出现）
            item {
                StaggeredWrap(0) {
                    HomeHeader(state, vm) {
                        if (vm.hasLocationPermission()) {
                            vm.useCurrentLocation()
                        } else {
                            permLauncher.launch(
                                arrayOf(
                                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                    android.Manifest.permission.ACCESS_FINE_LOCATION
                                )
                            )
                        }
                    }
                }
            }
            item {
                StaggeredWrap(1) {
                    val weather by vm.weather.collectAsStateWithLifecycle()
                    GardenShowcase(
                        plants = sorted,
                        condition = weather.info?.condition,
                        needWater = summary.needWater,
                        needFertilize = summary.needFertilize,
                        overdue = summary.overdue,
                        onOpenPlant = onOpenPlant
                    )
                }
            }

            if (state.plants.isEmpty()) {
                item { StaggeredWrap(2) { EmptyGarden(onAddPlant) } }
            } else {
                item {
                    StaggeredWrap(2) {
                        Text(
                            "🪴 我的植物 · ${state.plants.size} 盆",
                            fontSize = 14.sp, fontWeight = FontWeight.Bold, color = InkDark,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
                itemsIndexed(sorted, key = { _, p -> p.id }) { idx, plant ->
                    PlantCard(
                        plant = plant,
                        today = today,
                        onOpen = onOpenPlant,
                        onCare = { type -> pendingCare = plant to type },
                        staggerIndex = idx + 3
                    )
                }
            }
        }

        MonsteraAddButton(
            onClick = onAddPlant,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 104.dp)
        )
    }

    // 打卡二次确认：填用量 / 照片 / 备注后落库
    pendingCare?.let { (plant, type) ->
        CareLogDialog(
            plantName = plant.displayName,
            type = type,
            onDismiss = { pendingCare = null },
            onConfirm = { amount, photoPath, note ->
                vm.care(plant.id, type, amount, photoPath, note)
                pendingCare = null
                Toast.makeText(context, "已记录${type.label}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

/** Staggered 包裹：根据 index 延迟渐入 + 上滑 */
@Composable
private fun StaggeredWrap(index: Int, content: @Composable () -> Unit) {
    val delay = (index * 60).coerceAtMost(360)
    AnimatedVisibility(
        visible = true,
        enter = fadeIn(animationSpec = tween(durationMillis = 420, delayMillis = delay)) +
            slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = tween(durationMillis = 420, delayMillis = delay)
            ),
        exit = slideOutVertically(targetOffsetY = { it / 2 })
    ) {
        content()
    }
}

@Composable
private fun HomeHeader(state: AppState, vm: MainViewModel, onRequestLocation: () -> Unit) {
    val hour = LocalTime.now().hour
    val greeting = when {
        hour < 11 -> "早上好"
        hour < 14 -> "中午好"
        hour < 18 -> "下午好"
        else -> "晚上好"
    }
    val dateText = "愿每一片绿叶，都被温柔以待 🌿"
    val weather by vm.weather.collectAsStateWithLifecycle()
    var showRegion by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Avatar(emoji = state.user?.avatar ?: "🧑‍🌾", size = 48.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                "$greeting，${state.user?.nickname ?: "花友"}",
                fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkDark
            )
            Text(dateText, fontSize = 12.sp, color = InkSecondary)
        }
        Spacer(Modifier.width(8.dp))
        WeatherChip(
            state = weather,
            autoMode = state.settings.weatherAuto,
            onClick = { showRegion = true }
        )
    }

    if (showRegion) {
        WeatherRegionDialog(
            currentCity = state.settings.weatherCity.ifBlank { "未设置" },
            autoMode = state.settings.weatherAuto,
            onDismiss = { showRegion = false },
            onUseLocation = {
                showRegion = false
                onRequestLocation()
            },
            onSearch = { q, cb -> vm.searchCities(q, cb) },
            onPick = { hit ->
                showRegion = false
                vm.setWeatherRegion(hit.name, hit.lat, hit.lon)
            }
        )
    }
}

@Composable
private fun PlantCard(
    plant: MyPlant,
    today: LocalDate,
    onOpen: (String) -> Unit,
    onCare: (CareType) -> Unit,
    staggerIndex: Int
) {
    val waterDays = CareEngine.daysUntilWater(plant, today)
    val fertDays = CareEngine.daysUntilFertilize(plant, today)
    val health = CareEngine.healthState(plant, today)

    // 快捷打卡的粒子动效触发计数（点击即播放，确认弹窗后落库）
    var waterBurst by remember { mutableStateOf(0) }
    var fertBurst by remember { mutableStateOf(0) }
    var pestBurst by remember { mutableStateOf(0) }
    var pruneBurst by remember { mutableStateOf(0) }

    StaggeredWrap(staggerIndex) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = healthCardBg(health)),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            modifier = Modifier.fillMaxWidth().clickable { onOpen(plant.id) }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(12.dp)
            ) {
                // 带状态角标的植物图
                Box {
                    PlantPhoto(
                        path = plant.photoPath,
                        emoji = plant.emoji,
                        fallbackSize = 32.sp,
                        speciesId = plant.speciesId,
                        modifier = Modifier.size(70.dp).clip(RoundedCornerShape(18.dp))
                    )
                    // 健康状态角标：右下角小圆 + 严重时白边
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 2.dp, bottom = 2.dp)
                            .size(20.dp)
                            .background(healthDotColor(health), CircleShape)
                            .then(
                                if (health.ordinal >= PlantHealthState.WILTING.ordinal) {
                                    Modifier.border(1.5.dp, Color.White, CircleShape)
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(healthIcon(health), null, tint = Color.White, modifier = Modifier.size(12.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(plant.displayName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = InkDark, maxLines = 1)
                    Text(plant.speciesName, fontSize = 11.sp, color = InkSecondary, maxLines = 1)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        DueChip(GardenIcons.Drop, waterDays)
                        DueChip(GardenIcons.Grain, fertDays)
                    }
                }
                Spacer(Modifier.width(8.dp))
                // 四宫格快捷打卡：浇水 / 施肥 / 除虫 / 修剪
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        CareQuickButton(GardenIcons.Drop, DropBlue, waterDays <= 0, waterBurst, CareType.WATER) {
                            waterBurst++
                            onCare(CareType.WATER)
                        }
                        CareQuickButton(GardenIcons.Grain, FertilizeGreen, fertDays <= 0, fertBurst, CareType.FERTILIZE) {
                            fertBurst++
                            onCare(CareType.FERTILIZE)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        CareQuickButton(GardenIcons.Bug, SoilBrown, false, pestBurst, CareType.PEST) {
                            pestBurst++
                            onCare(CareType.PEST)
                        }
                        CareQuickButton(GardenIcons.Scissors, MintPrimary, false, pruneBurst, CareType.PRUNE) {
                            pruneBurst++
                            onCare(CareType.PRUNE)
                        }
                    }
                }
            }
        }
    }
}

/** 卡片右侧的快捷打卡按钮：纯图形无气泡外圈，到期用满色强调，点击仍有粒子动效 */
@Composable
private fun CareQuickButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    due: Boolean,
    burst: Int,
    careType: CareType,
    onClick: () -> Unit
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(38.dp)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (due) tint else tint.copy(alpha = 0.72f),
            modifier = Modifier.size(if (due) 24.dp else 22.dp)
        )
        CareBurst(trigger = burst, careType = careType, modifier = Modifier.fillMaxSize())
    }
}

/** 卡片背景色：健康状态严重时稍微染色 */
@Composable
private fun healthCardBg(health: PlantHealthState): Color = when (health) {
    PlantHealthState.THRIVING, PlantHealthState.OK -> MaterialTheme.colorScheme.surface
    PlantHealthState.NEEDS_ATTENTION -> MaterialTheme.colorScheme.surface
    PlantHealthState.WILTING -> WarnSurface
    PlantHealthState.YELLOWING -> WarnSurface
    PlantHealthState.WILTED -> CriticSurface
}

/** 角标圆点底色 */
@Composable
private fun healthDotColor(health: PlantHealthState): Color = when (health) {
    PlantHealthState.THRIVING, PlantHealthState.OK -> MintPrimary
    PlantHealthState.NEEDS_ATTENTION -> Amber
    PlantHealthState.WILTING -> BadgeWarnFg
    PlantHealthState.YELLOWING -> BadgeYellowFg
    PlantHealthState.WILTED -> ErrorRed
}

@Composable
private fun healthIcon(health: PlantHealthState): androidx.compose.ui.graphics.vector.ImageVector = when (health) {
    PlantHealthState.THRIVING -> GardenIcons.Sparkle
    PlantHealthState.OK -> GardenIcons.Sprout
    PlantHealthState.NEEDS_ATTENTION -> GardenIcons.Drop
    PlantHealthState.WILTING -> GardenIcons.Wilt
    PlantHealthState.YELLOWING -> GardenIcons.Wilt
    PlantHealthState.WILTED -> GardenIcons.Wilt
}

@Composable
private fun DueChip(icon: androidx.compose.ui.graphics.vector.ImageVector, days: Int) {
    val (bg, fg) = when {
        days < 0 -> PinkContainer to ErrorRed
        days == 0 -> AmberContainer to Amber
        else -> MintContainer to MintDeep
    }
    Surface(shape = RoundedCornerShape(8.dp), color = bg) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Icon(icon, null, tint = fg, modifier = Modifier.size(11.dp))
            Spacer(Modifier.width(3.dp))
            Text(
                CareEngine.dueText(days),
                fontSize = 10.sp, fontWeight = FontWeight.Medium, color = fg
            )
        }
    }
}

@Composable
private fun EmptyGarden(onAddPlant: () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(vertical = 36.dp, horizontal = 24.dp)
        ) {
            Text("🪴", fontSize = 56.sp)
            Spacer(Modifier.height(12.dp))
            Text("花园还空空的", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = InkDark)
            Spacer(Modifier.height(6.dp))
            Text(
                "从 500 种常见盆栽里挑一盆，\n或者拍照记录你自己的植物",
                fontSize = 12.sp, color = InkSecondary, textAlign = TextAlign.Center, lineHeight = 18.sp
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onAddPlant,
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MintDeep)
            ) {
                Text("＋ 添加第一盆植物", fontSize = 14.sp)
            }
        }
    }
}

/**
 * 添加植物按钮：无气泡外圈，一株微微透明的龟背竹直接浮在页面上，
 * 脚下带一抹柔和投影，点击仍是添加植物。
 */
@Composable
private fun MonsteraAddButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.90f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = 700f),
        label = "addButtonScale"
    )
    val mintDeep = MintDeep
    val mintPrimary = MintPrimary
    val mintLight = MintLight

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .size(60.dp)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) {
        Canvas(Modifier.size(56.dp)) {
            // 叶下柔和投影，让叶子自然浮在背景上
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(mintDeep.copy(alpha = 0.26f), Color.Transparent),
                    center = Offset(size.width / 2f, size.height * 0.95f),
                    radius = size.width * 0.32f
                ),
                topLeft = Offset(size.width * 0.18f, size.height * 0.79f),
                size = Size(size.width * 0.64f, size.height * 0.16f)
            )
            // 龟背竹叶片（微微透明，透出背后景色）
            drawMonsteraLeaf(
                fill = Brush.linearGradient(
                    colors = listOf(
                        mintLight.copy(alpha = 0.55f),
                        mintPrimary.copy(alpha = 0.82f),
                        mintDeep.copy(alpha = 0.88f)
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(size.width, size.height)
                ),
                vein = Color.White.copy(alpha = 0.55f),
                stem = mintDeep.copy(alpha = 0.90f)
            )
        }
    }
}

/** 手绘龟背竹：心形叶面 + 四条放射状裂口 + 叶柄（EvenOdd 让裂口透出玻璃底色） */
private fun DrawScope.drawMonsteraLeaf(fill: Brush, vein: Color, stem: Color) {
    val w = size.width
    val h = size.height
    val cx = w * 0.50f
    val cy = h * 0.46f
    val r = w * 0.33f

    // 叶柄
    drawLine(
        color = stem,
        start = Offset(cx, cy + r * 0.92f),
        end = Offset(w * 0.555f, h * 0.99f),
        strokeWidth = w * 0.05f,
        cap = StrokeCap.Round
    )

    // 放射状裂口：从叶缘切向叶心，形成龟背竹标志性的缺口
    fun slitPath(angleDeg: Float): Path {
        val base = Math.toRadians(angleDeg.toDouble())
        val half = Math.toRadians(8.0)
        fun point(radius: Float, angle: Double) = Offset(
            cx + radius * cos(angle).toFloat(),
            cy + radius * sin(angle).toFloat()
        )
        val outerA = point(r * 1.45f, base - half)
        val outerB = point(r * 1.45f, base + half)
        val inner = point(r * 0.40f, base)
        return Path().apply {
            moveTo(outerA.x, outerA.y)
            lineTo(outerB.x, outerB.y)
            lineTo(inner.x, inner.y)
            close()
        }
    }

    val body = Path().apply {
        // 心形叶面
        moveTo(cx, cy + r)
        cubicTo(
            cx - r * 0.95f, cy + r * 0.84f,
            cx - r, cy - r * 0.16f,
            cx - r * 0.72f, cy - r * 0.68f
        )
        cubicTo(
            cx - r * 0.45f, cy - r * 1.14f,
            cx + r * 0.45f, cy - r * 1.14f,
            cx + r * 0.72f, cy - r * 0.68f
        )
        cubicTo(
            cx + r, cy - r * 0.16f,
            cx + r * 0.95f, cy + r * 0.84f,
            cx, cy + r
        )
        close()
    }
    // 左右各两道裂口，用布尔差集从叶面挖掉
    val cuts = Path().apply {
        addPath(slitPath(150f))
        addPath(slitPath(205f))
        addPath(slitPath(335f))
        addPath(slitPath(30f))
    }
    val leaf = Path().apply { op(body, cuts, PathOperation.Difference) }
    drawPath(leaf, fill)

    // 主叶脉
    drawLine(
        vein,
        Offset(cx, cy + r * 0.86f),
        Offset(cx, cy - r * 0.62f),
        w * 0.018f,
        StrokeCap.Round
    )
}
