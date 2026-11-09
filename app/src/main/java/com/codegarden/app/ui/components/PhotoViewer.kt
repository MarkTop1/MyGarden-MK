package com.codegarden.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codegarden.app.ui.theme.InkSecondary
import com.codegarden.app.ui.theme.MintContainer
import com.codegarden.app.ui.theme.MintDeep
import com.codegarden.app.ui.theme.SageRing

/** 全屏看大图：铺满父容器的深色覆盖层，点击任意处关闭（不用 Dialog 窗口，避免影响导航） */
@Composable
fun PhotoOverlay(path: String?, onDismiss: () -> Unit) {
    if (path == null) return
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF21A1A18))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onDismiss() },
        contentAlignment = Alignment.Center
    ) {
        PlantPhoto(
            path = path,
            emoji = "",
            fallbackSize = 80.sp,
            containerColor = Color.Transparent,
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 90.dp)
        )
        // 顶部关闭按钮
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 18.dp, top = 54.dp)
                .size(36.dp)
                .background(Color.White.copy(alpha = 0.16f), CircleShape)
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                GardenIcons.Plus,
                contentDescription = "关闭",
                tint = Color.White,
                modifier = Modifier.size(18.dp).rotate(45f)
            )
        }
        Text(
            "点击任意处关闭",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 46.dp)
        )
    }
}

/** 通用分页条：‹ 上一页 · 1 / 3 · 下一页 › */
@Composable
fun PagerBar(
    page: Int,
    totalPages: Int,
    onPage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (totalPages <= 1) return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth().padding(top = 10.dp)
    ) {
        PageStep(
            text = "上一页",
            enabled = page > 0,
            onClick = { onPage(page - 1) }
        )
        Spacer(Modifier.weight(1f))
        Text(
            "${page + 1} / $totalPages",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = InkSecondary
        )
        Spacer(Modifier.weight(1f))
        PageStep(
            text = "下一页",
            enabled = page < totalPages - 1,
            onClick = { onPage(page + 1) }
        )
    }
}

@Composable
private fun PageStep(text: String, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (enabled) MintContainer else MintContainer.copy(alpha = 0.35f),
        modifier = if (enabled) Modifier.clickable { onClick() } else Modifier
    ) {
        Text(
            text,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (enabled) MintDeep else SageRing,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

/** 把列表按每页 [pageSize] 条切出当前页，并保证页码不越界 */
fun <T> List<T>.pageSlice(page: Int, pageSize: Int = 10): List<T> =
    drop(page * pageSize).take(pageSize)

/** 总页数（至少 1 页） */
fun totalPages(count: Int, pageSize: Int = 10): Int =
    maxOf(1, (count + pageSize - 1) / pageSize)
