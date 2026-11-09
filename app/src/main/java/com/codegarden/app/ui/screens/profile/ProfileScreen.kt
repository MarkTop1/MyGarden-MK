package com.codegarden.app.ui.screens.profile

import android.Manifest
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codegarden.app.data.model.AppState
import com.codegarden.app.notify.Reminders
import com.codegarden.app.ui.components.Avatar
import com.codegarden.app.ui.components.DialogAction
import com.codegarden.app.ui.components.GardenDialog
import com.codegarden.app.ui.components.GardenGlyphs
import com.codegarden.app.ui.components.GardenIcons
import com.codegarden.app.ui.components.GlyphIcon
import com.codegarden.app.ui.theme.Amber
import com.codegarden.app.ui.theme.AmberContainer
import com.codegarden.app.ui.theme.CardWarm
import com.codegarden.app.ui.theme.Hairline
import com.codegarden.app.ui.theme.InkDark
import com.codegarden.app.ui.theme.InkSecondary
import com.codegarden.app.ui.theme.MintContainer
import com.codegarden.app.ui.theme.MintDeep
import com.codegarden.app.ui.theme.MintLight
import com.codegarden.app.viewmodel.MainViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val PROFILE_AVATARS = GardenGlyphs.avatarKeys

/** Tab4 我的：离线单机资料 + 统计 + 编辑资料 / 提醒设置 / 植物回收站 / 关于 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(state: AppState, vm: MainViewModel, onOpenTrash: () -> Unit) {
    val context = LocalContext.current
    var editingProfile by remember { mutableStateOf(false) }
    var showReminders by remember { mutableStateOf(false) }
    var showAbout by remember { mutableStateOf(false) }
    var timePickerFor by remember { mutableStateOf<String?>(null) }

    // 从提醒设置里直接申请通知权限；授权成功后马上重排闹钟
    val notificationPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            Reminders.scheduleAll(context, state.settings)
            Toast.makeText(context, "通知已开启，会按时提醒你 🌿", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "通知权限未开启，提醒无法弹出", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        ProfileHeader(state, onEdit = { editingProfile = true })
        Spacer(Modifier.height(22.dp))

        MenuCard {
            MenuRow(GardenIcons.Person, "编辑资料", onClick = { editingProfile = true })
            MenuDivider()
            MenuRow(GardenIcons.Bell, "提醒设置", onClick = { showReminders = true })
            MenuDivider()
            MenuRow(
                icon = GardenIcons.Moon,
                label = if (state.settings.darkMode) "深夜森林模式 🌙" else "浅色治愈模式 ☀️",
                onClick = {},
                trailing = {
                    Switch(
                        checked = state.settings.darkMode,
                        onCheckedChange = { vm.setDarkMode(it) },
                        colors = SwitchDefaults.colors(checkedTrackColor = MintDeep, checkedThumbColor = Color.White)
                    )
                }
            )
            MenuDivider()
            MenuRow(
                icon = GardenIcons.Trash,
                label = "植物回收站",
                onClick = onOpenTrash,
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (state.trash.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .background(AmberContainer, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "${state.trash.size}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Amber
                                )
                            }
                            Spacer(Modifier.width(6.dp))
                        }
                        Icon(
                            GardenIcons.ChevronRight, null,
                            tint = InkSecondary, modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )
            MenuDivider()
            MenuRow(GardenIcons.Info, "关于", onClick = { showAbout = true })
        }

        Spacer(Modifier.height(120.dp))
    }

    if (editingProfile) {
        EditProfileDialog(
            nickname = state.user?.nickname ?: "花友",
            avatar = state.user?.avatar ?: "🌿",
            onDismiss = { editingProfile = false },
            onSave = { name, av ->
                vm.updateProfile(name, av)
                editingProfile = false
                Toast.makeText(context, "资料已更新 🌷", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showReminders) {
        ReminderSettingsDialog(
            state = state,
            vm = vm,
            onPickTime = { timePickerFor = it },
            onDismiss = { showReminders = false },
            onRequestNotification = {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    notificationPermLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    Reminders.openNotificationSettings(context)
                }
            }
        )
    }

    if (showAbout) {
        GardenDialog(
            onDismiss = { showAbout = false },
            title = "关于我的花园",
            icon = GardenIcons.Info,
            confirm = DialogAction("好的", onClick = { showAbout = false })
        ) {
            Column {
                Text("我的花园 v2.0.0", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = InkDark)
                Spacer(Modifier.height(8.dp))
                Text(
                    "一款治愈系植物养护记录应用。内置 1000 种常见盆栽，记录每一盆花什么时候该浇水、什么时候该施肥，到期自动提醒。支持拍照存档。",
                    fontSize = 12.sp, lineHeight = 19.sp, color = InkSecondary
                )
                Spacer(Modifier.height(8.dp))
                Text("离线单机 · 所有数据仅保存在本机。", fontSize = 11.sp, color = InkSecondary)
            }
        }
    }

    timePickerFor?.let { target ->
        val (hour, minute) = when (target) {
            "daily" -> parseTime(state.settings.dailyReminderTime, 9)
            else -> parseTime(state.settings.overdueReminderTime, 18)
        }
        val pickerState = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
        GardenDialog(
            onDismiss = { timePickerFor = null },
            title = if (target == "daily") "每日养护提醒时间" else "逾期预警时间",
            icon = GardenIcons.Clock,
            confirm = DialogAction("确定", onClick = {
                val t = "%02d:%02d".format(pickerState.hour, pickerState.minute)
                if (target == "daily") vm.setDailyReminderTime(t) else vm.setOverdueReminderTime(t)
                timePickerFor = null
            }),
            dismiss = DialogAction("取消", onClick = { timePickerFor = null })
        ) {
            TimePicker(state = pickerState)
        }
    }
}

/**
 * 顶部个人卡片：居中的大圆头像 + 昵称 + 一句签名，底部内嵌三格统计。
 * 整体是一张带柔光与角落叶饰的圆角卡片，替代原先「左头像 + 右文字 + 分割线 + 统计条」的散装布局。
 */
