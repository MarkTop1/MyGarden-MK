package com.codegarden.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codegarden.app.data.weather.CityHit
import com.codegarden.app.data.weather.WeatherCondition
import com.codegarden.app.data.weather.WeatherUiState
import com.codegarden.app.ui.theme.Amber
import com.codegarden.app.ui.theme.DropBlue
import com.codegarden.app.ui.theme.InkDark
import com.codegarden.app.ui.theme.InkSecondary
import com.codegarden.app.ui.theme.MintContainer
import com.codegarden.app.ui.theme.MintDeep
import com.codegarden.app.ui.theme.PinkAccent
import com.codegarden.app.ui.theme.WaterBlue
import kotlin.math.roundToInt

/** 天气状况对应的线性图标 */
fun weatherIcon(condition: WeatherCondition): ImageVector = when (condition) {
    WeatherCondition.CLEAR -> GardenIcons.Sun
    WeatherCondition.PARTLY_CLOUDY -> GardenIcons.CloudSun
    WeatherCondition.CLOUDY -> GardenIcons.Cloud
    WeatherCondition.FOG -> GardenIcons.Fog
    WeatherCondition.DRIZZLE, WeatherCondition.RAIN -> GardenIcons.Rain
    WeatherCondition.SNOW -> GardenIcons.Snow
    WeatherCondition.THUNDER -> GardenIcons.Lightning
    WeatherCondition.UNKNOWN -> GardenIcons.CloudSun
}

/** 天气状况配色 */
@Composable
fun weatherTint(condition: WeatherCondition): Color = when (condition) {
    WeatherCondition.CLEAR, WeatherCondition.PARTLY_CLOUDY -> Amber
    WeatherCondition.CLOUDY, WeatherCondition.FOG -> InkSecondary
    WeatherCondition.DRIZZLE, WeatherCondition.RAIN -> DropBlue
    WeatherCondition.SNOW -> WaterBlue
    WeatherCondition.THUNDER -> PinkAccent
    WeatherCondition.UNKNOWN -> InkSecondary
}

/** 主页右上角天气：无边框，写实手绘天气小插画 + 温度 + 城市 */
@Composable
fun WeatherChip(
    state: WeatherUiState,
    autoMode: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val info = state.info
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (info != null) {
            WeatherArt(info.condition, modifier = Modifier.size(44.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    "${info.tempC.roundToInt()}°",
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark,
                    lineHeight = 23.sp
                )
                Text(
                    "${info.condition.label} · ${state.cityName.ifBlank { "未知地区" }}",
                    fontSize = 11.sp,
                    color = InkSecondary,
                    maxLines = 1
                )
            }
        } else if (state.loading) {
            CircularProgressIndicator(
                color = MintDeep,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("天气加载中", fontSize = 12.sp, color = InkSecondary)
        } else {
            WeatherArt(WeatherCondition.UNKNOWN, modifier = Modifier.size(40.dp))
            Spacer(Modifier.width(8.dp))
            Column {
                Text("设置地区", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = InkDark)
                Text(
                    if (autoMode) "定位未开启" else "点击选择城市",
                    fontSize = 11.sp,
                    color = InkSecondary
                )
            }
        }
    }
}

// ======================================================================
// 写实天气小插画（Canvas 手绘：太阳/云/雨/雪/雷）
// ======================================================================

private val ArtSun = Color(0xFFFFC53D)
private val ArtSunDeep = Color(0xFFFF9E3D)
private val ArtCloud = Color(0xFFF6F9FB)
private val ArtCloudShade = Color(0xFFD9E2EA)
private val ArtRain = Color(0xFF5FA8E0)
private val ArtBolt = Color(0xFFFFC93C)
private val ArtSnow = Color(0xFFEAF3FA)

