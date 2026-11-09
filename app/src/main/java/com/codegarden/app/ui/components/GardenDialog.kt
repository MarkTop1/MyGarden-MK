package com.codegarden.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.codegarden.app.ui.theme.CardWarm
import com.codegarden.app.ui.theme.ErrorRed
import com.codegarden.app.ui.theme.Hairline
import com.codegarden.app.ui.theme.InkDark
import com.codegarden.app.ui.theme.InkSecondary
import com.codegarden.app.ui.theme.MintContainer
import com.codegarden.app.ui.theme.MintDeep
import com.codegarden.app.ui.theme.WarmBg

/** 弹窗按钮动作 */
data class DialogAction(
    val label: String,
    val destructive: Boolean = false,
    val enabled: Boolean = true,
    val onClick: () -> Unit
)

/** 应用统一弹窗：暖米色圆角卡片 + 线描图标标题 + 胶囊按钮，替代系统默认 AlertDialog */
@Composable
fun GardenDialog(
    onDismiss: () -> Unit,
    title: String? = null,
    icon: ImageVector? = null,
    confirm: DialogAction? = null,
    dismiss: DialogAction? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(CardWarm)
                    .border(0.8.dp, Hairline, RoundedCornerShape(26.dp))
                    .padding(22.dp)
            ) {
                if (title != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (icon != null) {
                            Box(
                                modifier = Modifier.size(36.dp).background(MintContainer, RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(icon, null, tint = MintDeep, modifier = Modifier.size(19.dp))
                            }
                            Spacer(Modifier.width(12.dp))
                        }
                        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = InkDark)
                    }
                    Spacer(Modifier.height(16.dp))
                }
                content()
                if (confirm != null || dismiss != null) {
                    Spacer(Modifier.height(20.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (dismiss != null) {
                            DialogPill(dismiss.label, filled = false, destructive = false, enabled = dismiss.enabled, onClick = dismiss.onClick)
                            Spacer(Modifier.width(10.dp))
                        }
                        if (confirm != null) {
                            DialogPill(confirm.label, filled = true, destructive = confirm.destructive, enabled = confirm.enabled, onClick = confirm.onClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DialogPill(label: String, filled: Boolean, destructive: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val bg = when {
        !filled -> Color.Transparent
        destructive -> ErrorRed
        else -> MintDeep
    }
    val fg = when {
        !filled -> InkSecondary
        else -> Color.White
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (enabled) bg else bg.copy(alpha = 0.4f))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = if (enabled) fg else fg.copy(alpha = 0.6f))
    }
}

/** 简洁确认弹窗（删除 / 清除等） */
@Composable
fun GardenConfirmDialog(
    onDismiss: () -> Unit,
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    icon: ImageVector? = null,
    destructive: Boolean = true
) {
    GardenDialog(
        onDismiss = onDismiss,
        title = title,
        icon = icon,
        confirm = DialogAction(confirmLabel, destructive = destructive, onClick = onConfirm),
        dismiss = DialogAction("取消", onClick = onDismiss)
    ) {
        Text(message, fontSize = 13.sp, lineHeight = 20.sp, color = InkSecondary)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun gardenDatePickerColors() = DatePickerDefaults.colors(
    containerColor = CardWarm,
    titleContentColor = InkSecondary,
    headlineContentColor = InkDark,
    weekdayContentColor = InkSecondary,
    subheadContentColor = InkDark,
    navigationContentColor = MintDeep,
    yearContentColor = InkDark,
    disabledYearContentColor = InkSecondary,
    currentYearContentColor = MintDeep,
    selectedYearContainerColor = MintDeep,
    selectedYearContentColor = Color.White,
    disabledSelectedYearContentColor = Color.White,
    dayContentColor = InkDark,
    disabledDayContentColor = InkSecondary,
    selectedDayContainerColor = MintDeep,
    selectedDayContentColor = Color.White,
    disabledSelectedDayContainerColor = MintContainer,
    disabledSelectedDayContentColor = MintDeep,
    todayContentColor = MintDeep,
    todayDateBorderColor = MintDeep,
    dayInSelectionRangeContainerColor = MintContainer,
    dayInSelectionRangeContentColor = MintDeep
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun gardenTimePickerColors() = TimePickerDefaults.colors(
    clockDialColor = WarmBg,
    clockDialSelectedContentColor = Color.White,
    clockDialUnselectedContentColor = InkDark,
    selectorColor = MintDeep,
    containerColor = CardWarm,
    periodSelectorBorderColor = Hairline,
    periodSelectorSelectedContainerColor = MintContainer,
    periodSelectorUnselectedContainerColor = Color.Transparent,
    periodSelectorSelectedContentColor = MintDeep,
    periodSelectorUnselectedContentColor = InkSecondary,
    timeSelectorSelectedContainerColor = MintContainer,
    timeSelectorUnselectedContainerColor = WarmBg,
    timeSelectorSelectedContentColor = MintDeep,
    timeSelectorUnselectedContentColor = InkDark
)