package com.codegarden.app.ui.screens.calendar

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import com.codegarden.app.ui.components.DialogAction
import com.codegarden.app.ui.components.GardenConfirmDialog
import com.codegarden.app.ui.components.GardenDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codegarden.app.data.model.AppState
import com.codegarden.app.data.model.CareLog
import com.codegarden.app.data.model.CareType
import com.codegarden.app.data.model.MyPlant
import com.codegarden.app.ui.components.GardenIcons
import com.codegarden.app.ui.components.PagerBar
import com.codegarden.app.ui.components.PhotoOverlay
import com.codegarden.app.ui.components.PlantPhoto
import com.codegarden.app.ui.components.TodayPulseRing
import com.codegarden.app.ui.components.careIcon
import com.codegarden.app.ui.components.pageSlice
import com.codegarden.app.ui.components.totalPages
import com.codegarden.app.ui.theme.CardWarm
import com.codegarden.app.ui.theme.FertilizeGreen
import com.codegarden.app.ui.theme.Hairline
import com.codegarden.app.ui.theme.InkDark
import com.codegarden.app.ui.theme.InkSecondary
import com.codegarden.app.ui.theme.MintContainer
import com.codegarden.app.ui.theme.MintDeep
import com.codegarden.app.ui.theme.MintPrimary
import com.codegarden.app.ui.theme.SageRing
import com.codegarden.app.ui.theme.SoilBrown
import com.codegarden.app.ui.theme.WaterBlue
import com.codegarden.app.viewmodel.MainViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZoneOffset

private val WeekLabels = listOf("一", "二", "三", "四", "五", "六", "日")

/** Tab2 养护日历：月历 + 事件标记 + 植物筛选 + 当日照料清单（补录 / 编辑 / 删除） */
@Composable
fun CalendarScreen(state: AppState, vm: MainViewModel, onOpenPlant: (String) -> Unit) {
    val context = LocalContext.current
    var month by rememberSaveable { mutableStateOf(YearMonth.now()) }
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var filterPlantId by rememberSaveable { mutableStateOf<String?>(null) }
    var editor by remember { mutableStateOf<EditorState?>(null) }
    var deleting by remember { mutableStateOf<CareLog?>(null) }
    var viewerPath by remember { mutableStateOf<String?>(null) }

    val logs = remember(state.logs, filterPlantId) {
        state.logs.filter { filterPlantId == null || it.plantId == filterPlantId }
    }
    val eventsByDate = remember(logs) {
        logs.groupBy { it.date }.mapValues { (_, l) -> l.map { it.type }.toSet() }
    }
    val dayLogs = remember(logs, selectedDate) {
        logs.filter { it.date == selectedDate }.sortedByDescending { it.epochMillis }
    }

    fun openAdd(date: LocalDate) {
        if (state.plants.isEmpty()) {
            Toast.makeText(context, "先去添加一盆植物吧 🌱", Toast.LENGTH_SHORT).show()
        } else {
            editor = EditorState(null, filterPlantId ?: state.plants.first().id, CareType.WATER, date, "")
        }
    }

    Box(Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        Text("每一次照料，都有迹可循", fontSize = 12.sp, color = InkSecondary)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(
                "养护日历",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = InkDark,
                modifier = Modifier.weight(1f)
            )
            PillButton(icon = true, text = "补录") { openAdd(LocalDate.now()) }
        }
        Spacer(Modifier.height(6.dp))
        Text("那些认真照料的小日子。", fontSize = 13.sp, color = InkSecondary)
        Spacer(Modifier.height(16.dp))

        CalendarCard(
            month = month,
            selectedDate = selectedDate,
            eventsByDate = eventsByDate,
            plants = state.plants,
            filterPlantId = filterPlantId,
            onFilter = { filterPlantId = it },
            onPrev = { month = month.minusMonths(1) },
            onNext = { month = month.plusMonths(1) },
            onToday = {
                month = YearMonth.now()
                selectedDate = LocalDate.now()
            },
            onSelect = { selectedDate = it }
        )

        Spacer(Modifier.height(14.dp))

        DayCareCard(
            date = selectedDate,
            logs = dayLogs,
            plants = state.plants,
            onAdd = { openAdd(selectedDate) },
            onOpenPlant = onOpenPlant,
            onEdit = { log ->
                editor = EditorState(log.id, log.plantId, log.type, log.date, log.note)
            },
            onDelete = { deleting = it },
            onViewPhoto = { viewerPath = it }
        )

        Spacer(Modifier.height(110.dp))
    }
        PhotoOverlay(path = viewerPath, onDismiss = { viewerPath = null })
    }

    editor?.let { ed ->
        LogEditorDialog(
            initial = ed,
            plants = state.plants,
            onDismiss = { editor = null },
            onConfirm = { result ->
                if (result.logId == null) {
                    result.plantId?.let { vm.addLog(it, result.type, result.date, result.note) }
                } else {
                    vm.updateLog(result.logId, result.type, result.date, result.note)
                }
                selectedDate = result.date
                month = YearMonth.from(result.date)
                editor = null
            }
        )
    }

    deleting?.let { log ->
        val plant = state.plants.firstOrNull { it.id == log.plantId }
        GardenConfirmDialog(
            onDismiss = { deleting = null },
            title = "删除这条记录",
            icon = GardenIcons.Trash,
            confirmLabel = "删除",
            onConfirm = {
                vm.deleteLog(log.id)
                deleting = null
            },
            message = "将删除「${plant?.displayName ?: "植物"} · ${log.type.label}」的照料记录，确定吗？",
            destructive = true
        )
    }

    // 照料记录照片放大已在上方覆盖层中处理
}