/** 写实天气插画：柔和渐变太阳 + 立体云朵，尺寸随 modifier 缩放 */
@Composable
fun WeatherArt(condition: WeatherCondition, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val u = minOf(w, h) / 24f   // 基本单位

        fun sun(cx: Float, cy: Float, r: Float, rays: Boolean) {
            if (rays) {
                for (i in 0 until 8) {
                    val a = Math.toRadians((i * 45.0))
                    drawLine(
                        color = ArtSun.copy(alpha = 0.85f),
                        start = Offset(cx + r * 1.25f * kotlin.math.cos(a).toFloat(), cy + r * 1.25f * kotlin.math.sin(a).toFloat()),
                        end = Offset(cx + r * 1.7f * kotlin.math.cos(a).toFloat(), cy + r * 1.7f * kotlin.math.sin(a).toFloat()),
                        strokeWidth = u * 0.55f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(ArtSun, ArtSunDeep),
                    center = Offset(cx, cy),
                    radius = r * 1.15f
                ),
                radius = r,
                center = Offset(cx, cy)
            )
            // 高光
            drawCircle(
                color = Color.White.copy(alpha = 0.5f),
                radius = r * 0.3f,
                center = Offset(cx - r * 0.28f, cy - r * 0.3f)
            )
        }

        fun cloud(cx: Float, cy: Float, s: Float, body: Color = ArtCloud, shade: Color = ArtCloudShade) {
            // 底部阴影层（错位，营造立体感）
            drawCircle(color = shade, radius = s * 0.95f, center = Offset(cx - s * 0.7f, cy + s * 0.22f))
            drawCircle(color = shade, radius = s * 1.15f, center = Offset(cx, cy + s * 0.18f))
            drawCircle(color = shade, radius = s * 0.8f, center = Offset(cx + s * 0.85f, cy + s * 0.26f))
            // 主体
            drawCircle(color = body, radius = s * 0.88f, center = Offset(cx - s * 0.72f, cy + s * 0.05f))
            drawCircle(color = body, radius = s * 1.12f, center = Offset(cx, cy - s * 0.08f))
            drawCircle(color = body, radius = s * 0.74f, center = Offset(cx + s * 0.82f, cy + s * 0.1f))
            // 顶部高光
            drawCircle(color = Color.White.copy(alpha = 0.55f), radius = s * 0.5f, center = Offset(cx - s * 0.25f, cy - s * 0.42f))
        }

        when (condition) {
            WeatherCondition.CLEAR -> sun(w * 0.5f, h * 0.5f, u * 5.2f, rays = true)

            WeatherCondition.PARTLY_CLOUDY, WeatherCondition.UNKNOWN -> {
                sun(w * 0.36f, h * 0.36f, u * 4f, rays = true)
                cloud(w * 0.56f, h * 0.62f, u * 4.6f)
            }

            WeatherCondition.CLOUDY -> {
                cloud(w * 0.38f, h * 0.42f, u * 3.6f, body = ArtCloud.copy(alpha = 0.75f), shade = ArtCloudShade.copy(alpha = 0.7f))
                cloud(w * 0.58f, h * 0.6f, u * 4.8f)
            }

            WeatherCondition.FOG -> {
                cloud(w * 0.5f, h * 0.4f, u * 4f)
                for (i in 0 until 3) {
                    val y = h * (0.72f + i * 0.1f)
                    drawLine(
                        color = ArtCloudShade,
                        start = Offset(w * 0.18f, y),
                        end = Offset(w * (0.82f - i * 0.1f), y),
                        strokeWidth = u * 0.7f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }

            WeatherCondition.DRIZZLE, WeatherCondition.RAIN -> {
                cloud(w * 0.5f, h * 0.38f, u * 4.6f)
                val drops = if (condition == WeatherCondition.DRIZZLE) 3 else 5
                for (i in 0 until drops) {
                    val x = w * (0.24f + i * 0.52f / drops + 0.1f)
                    val y0 = h * 0.66f
                    drawLine(
                        color = ArtRain,
                        start = Offset(x, y0 + (i % 2) * u * 1.2f),
                        end = Offset(x - u * 0.8f, y0 + u * 2.6f + (i % 2) * u * 1.2f),
                        strokeWidth = u * 0.6f,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }

            WeatherCondition.THUNDER -> {
                cloud(w * 0.5f, h * 0.38f, u * 4.6f)
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.54f, h * 0.58f)
                    lineTo(w * 0.42f, h * 0.78f)
                    lineTo(w * 0.50f, h * 0.78f)
                    lineTo(w * 0.44f, h * 0.95f)
                    lineTo(w * 0.60f, h * 0.72f)
                    lineTo(w * 0.51f, h * 0.72f)
                    lineTo(w * 0.62f, h * 0.58f)
                    close()
                }
                drawPath(path, color = ArtBolt)
            }

            WeatherCondition.SNOW -> {
                cloud(w * 0.5f, h * 0.38f, u * 4.6f)
                for (i in 0 until 5) {
                    val x = w * (0.22f + i * 0.56f / 5 + 0.08f)
                    val y = h * (0.68f + (i % 2) * 0.12f)
                    drawCircle(color = ArtSnow, radius = u * 0.75f, center = Offset(x, y))
                }
            }
        }
    }
}

/** 常用城市快捷入口 */
private val presetCities = listOf(
    CityHit("北京", 39.9042, 116.4074, "北京市"),
    CityHit("上海", 31.2304, 121.4737, "上海市"),
    CityHit("广州", 23.1291, 113.2644, "广东省"),
    CityHit("深圳", 22.5431, 114.0579, "广东省"),
    CityHit("杭州", 30.2741, 120.1551, "浙江省"),
    CityHit("成都", 30.5728, 104.0668, "四川省"),
    CityHit("重庆", 29.5630, 106.5516, "重庆市"),
    CityHit("南京", 32.0603, 118.7969, "江苏省"),
    CityHit("武汉", 30.5928, 114.3055, "湖北省"),
    CityHit("西安", 34.3416, 108.9398, "陕西省"),
    CityHit("长沙", 28.2282, 112.9388, "湖南省"),
    CityHit("青岛", 36.0671, 120.3826, "山东省"),
    CityHit("厦门", 24.4798, 118.0894, "福建省"),
    CityHit("苏州", 31.2989, 120.5853, "江苏省"),
    CityHit("昆明", 25.0389, 102.7183, "云南省"),
    CityHit("哈尔滨", 45.8038, 126.5349, "黑龙江省"),
    CityHit("台北", 25.0330, 121.5654, "台湾省")
)

/**
 * 地区设置弹窗：使用当前位置 / 手动搜索城市 / 常用城市。
 */
@Composable
fun WeatherRegionDialog(
    currentCity: String,
    autoMode: Boolean,
    onDismiss: () -> Unit,
    onUseLocation: () -> Unit,
    onSearch: (String, (List<CityHit>) -> Unit) -> Unit,
    onPick: (CityHit) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf(emptyList<CityHit>()) }

    LaunchedEffect(query) {
        if (query.isBlank()) {
            results = emptyList()
            return@LaunchedEffect
        }
        kotlinx.coroutines.delay(400)
        onSearch(query) { results = it }
    }

    GardenDialog(
        onDismiss = onDismiss,
        title = "设置天气地区",
        icon = GardenIcons.Pin
    ) {
        Text(
            if (autoMode) "当前：自动定位（$currentCity）" else "当前：$currentCity",
            fontSize = 12.sp,
            color = InkSecondary
        )
        Spacer(Modifier.height(14.dp))

        // 搜索框
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MintContainer)
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            if (query.isEmpty()) {
                Text("搜索城市，如「杭州」", fontSize = 13.sp, color = InkSecondary)
            }
            BasicTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                textStyle = TextStyle(fontSize = 14.sp, color = InkDark),
                cursorBrush = SolidColor(MintDeep),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(12.dp))

        // 使用当前位置
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MintContainer)
                .clickable { onUseLocation() }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(GardenIcons.Pin, null, tint = MintDeep, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text("使用手机定位", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MintDeep)
        }

        Spacer(Modifier.height(12.dp))

        if (results.isNotEmpty()) {
            Text("搜索结果", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = InkDark)
            Spacer(Modifier.height(6.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(results) { hit ->
                    CityRow(hit) { onPick(hit) }
                }
            }
        } else {
            Text("常用城市", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = InkDark)
            Spacer(Modifier.height(8.dp))
            LazyColumn(
                modifier = Modifier.fillMaxWidth().heightIn(max = 220.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                items(presetCities) { hit ->
                    CityRow(hit) { onPick(hit) }
                }
            }
        }
    }
}

@Composable
private fun CityRow(hit: CityHit, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(GardenIcons.Pin, null, tint = InkSecondary, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(10.dp))
        Text(hit.name, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = InkDark)
        if (hit.region.isNotBlank()) {
            Spacer(Modifier.width(8.dp))
            Text(hit.region, fontSize = 11.sp, color = InkSecondary)
        }
    }
}
