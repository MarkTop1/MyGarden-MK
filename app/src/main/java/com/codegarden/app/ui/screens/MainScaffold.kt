package com.codegarden.app.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codegarden.app.ui.components.GardenBackground
import com.codegarden.app.ui.components.GardenIcons
import com.codegarden.app.ui.screens.calendar.CalendarScreen
import com.codegarden.app.ui.screens.catalog.CatalogScreen
import com.codegarden.app.ui.screens.home.HomeScreen
import com.codegarden.app.ui.screens.profile.ProfileScreen
import com.codegarden.app.ui.theme.CardWarm
import com.codegarden.app.ui.theme.Hairline
import com.codegarden.app.ui.theme.InkSecondary
import com.codegarden.app.ui.theme.MintContainer
import com.codegarden.app.ui.theme.MintDeep
import com.codegarden.app.viewmodel.MainViewModel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

private enum class Tab(val label: String, val icon: ImageVector) {
    PLANTS("花园", GardenIcons.Leaf),
    CALENDAR("日历", GardenIcons.Calendar),
    LIBRARY("图鉴", GardenIcons.BookOpen),
    PROFILE("我的", GardenIcons.Person)
}

/** 主框架：4 Tab + 毛玻璃底部导航（指示器弹性 overshoot 滑动、页面淡入淡出切换） */
@Composable
fun MainScaffold(
    vm: MainViewModel,
    onAddPlant: () -> Unit,
    onAddSpecies: (Int) -> Unit,
    onOpenPlant: (String) -> Unit,
    onOpenTrash: () -> Unit
) {
    val state by vm.state.collectAsStateWithLifecycle()
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val holder = rememberSaveableStateHolder()
    val hazeState = remember { HazeState() }

    Box(modifier = Modifier.fillMaxSize()) {
        GardenBackground()
        Box(modifier = Modifier.fillMaxSize().haze(hazeState)) {
            Box(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
                Crossfade(
                    targetState = selected,
                    animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
                    label = "tabContent"
                ) { sel ->
                    when (sel) {
                        0 -> holder.SaveableStateProvider("home") {
                            HomeScreen(state, vm, onAddPlant, onOpenPlant)
                        }
                        1 -> holder.SaveableStateProvider("calendar") {
                            CalendarScreen(state, vm, onOpenPlant)
                        }
                        2 -> holder.SaveableStateProvider("catalog") {
                            CatalogScreen(vm, onAddSpecies)
                        }
                        3 -> holder.SaveableStateProvider("profile") {
                            ProfileScreen(state, vm, onOpenTrash)
                        }
                    }
                }
            }
        }
        FrostedNavBar(
            selected = selected,
            onSelect = { selected = it },
            hazeState = hazeState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun FrostedNavBar(
    selected: Int,
    onSelect: (Int) -> Unit,
    hazeState: HazeState,
    modifier: Modifier = Modifier
) {
    // 悬浮玻璃卡片：整条 bar 抬到系统手势区之上，卡片下方只剩页面背景 → 彻底没有白条框
    val cardShape = RoundedCornerShape(26.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 14.dp, shape = cardShape, clip = false)
                .clip(cardShape)
                .background(CardWarm.copy(alpha = 0.95f))
                .hazeChild(
                    state = hazeState,
                    shape = cardShape,
                    style = HazeStyle(
                        tint = CardWarm.copy(alpha = 0.86f),
                        blurRadius = 26.dp,
                        noiseFactor = 0.04f
                    )
                )
                .border(0.8.dp, Hairline.copy(alpha = 0.75f), cardShape)
        ) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp)
            ) {
                val barHeight = 52.dp
                val cellWidth = maxWidth / Tab.entries.size
                val pillWidth = cellWidth * 0.82f
                val targetX = cellWidth * selected + (cellWidth - pillWidth) / 2f

                val indicatorX by animateDpAsState(
                    targetValue = targetX,
                    animationSpec = spring(dampingRatio = 0.62f, stiffness = 400f),
                    label = "navIndicatorX"
                )

                // Pill 指示器（底层）
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = indicatorX)
                        .width(pillWidth)
                        .height(barHeight)
                        .background(MintContainer, RoundedCornerShape(20.dp))
                        .border(0.6.dp, Hairline, RoundedCornerShape(20.dp))
                )
                // Row（上层，透明背景不盖 pill）
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(barHeight),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Tab.entries.forEachIndexed { index, tab ->
                        NavCell(
                            tab = tab,
                            selected = selected == index,
                            modifier = Modifier.weight(1f).fillMaxHeight()
                        ) { onSelect(index) }
                    }
                }
            }
        }
    }
}

@Composable
private fun NavCell(
    tab: Tab,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val fg by animateColorAsState(
        targetValue = if (selected) MintDeep else InkSecondary,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "navFg"
    )
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick() }
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(tab.icon, tab.label, tint = fg, modifier = Modifier.size(21.dp))
        Spacer(Modifier.width(5.dp))
        Text(
            tab.label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = fg,
            maxLines = 1
        )
    }
}