@Composable
private fun CalendarCard(
    month: YearMonth,
    selectedDate: LocalDate,
    eventsByDate: Map<LocalDate, Set<CareType>>,
    plants: List<MyPlant>,
    filterPlantId: String?,
    onFilter: (String?) -> Unit,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    onSelect: (LocalDate) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(26.dp),
        color = CardWarm,
        border = androidx.compose.foundation.BorderStroke(0.8.dp, Hairline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "${month.year}年${month.monthValue}月",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark,
                    modifier = Modifier.weight(1f)
                )
                IconTap(GardenIcons.ChevronLeft, "上个月", onPrev)
                Spacer(Modifier.width(6.dp))
                IconTap(GardenIcons.ChevronRight, "下个月", onNext)
                Spacer(Modifier.width(10.dp))
                Text(
                    "今天",
                    fontSize = 14.sp,
                    color = MintDeep,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onToday() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                PlantFilter(plants, filterPlantId, onFilter, Modifier.weight(1f))
                Spacer(Modifier.width(10.dp))
                LegendDot(WaterBlue, "浇水")
                Spacer(Modifier.width(12.dp))
                LegendDot(FertilizeGreen, "施肥")
            }

            Spacer(Modifier.height(14.dp))

            Row(Modifier.fillMaxWidth()) {
                WeekLabels.forEach { w ->
                    Text(
                        w,
                        fontSize = 13.sp,
                        color = InkSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))

            val offset = (month.atDay(1).dayOfWeek.value - 1)
            val cells = buildList<LocalDate?> {
                repeat(offset) { add(null) }
                for (d in 1..month.lengthOfMonth()) add(month.atDay(d))
                while (size % 7 != 0) add(null)
            }
            cells.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth()) {
                    week.forEach { date ->
                        if (date == null) {
                            Box(Modifier.weight(1f).aspectRatio(1f))
                        } else {
                            DayCell(
                                date = date,
                                selected = date == selectedDate,
                                isToday = date == LocalDate.now(),
                                events = eventsByDate[date] ?: emptySet(),
                                modifier = Modifier.weight(1f),
                                onClick = { onSelect(date) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    selected: Boolean,
    isToday: Boolean,
    events: Set<CareType>,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val hasWater = CareType.WATER in events
    val hasFert = CareType.FERTILIZE in events
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        // 今天脉冲光圈（最底层）
        if (isToday && !selected) {
            TodayPulseRing(color = MintDeep, baseSize = 36.dp)
        }
        if (isToday || selected) {
            Box(Modifier.size(36.dp).background(MintContainer, CircleShape))
        }
        if (hasFert || selected) {
            Box(
                Modifier
                    .size(41.dp)
                    .border(if (selected) 1.8.dp else 1.3.dp, if (selected) MintDeep else FertilizeGreen, CircleShape)
            )
        }
        if (hasWater) {
            Box(
                Modifier
                    .size(if (hasFert || selected) 32.dp else 41.dp)
                    .border(1.3.dp, WaterBlue, CircleShape)
            )
        }
        Text(
            "${date.dayOfMonth}",
            fontSize = 16.sp,
            fontWeight = if (selected || isToday) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MintDeep else InkDark
        )
        // 除虫 / 修剪记录小圆点
        if (CareType.PEST in events || CareType.PRUNE in events) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 3.dp)
                    .size(5.dp)
                    .background(
                        when {
                            CareType.PEST in events && CareType.PRUNE in events -> MintPrimary
                            CareType.PEST in events -> SoilBrown
                            else -> MintPrimary
                        },
                        CircleShape
                    )
            )
        }
    }
}

@Composable
private fun DayCareCard(
    date: LocalDate,
    logs: List<CareLog>,
    plants: List<MyPlant>,
    onAdd: () -> Unit,
    onOpenPlant: (String) -> Unit,
    onEdit: (CareLog) -> Unit,
    onDelete: (CareLog) -> Unit,
    onViewPhoto: (String) -> Unit
) {
    // 每日记录分页：每页 10 条，切换日期或记录数变化时回到第一页
    var page by remember(date, logs.size) { mutableStateOf(0) }
    val total = totalPages(logs.size)
    val pageLogs = logs.pageSlice(page.coerceIn(0, total - 1))

    Surface(
        shape = RoundedCornerShape(26.dp),
        color = CardWarm,
        border = androidx.compose.foundation.BorderStroke(0.8.dp, Hairline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(
                    "${date.monthValue}月${date.dayOfMonth}日 的照料",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = InkDark,
                    modifier = Modifier.weight(1f)
                )
                if (logs.isNotEmpty()) {
                    Text(
                        "${logs.size} 条",
                        fontSize = 12.sp,
                        color = InkSecondary
                    )
                    Spacer(Modifier.width(8.dp))
                }
                IconTap(GardenIcons.Plus, "补录当天", onAdd, tint = MintDeep)
            }
            Spacer(Modifier.height(6.dp))
            if (logs.isEmpty()) {
                Text(
                    "这一天还没有照料记录，点右上角 + 补录一笔。",
                    fontSize = 12.sp,
                    color = InkSecondary,
                    modifier = Modifier.padding(vertical = 14.dp)
                )
            } else {
                pageLogs.forEach { log ->
                    val plant = plants.firstOrNull { it.id == log.plantId } ?: return@forEach
                    CareLogRow(
                        plant = plant,
                        log = log,
                        onClick = { onOpenPlant(plant.id) },
                        onEdit = { onEdit(log) },
                        onDelete = { onDelete(log) },
                        onViewPhoto = onViewPhoto
                    )
                }
                PagerBar(
                    page = page.coerceIn(0, total - 1),
                    totalPages = total,
                    onPage = { page = it }
                )
            }
        }
    }
}