@Composable
private fun ProfileHeader(state: AppState, onEdit: () -> Unit) {
    val days = remember(state.plants, state.logs) { persistDays(state) }
    val careCount = remember(state.plants, state.logs) {
        val activeIds = state.plants.mapTo(HashSet()) { it.id }
        state.logs.count { it.plantId in activeIds }
    }
    val mintContainer = MintContainer
    val mintLight = MintLight
    val mintDeep = MintDeep
    val cardWarm = CardWarm
    val hairline = Hairline

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(
                Brush.verticalGradient(
                    listOf(mintContainer.copy(alpha = 0.92f), cardWarm)
                )
            )
            .border(0.8.dp, hairline, RoundedCornerShape(30.dp))
    ) {
        // 顶部中央柔光
        Canvas(Modifier.fillMaxWidth().height(170.dp)) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(mintLight.copy(alpha = 0.50f), Color.Transparent),
                    center = Offset(size.width / 2, 0f),
                    radius = size.width * 0.62f
                ),
                radius = size.width * 0.62f,
                center = Offset(size.width / 2, 0f)
            )
        }

        // 角落叶饰（极淡，增加层次不抢焦点）
        Icon(
            GardenGlyphs.icon("leaf"),
            contentDescription = null,
            tint = mintDeep.copy(alpha = 0.13f),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 22.dp, top = 26.dp)
                .size(34.dp)
                .rotate(-20f)
        )
        Icon(
            GardenGlyphs.icon("sprout"),
            contentDescription = null,
            tint = mintDeep.copy(alpha = 0.10f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 20.dp, top = 34.dp)
                .size(30.dp)
                .rotate(18f)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth().padding(top = 26.dp, bottom = 16.dp)
        ) {
            // 居中头像：三环衬底 + 右下角相机徽标
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.clickable { onEdit() }
            ) {
                Box(Modifier.size(112.dp).background(mintDeep.copy(alpha = 0.07f), CircleShape))
                Box(
                    Modifier
                        .size(97.dp)
                        .background(cardWarm, CircleShape)
                        .border(1.5.dp, mintLight.copy(alpha = 0.85f), CircleShape)
                )
                Avatar(emoji = state.user?.avatar ?: "🌿", size = 86.dp)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(x = (-8).dp, y = (-6).dp)
                        .size(30.dp)
                        .background(mintDeep, CircleShape)
                        .border(2.5.dp, cardWarm, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(GardenIcons.Camera, "编辑头像", tint = Color.White, modifier = Modifier.size(15.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                state.user?.nickname ?: "花友",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = InkDark,
                maxLines = 1
            )

            Spacer(Modifier.height(18.dp))

            // 内嵌统计面板
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = cardWarm.copy(alpha = 0.78f),
                border = androidx.compose.foundation.BorderStroke(0.8.dp, hairline),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp)
                ) {
                    StatCell("${state.plants.size}", "正在照料", Modifier.weight(1f))
                    VerticalRule()
                    StatCell("$careCount", "养护打卡", Modifier.weight(1f))
                    VerticalRule()
                    StatCell("$days", "坚持天数", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatCell(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = MintDeep)
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 11.sp, color = InkSecondary)
    }
}

@Composable
private fun VerticalRule() {
    Box(Modifier.width(0.8.dp).height(28.dp).background(Hairline))
}

private fun persistDays(state: AppState): Int {
    val dates = state.plants.map { it.addedDate } + state.logs.map { it.date }
    val earliest = dates.minOrNull() ?: return 0
    return (ChronoUnit.DAYS.between(earliest, LocalDate.now()).toInt() + 1).coerceAtLeast(0)
}

@Composable
private fun MenuCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = CardWarm,
        border = androidx.compose.foundation.BorderStroke(0.8.dp, Hairline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp), content = content)
    }
}

