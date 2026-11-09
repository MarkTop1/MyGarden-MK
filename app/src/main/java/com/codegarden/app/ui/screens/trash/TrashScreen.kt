package com.codegarden.app.ui.screens.trash

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codegarden.app.data.model.TRASH_RETENTION_DAYS
import com.codegarden.app.data.model.TrashedPlant
import com.codegarden.app.data.model.formatRelativeTime
import com.codegarden.app.ui.components.GardenBackground
import com.codegarden.app.ui.components.GardenConfirmDialog
import com.codegarden.app.ui.components.GardenIcons
import com.codegarden.app.ui.components.PlantPhoto
import com.codegarden.app.ui.theme.Amber
import com.codegarden.app.ui.theme.AmberContainer
import com.codegarden.app.ui.theme.CardWarm
import com.codegarden.app.ui.theme.ErrorRed
import com.codegarden.app.ui.theme.Hairline
import com.codegarden.app.ui.theme.InkDark
import com.codegarden.app.ui.theme.InkSecondary
import com.codegarden.app.ui.theme.MintContainer
import com.codegarden.app.ui.theme.MintDeep
import com.codegarden.app.viewmodel.MainViewModel
import kotlinx.coroutines.delay

/** 植物回收站：删除的植物保留 30 天，可恢复或彻底删除，带实时倒计时 */
@Composable
fun TrashScreen(vm: MainViewModel, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var pendingPurge by remember { mutableStateOf<TrashedPlant?>(null) }
    // 每秒刷新一次，驱动倒计时
    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }
    val trash = remember(state.trash) {
        state.trash.sortedByDescending { it.deletedAt }
    }

    Box(Modifier.fillMaxSize()) {
        GardenBackground()
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            TrashTopBar(count = trash.size, onBack = onBack)

            if (trash.isEmpty()) {
                EmptyTrash()
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 40.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { TrashTip() }
                    items(trash, key = { it.plant.id }) { item ->
                        TrashCard(
                            item = item,
                            now = now,
                            onRestore = {
                                vm.restorePlant(item.plant.id)
                                Toast.makeText(context, "已恢复「${item.plant.displayName}」🌿", Toast.LENGTH_SHORT).show()
                            },
                            onPurge = { pendingPurge = item }
                        )
                    }
                }
            }
        }
    }

    pendingPurge?.let { item ->
        GardenConfirmDialog(
            onDismiss = { pendingPurge = null },
            title = "彻底删除",
            icon = GardenIcons.Trash,
            confirmLabel = "彻底删除",
            destructive = true,
            onConfirm = {
                pendingPurge = null
                vm.purgePlant(item.plant.id)
                Toast.makeText(context, "已彻底删除，无法恢复", Toast.LENGTH_SHORT).show()
            },
            message = "「${item.plant.displayName}」的照片和养护记录会一起清除，操作后无法恢复，确定吗？"
        )
    }
}

@Composable
private fun TrashTopBar(count: Int, onBack: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(CardWarm, CircleShape)
                .border(0.8.dp, Hairline, CircleShape)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Icon(GardenIcons.ChevronLeft, "返回", tint = InkDark, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text("植物回收站", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = InkDark)
            Text(
                if (count == 0) "暂无删除的植物" else "$count 盆植物等待处理",
                fontSize = 11.sp, color = InkSecondary
            )
        }
    }
}

@Composable
private fun TrashTip() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MintContainer.copy(alpha = 0.7f))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Icon(GardenIcons.Clock, null, tint = MintDeep, modifier = Modifier.size(17.dp))
        Spacer(Modifier.width(10.dp))
        Text(
            "删除的植物会保留 ${TRASH_RETENTION_DAYS} 天，期间可随时恢复；倒计时结束后自动彻底删除。",
            fontSize = 11.sp, lineHeight = 17.sp, color = InkSecondary
        )
    }
}

@Composable
private fun TrashCard(
    item: TrashedPlant,
    now: Long,
    onRestore: () -> Unit,
    onPurge: () -> Unit
) {
    val remaining = item.remainingMillis(now)
    val urgent = remaining < 24 * 60 * 60 * 1000L
    Surface(
        shape = RoundedCornerShape(22.dp),
        color = CardWarm,
        border = androidx.compose.foundation.BorderStroke(0.8.dp, Hairline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                PlantPhoto(
                    path = item.plant.photoPath,
                    emoji = item.plant.emoji,
                    fallbackSize = 28.sp,
                    speciesId = item.plant.speciesId,
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(16.dp))
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        item.plant.displayName,
                        fontSize = 15.sp, fontWeight = FontWeight.Bold, color = InkDark, maxLines = 1
                    )
                    Text(
                        item.plant.speciesName,
                        fontSize = 11.sp, color = InkSecondary, maxLines = 1
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        "删除于 ${formatRelativeTime(item.deletedAt)}",
                        fontSize = 10.sp, color = InkSecondary
                    )
                    Spacer(Modifier.height(5.dp))
                    // 倒计时胶囊
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (urgent) AmberContainer else MintContainer
                    ) {
                        Text(
                            countdownText(remaining),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (urgent) Amber else MintDeep,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = Hairline, modifier = Modifier.padding(vertical = 10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MintDeep)
                        .clickable { onRestore() }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("恢复", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(ErrorRed.copy(alpha = 0.10f))
                        .border(0.8.dp, ErrorRed.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                        .clickable { onPurge() }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("彻底删除", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = ErrorRed)
                }
            }
        }
    }
}

@Composable
private fun EmptyTrash() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().padding(bottom = 100.dp)
    ) {
        Box(
            modifier = Modifier.size(96.dp).background(MintContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(GardenIcons.Trash, null, tint = MintDeep.copy(alpha = 0.7f), modifier = Modifier.size(40.dp))
        }
        Spacer(Modifier.height(18.dp))
        Text("回收站空空的", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = InkDark)
        Spacer(Modifier.height(6.dp))
        Text(
            "删除的植物会暂时保存在这里\n${TRASH_RETENTION_DAYS} 天内都可以找回",
            fontSize = 12.sp, lineHeight = 18.sp, color = InkSecondary, textAlign = TextAlign.Center
        )
    }
}

/** 把剩余毫秒格式化成「剩余 X 天 HH:mm:ss」 */
private fun countdownText(remaining: Long): String {
    if (remaining <= 0L) return "即将彻底删除"
    val totalSec = remaining / 1000
    val days = totalSec / 86400
    val hours = (totalSec % 86400) / 3600
    val minutes = (totalSec % 3600) / 60
    val seconds = totalSec % 60
    return if (days > 0) {
        "剩余 %d 天 %02d:%02d:%02d".format(days, hours, minutes, seconds)
    } else {
        "剩余 %02d:%02d:%02d".format(hours, minutes, seconds)
    }
}