@Composable
private fun CareLogRow(
    plant: MyPlant,
    log: CareLog,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onViewPhoto: (String) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 10.dp)
    ) {
        PlantPhoto(
            path = plant.photoPath,
            emoji = plant.emoji,
            speciesId = plant.speciesId,
            fallbackSize = 24.sp,
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(12.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(plant.displayName, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = InkDark, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text(logStamp(log), fontSize = 12.sp, color = InkSecondary, maxLines = 2)
        }
        log.photoPath?.let { path ->
            Spacer(Modifier.width(8.dp))
            PlantPhoto(
                path = path,
                emoji = "",
                fallbackSize = 20.sp,
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onViewPhoto(path) }
            )
        }
        IconTap(GardenIcons.Pencil, "编辑", onEdit, tint = FertilizeGreen)
        Spacer(Modifier.width(4.dp))
        IconTap(GardenIcons.Trash, "删除", onDelete, tint = InkSecondary)
    }
}

@Composable
private fun PlantFilter(
    plants: List<MyPlant>,
    filterPlantId: String?,
    onFilter: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val label = plants.firstOrNull { it.id == filterPlantId }?.displayName ?: "全部植物"
    Box(modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Text(label, fontSize = 14.sp, color = InkDark, maxLines = 1, modifier = Modifier.weight(1f))
            Icon(GardenIcons.ChevronRight, null, tint = InkSecondary, modifier = Modifier.size(16.dp).rotate(90f))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            // 最多展示 3 行，植物多了可以在下拉里滑动查看
            Column(
                Modifier
                    .heightIn(max = 144.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                DropdownMenuItem(text = { Text("全部植物") }, onClick = { onFilter(null); expanded = false })
                plants.forEach { p ->
                    DropdownMenuItem(text = { Text("${p.emoji} ${p.displayName}") }, onClick = { onFilter(p.id); expanded = false })
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(13.dp).border(1.6.dp, color, CircleShape))
        Spacer(Modifier.width(5.dp))
        Text(label, fontSize = 13.sp, color = InkSecondary)
    }
}

@Composable
private fun PillButton(icon: Boolean, text: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MintContainer)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        if (icon) {
            Icon(GardenIcons.Plus, null, tint = MintDeep, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(5.dp))
        }
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MintDeep)
    }
}