@Composable
private fun MenuRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit = { Icon(GardenIcons.ChevronRight, null, tint = InkSecondary, modifier = Modifier.size(18.dp)) }
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 16.dp)
    ) {
        Box(
            modifier = Modifier.size(40.dp).background(MintContainer, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = MintDeep, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(label, fontSize = 16.sp, color = InkDark, modifier = Modifier.weight(1f))
        trailing()
    }
}

@Composable
private fun MenuDivider() {
    HorizontalDivider(color = Hairline, modifier = Modifier.padding(start = 54.dp))
}

@Composable
private fun ReminderSettingsDialog(
    state: AppState,
    vm: MainViewModel,
    onPickTime: (String) -> Unit,
    onDismiss: () -> Unit,
    onRequestNotification: () -> Unit
) {
    val context = LocalContext.current
    val notificationsOk = Reminders.canPostNotifications(context)
    val exactOk = Reminders.canScheduleExact(context)
    GardenDialog(
        onDismiss = onDismiss,
        title = "提醒设置",
        icon = GardenIcons.Bell,
        confirm = DialogAction("完成", onClick = onDismiss)
    ) {
        Column {
            if (!notificationsOk) {
                PermissionNotice(
                    text = "通知权限未开启，到点后无法弹出提醒",
                    action = "开启",
                    onAction = onRequestNotification
                )
                Spacer(Modifier.height(8.dp))
            }
            if (!exactOk) {
                PermissionNotice(
                    text = "系统限制了精确闹钟，提醒可能延迟",
                    action = "去设置",
                    onAction = { Reminders.openExactAlarmSettings(context) }
                )
                Spacer(Modifier.height(8.dp))
            }
            SettingSwitchRow(
                title = "每日养护提醒",
                subtitle = "汇总今天该浇水 / 施肥的植物",
                checked = state.settings.dailyReminder,
                onChange = { vm.setDailyReminder(it) },
                timeText = state.settings.dailyReminderTime,
                onTimeClick = { onPickTime("daily") }
            )
            HorizontalDivider(color = Hairline)
            SettingSwitchRow(
                title = "逾期预警",
                subtitle = "有植物逾期未养护时提醒你",
                checked = state.settings.overdueReminder,
                onChange = { vm.setOverdueReminder(it) },
                timeText = state.settings.overdueReminderTime,
                onTimeClick = { onPickTime("overdue") }
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "提醒会像闹钟一样在设定时间准点通知，App 在后台也会按时弹出。",
                fontSize = 10.sp, lineHeight = 15.sp, color = InkSecondary
            )
        }
    }
}

/** 提醒设置里的权限提示条 */
@Composable
private fun PermissionNotice(text: String, action: String, onAction: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(AmberContainer.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Icon(GardenIcons.Alert, null, tint = Amber, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 11.sp, color = InkSecondary, modifier = Modifier.weight(1f))
        Spacer(Modifier.width(8.dp))
        Text(
            action,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MintDeep,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable { onAction() }
                .padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun SettingSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
    timeText: String,
    onTimeClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = InkDark)
            Text(subtitle, fontSize = 11.sp, color = InkSecondary)
        }
        if (checked) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MintContainer,
                modifier = Modifier.clickable { onTimeClick() }
            ) {
                Text(
                    timeText,
                    fontSize = 12.sp,
                    color = MintDeep,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            Spacer(Modifier.size(8.dp))
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(checkedTrackColor = MintDeep, checkedThumbColor = Color.White)
        )
    }
}

@Composable
private fun EditProfileDialog(
    nickname: String,
    avatar: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var name by remember { mutableStateOf(nickname) }
    var av by remember { mutableStateOf(GardenGlyphs.avatarKey(avatar)) }
    var error by remember { mutableStateOf(false) }
    GardenDialog(
        onDismiss = onDismiss,
        title = "编辑资料",
        icon = GardenIcons.Person,
        confirm = DialogAction("保存", onClick = { if (name.isBlank()) error = true else onSave(name, av) }),
        dismiss = DialogAction("取消", onClick = onDismiss)
    ) {
        Column(
            modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(12); error = false },
                label = { Text("昵称") },
                singleLine = true,
                isError = error,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintDeep, focusedLabelColor = MintDeep),
                modifier = Modifier.fillMaxWidth()
            )
            Text("头像", fontSize = 12.sp, color = InkSecondary)
            PROFILE_AVATARS.chunked(6).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    row.forEach { key ->
                        val selected = av == key
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(40.dp)
                                .background(if (selected) MintContainer else MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                .border(if (selected) 2.dp else 0.dp, if (selected) MintDeep else Color.Transparent, CircleShape)
                                .clickable { av = key }
                        ) {
                            GlyphIcon(key, size = 20.dp, tint = if (selected) MintDeep else InkSecondary)
                        }
                    }
                }
            }
        }
    }
}

private fun parseTime(text: String, defaultHour: Int): Pair<Int, Int> = try {
    val parts = text.split(":")
    Pair(parts[0].toInt(), parts[1].toInt())
} catch (e: Exception) {
    Pair(defaultHour, 0)
}