@Composable
private fun IconTap(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    desc: String,
    onClick: () -> Unit,
    tint: Color = InkDark
) {
    Icon(
        icon,
        contentDescription = desc,
        tint = tint,
        modifier = Modifier
            .clip(CircleShape)
            .clickable { onClick() }
            .padding(6.dp)
            .size(20.dp)
    )
}

private data class EditorState(
    val logId: String?,
    val plantId: String?,
    val type: CareType,
    val date: LocalDate,
    val note: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LogEditorDialog(
    initial: EditorState,
    plants: List<MyPlant>,
    onDismiss: () -> Unit,
    onConfirm: (EditorState) -> Unit
) {
    val isEdit = initial.logId != null
    var plantId by remember { mutableStateOf(initial.plantId) }
    var type by remember { mutableStateOf(initial.type) }
    var date by remember { mutableStateOf(initial.date) }
    var note by remember { mutableStateOf(initial.note) }
    var plantMenu by remember { mutableStateOf(false) }
    var pickDate by remember { mutableStateOf(false) }
    val plant = plants.firstOrNull { it.id == plantId }

    GardenDialog(
        onDismiss = onDismiss,
        title = if (isEdit) "编辑照料记录" else "补录照料",
        icon = GardenIcons.Pencil,
        confirm = DialogAction(
            label = "保存",
            enabled = plantId != null,
            onClick = { onConfirm(EditorState(initial.logId, plantId, type, date, note)) }
        ),
        dismiss = DialogAction(label = "取消", onClick = onDismiss)
    ) {
        Column(
            modifier = Modifier.heightIn(max = 460.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("植物", fontSize = 12.sp, color = InkSecondary)
            if (isEdit) {
                Text("${plant?.emoji ?: "🪴"} ${plant?.displayName ?: "植物"}", fontSize = 15.sp, color = InkDark)
            } else {
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { plantMenu = true }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                    ) {
                        Text(
                            plant?.let { "${it.emoji} ${it.displayName}" } ?: "选择植物",
                            fontSize = 14.sp,
                            color = InkDark,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(GardenIcons.ChevronRight, null, tint = InkSecondary, modifier = Modifier.size(16.dp).rotate(90f))
                    }
                    DropdownMenu(expanded = plantMenu, onDismissRequest = { plantMenu = false }) {
                        plants.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.emoji} ${p.displayName}") },
                                onClick = { plantId = p.id; plantMenu = false }
                            )
                        }
                    }
                }
            }

            Text("类型", fontSize = 12.sp, color = InkSecondary)
            CareType.entries.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    pair.forEach { t ->
                        val on = type == t
                        val c = when (t) {
                            CareType.WATER -> WaterBlue
                            CareType.FERTILIZE -> FertilizeGreen
                            CareType.PEST -> SoilBrown
                            CareType.PRUNE -> MintPrimary
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (on) MintContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .border(if (on) 1.4.dp else 0.dp, if (on) c else Color.Transparent, RoundedCornerShape(12.dp))
                                .clickable { type = t }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Icon(
                                careIcon(t),
                                contentDescription = null,
                                tint = if (on) c else InkSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(t.label, fontSize = 14.sp, color = if (on) InkDark else InkSecondary, fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal)
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Text("日期", fontSize = 12.sp, color = InkSecondary)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { pickDate = true }
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Icon(GardenIcons.Calendar, null, tint = MintDeep, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("${date.year}年${date.monthValue}月${date.dayOfMonth}日", fontSize = 14.sp, color = InkDark)
            }

            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(60) },
                label = { Text("备注（可选）") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintDeep, focusedLabelColor = MintDeep),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (pickDate) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { pickDate = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    pickDate = false
                }) { Text("确定") }
            },
            dismissButton = { TextButton(onClick = { pickDate = false }) { Text("取消") } }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

private fun logStamp(log: CareLog): String {
    val t = Instant.ofEpochMilli(log.epochMillis).atZone(ZoneId.systemDefault())
    val hm = "%02d:%02d".format(t.hour, t.minute)
    val base = "${log.type.label} · ${log.date.monthValue}月${log.date.dayOfMonth}日 $hm"
    return if (log.note.isBlank()) base else "$base · ${log.note}"
}