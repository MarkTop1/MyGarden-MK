package com.codegarden.app.ui.screens.detail

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import com.codegarden.app.ui.components.CareAmountChip
import com.codegarden.app.ui.components.CareLogDialog
import com.codegarden.app.ui.components.CareLogThumb
import com.codegarden.app.ui.components.DialogAction
import com.codegarden.app.ui.components.GardenConfirmDialog
import com.codegarden.app.ui.components.GardenDialog
import com.codegarden.app.ui.components.GardenIcons
import com.codegarden.app.ui.components.PagerBar
import com.codegarden.app.ui.components.PhotoOverlay
import com.codegarden.app.ui.components.careAccent
import com.codegarden.app.ui.components.pageSlice
import com.codegarden.app.ui.components.totalPages
import com.codegarden.app.data.local.PhotoStore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codegarden.app.data.CareEngine
import com.codegarden.app.data.model.CareType
import com.codegarden.app.data.model.MyPlant
import com.codegarden.app.data.model.PlantCategory
import com.codegarden.app.data.model.PlantSpecies
import com.codegarden.app.data.model.formatDate
import com.codegarden.app.ui.components.PlantPhoto
import com.codegarden.app.ui.components.careIcon
import com.codegarden.app.ui.theme.AmberContainer
import com.codegarden.app.ui.theme.BadgeYellowFg
import com.codegarden.app.ui.theme.DropBlue
import com.codegarden.app.ui.theme.ErrorRed
import com.codegarden.app.ui.theme.FertilizeGreen
import com.codegarden.app.ui.theme.InkDark
import com.codegarden.app.ui.theme.InkSecondary
import com.codegarden.app.ui.theme.MintContainer
import com.codegarden.app.ui.theme.MintDeep
import com.codegarden.app.ui.theme.MintPrimary
import com.codegarden.app.ui.theme.PinkContainer
import com.codegarden.app.ui.theme.SoilBrown
import com.codegarden.app.viewmodel.MainViewModel
import java.time.LocalDate

/** 植物详情：状态 + 浇水/施肥打卡 + 养护历史 + 编辑/删除 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(vm: MainViewModel, plantId: String, onBack: () -> Unit) {
    val state by vm.state.collectAsStateWithLifecycle()
    val plant = state.plants.firstOrNull { it.id == plantId }
    val context = LocalContext.current
    var showEdit by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    var pendingCare by remember { mutableStateOf<CareType?>(null) }
    var showPhotoMenu by remember { mutableStateOf(false) }
    var viewerPath by remember { mutableStateOf<String?>(null) }
    var pendingCameraPath by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(plant) {
        if (plant == null) onBack()
    }
    if (plant == null) return

    val today = LocalDate.now()
    val waterDays = CareEngine.daysUntilWater(plant, today)
    val fertDays = CareEngine.daysUntilFertilize(plant, today)
    val species = vm.speciesById(plant.speciesId)
    val logs = state.logs.filter { it.plantId == plantId }
    // 养护历史分页：每页 10 条，记录变化时回到第一页
    var historyPage by remember(logs.size) { mutableStateOf(0) }

    // 更换植物图片：拍照 / 相册 / 恢复默认
    fun applyPhoto(path: String?) {
        vm.state.value.plants.firstOrNull { it.id == plantId }?.let { p ->
            vm.updatePlant(p.copy(photoPath = path))
        }
    }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) PhotoStore.savePhoto(context, uri)?.let { applyPhoto(it) }
    }
    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val path = pendingCameraPath
        if (ok && path != null && java.io.File(path).length() > 0) {
            applyPhoto(path)
        } else if (path != null) {
            runCatching { java.io.File(path).delete() }
        }
        pendingCameraPath = null
    }

    Box(Modifier.fillMaxSize()) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(plant.displayName, fontSize = 17.sp, fontWeight = FontWeight.Bold, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showEdit = true }) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .background(MintContainer, CircleShape)
                        ) {
                            Icon(Icons.Filled.Edit, contentDescription = "编辑", tint = MintDeep, modifier = Modifier.size(17.dp))
                        }
                    }
                    IconButton(onClick = { showDelete = true }) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(34.dp)
                                .background(ErrorRed.copy(alpha = 0.12f), CircleShape)
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = "删除", tint = ErrorRed, modifier = Modifier.size(17.dp))
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hero：柔和渐变底 + 右侧植物大图 + 左侧标题/描述/分类胶囊
            Box(modifier = Modifier.fillMaxWidth().height(212.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(28.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(MintContainer.copy(alpha = 0.6f), MaterialTheme.colorScheme.surface)
                            )
                        )
                )
                // 植物图：点击可更换（拍照 / 相册 / 恢复默认图）
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .fillMaxWidth(0.62f)
                        .height(196.dp)
                        .clickable { showPhotoMenu = true }
                ) {
                    PlantPhoto(
                        path = plant.photoPath,
                        emoji = plant.emoji,
                        fallbackSize = 76.sp,
                        speciesId = plant.speciesId,
                        containerColor = Color.Transparent,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    // 右下角相机徽标，提示可点击换图
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 12.dp, bottom = 10.dp)
                            .size(28.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f), CircleShape)
                    ) {
                        Icon(
                            GardenIcons.Camera,
                            contentDescription = "更换植物图片",
                            tint = MintDeep,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .fillMaxWidth(0.5f)
                        .padding(start = 20.dp)
                ) {
                    Text(plant.displayName, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = InkDark, maxLines = 2)
                    Spacer(Modifier.height(5.dp))
                    species?.let {
                        Text(
                            "${it.difficulty} · ${it.light}",
                            fontSize = 12.sp, color = InkSecondary, maxLines = 2
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                    Surface(shape = RoundedCornerShape(10.dp), color = MintContainer) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Icon(GardenIcons.Leaf, contentDescription = null, tint = MintDeep, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                species?.category?.label ?: plant.speciesName,
                                fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MintDeep
                            )
                        }
                    }
                }
            }

            // 养护状态
            val dueToday = waterDays == 0 || fertDays == 0
            val overdueAny = waterDays < 0 || fertDays < 0
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📅 养护计划", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = InkDark)
                        Spacer(Modifier.weight(1f))
                        if (dueToday) {
                            Surface(shape = RoundedCornerShape(8.dp), color = AmberContainer) {
                                Text(
                                    "今日",
                                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = BadgeYellowFg,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        } else if (overdueAny) {
                            Surface(shape = RoundedCornerShape(8.dp), color = PinkContainer) {
                                Text(
                                    "已逾期",
                                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ErrorRed,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                    CareStatusRow(
                        GardenIcons.Drop, "浇水", CareEngine.nextWaterDate(plant), waterDays,
                        "上次：${plant.lastWatered?.let(::formatDate) ?: "还没浇过水"}",
                        tint = DropBlue
                    )
                    CareStatusRow(
                        GardenIcons.Grain, "施肥", CareEngine.nextFertilizeDate(plant), fertDays,
                        "上次：${plant.lastFertilized?.let(::formatDate) ?: "还没施过肥"}",
                        tint = FertilizeGreen
                    )
                }
            }

            // 打卡区：浇水 / 施肥 / 除虫 / 修剪 同处一行
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CareAction(
                        icon = GardenIcons.Drop, label = "浇水", tint = DropBlue,
                        modifier = Modifier.weight(1f)
                    ) { pendingCare = CareType.WATER }
                    CareAction(
                        icon = GardenIcons.Grain, label = "施肥", tint = FertilizeGreen,
                        modifier = Modifier.weight(1f)
                    ) { pendingCare = CareType.FERTILIZE }
                    CareAction(
                        icon = GardenIcons.Bug, label = "除虫", tint = SoilBrown,
                        modifier = Modifier.weight(1f)
                    ) { pendingCare = CareType.PEST }
                    CareAction(
                        icon = GardenIcons.Scissors, label = "修剪", tint = MintPrimary,
                        modifier = Modifier.weight(1f)
                    ) { pendingCare = CareType.PRUNE }
                }
            }

            // 基本信息：左侧档案，右侧该品种的习性小卡
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("🌿 植物档案", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = InkDark)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            species?.let {
                                InfoRow("光照", it.light)
                                InfoRow("难度", it.difficulty)
                            }
                            InfoRow("浇水间隔", "约 ${plant.waterDays} 天一次")
                            InfoRow("施肥间隔", "约 ${plant.fertilizeDays} 天一次")
                            InfoRow("入住花园", formatDate(plant.addedDate))
                            if (plant.note.isNotBlank()) InfoRow("备注", plant.note)
                        }
                        HabitCard(species)
                    }
                }
            }

            // 养护历史
            Card(
                shape = MaterialTheme.shapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("🕘 养护历史 · ${logs.size} 次", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = InkDark)
                    Spacer(Modifier.height(6.dp))
                    if (logs.isEmpty()) {
                        Text("还没有记录，点上面的按钮开始第一次养护吧", fontSize = 12.sp, color = InkSecondary)
                    }
                    val historyPageSize = 10
                    val historyTotal = totalPages(logs.size, historyPageSize)
                    val pageLogs = logs.pageSlice(historyPage, historyPageSize)

                    pageLogs.forEachIndexed { index, log ->
                        if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Icon(
                                careIcon(log.type),
                                contentDescription = null,
                                tint = careAccent(log.type),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "${log.type.label} · ${log.type.verb}",
                                        fontSize = 13.sp, color = InkDark
                                    )
                                    if (log.amount.isNotBlank()) {
                                        Spacer(Modifier.width(6.dp))
                                        CareAmountChip(log.amount, log.type)
                                    }
                                }
                                if (log.note.isNotBlank()) {
                                    Text(
                                        log.note,
                                        fontSize = 11.sp,
                                        color = InkSecondary,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            log.photoPath?.let { path ->
                                Spacer(Modifier.width(8.dp))
                                CareLogThumb(
                                    path = path,
                                    modifier = Modifier.clickable { viewerPath = path }
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(log.timeText, fontSize = 11.sp, color = InkSecondary)
                        }
                    }

                    PagerBar(
                        page = historyPage.coerceIn(0, historyTotal - 1),
                        totalPages = historyTotal,
                        onPage = { historyPage = it }
                    )
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
        // 养护历史照片放大：压在页面之上的覆盖层
        PhotoOverlay(path = viewerPath, onDismiss = { viewerPath = null })
    }

    if (showEdit) {
        EditPlantDialog(
            plant = plant,
            onDismiss = { showEdit = false },
            onSave = { updated ->
                vm.updatePlant(updated)
                showEdit = false
                Toast.makeText(context, "已保存 ✏️", Toast.LENGTH_SHORT).show()
            }
        )
    }

    pendingCare?.let { type ->
        CareLogDialog(
            plantName = plant.displayName,
            type = type,
            onDismiss = { pendingCare = null },
            onConfirm = { amount, photoPath, note ->
                vm.care(plantId, type, amount, photoPath, note)
                pendingCare = null
                Toast.makeText(context, "已记录${type.label}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // 更换植物图片
    if (showPhotoMenu) {
        GardenDialog(
            onDismiss = { showPhotoMenu = false },
            title = "更换植物图片",
            icon = GardenIcons.Camera,
            confirm = DialogAction(label = "取消", onClick = { showPhotoMenu = false }),
            dismiss = null
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "选一张照片作为这盆植物的封面，不选则使用品种默认图。",
                    fontSize = 12.sp,
                    color = InkSecondary
                )
                PhotoMenuRow("📷", "拍照") {
                    val dir = java.io.File(context.filesDir, "plant_photos").apply { mkdirs() }
                    val file = java.io.File(dir, "plant_${System.currentTimeMillis()}.jpg")
                    pendingCameraPath = file.absolutePath
                    val uri = runCatching {
                        androidx.core.content.FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            file
                        )
                    }.getOrNull()
                    if (uri != null) {
                        takePhoto.launch(uri)
                        showPhotoMenu = false
                    }
                }
                PhotoMenuRow("🖼️", "从相册选择") {
                    pickPhoto.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                    showPhotoMenu = false
                }
                if (plant.photoPath != null) {
                    PhotoMenuRow("🌱", "恢复默认图", tint = MintDeep) {
                        applyPhoto(null)
                        showPhotoMenu = false
                    }
                }
            }
        }
    }

    // 养护历史照片放大已在上方覆盖层中处理

    if (showDelete) {
        GardenConfirmDialog(
            onDismiss = { showDelete = false },
            title = "删除植物",
            icon = GardenIcons.Trash,
            confirmLabel = "移入回收站",
            onConfirm = {
                showDelete = false
                vm.deletePlant(plantId)
                onBack()
            },
            message = "确定把「${plant.displayName}」移出花园吗？它会先进入植物回收站，30 天内可以随时恢复。",
            destructive = true
        )
    }
}

/** 更换图片弹窗里的一行选项 */
@Composable
private fun PhotoMenuRow(
    emoji: String,
    label: String,
    tint: Color = InkDark,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        Text(emoji, fontSize = 16.sp)
        Spacer(Modifier.width(10.dp))
        Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = tint)
        Spacer(Modifier.weight(1f))
        Icon(GardenIcons.ChevronRight, null, tint = InkSecondary, modifier = Modifier.size(15.dp))
    }
}

/**
 * 植物档案右侧的习性小卡：固定宽度，绝不撑出档案区。
 * 只取品种描述里的"养护习性"部分（浇水/施肥频率已在左侧展示），配一枚叶片 logo。
 */
@Composable
private fun HabitCard(species: PlantSpecies?) {
    val habit = remember(species) {
        val raw = species?.description.orEmpty()
        raw.substringBefore("参考浇水").trim().ifBlank {
            when (species?.category) {
                PlantCategory.SUCCULENT -> "耐旱怕涝，盆土干透再浇透，喜光但夏季需遮阴通风。"
                PlantCategory.CACTUS -> "极耐旱，宁干勿湿，全日照养护，冬季断水更安全。"
                PlantCategory.FLOWERING, PlantCategory.BULB -> "喜光喜肥，花期保持土壤微润，注意通风防落蕾。"
                PlantCategory.HERB -> "喜光通风，土壤微湿即可，勤修剪能促分枝。"
                PlantCategory.BONSAI -> "喜充足光照，薄肥勤施，定期修剪保持树形。"
                PlantCategory.AQUATIC -> "喜静水或湿润基质，避免暴晒，保持水质清洁。"
                PlantCategory.FOLIAGE, null -> "喜温暖湿润与明亮散射光，忌暴晒和闷热，保持通风。"
            }
        }
    }
    Column(
        modifier = Modifier
            .width(118.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MintContainer.copy(alpha = 0.6f))
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(18.dp)
                    .background(MintDeep.copy(alpha = 0.15f), CircleShape)
            ) {
                Icon(GardenIcons.Leaf, null, tint = MintDeep, modifier = Modifier.size(11.dp))
            }
            Spacer(Modifier.width(5.dp))
            Text("习性", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MintDeep)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            habit,
            fontSize = 10.sp,
            lineHeight = 15.sp,
            color = InkSecondary,
            maxLines = 8,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** 打卡区里的一格：圆形图标 + 文案 */
@Composable
private fun CareAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(tint.copy(alpha = 0.14f))
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(21.dp))
        }
        Spacer(Modifier.height(5.dp))
        Text(label, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = InkDark)
    }
}

private fun toast(context: android.content.Context, msg: String) {
    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
}

@Composable
private fun CareStatusRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    nextDate: LocalDate,
    days: Int,
    lastText: String,
    tint: Color
) {
    val pillBg = when {
        days < 0 -> PinkContainer
        days == 0 -> AmberContainer
        else -> MintContainer
    }
    val pillFg = when {
        days < 0 -> ErrorRed
        days == 0 -> BadgeYellowFg
        else -> MintDeep
    }
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .background(tint.copy(alpha = 0.15f), CircleShape)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = InkDark)
            Text(lastText, fontSize = 11.sp, color = InkSecondary)
        }
        Surface(shape = RoundedCornerShape(9.dp), color = pillBg) {
            Text(
                formatDate(nextDate),
                fontSize = 11.sp, fontWeight = FontWeight.Bold, color = pillFg,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, fontSize = 12.sp, color = InkSecondary, modifier = Modifier.width(72.dp))
        Text(value, fontSize = 12.sp, color = InkDark, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun EditPlantDialog(plant: MyPlant, onDismiss: () -> Unit, onSave: (MyPlant) -> Unit) {
    var nickname by remember { mutableStateOf(plant.nickname) }
    var waterDays by remember { mutableStateOf(plant.waterDays.toString()) }
    var fertDays by remember { mutableStateOf(plant.fertilizeDays.toString()) }
    var note by remember { mutableStateOf(plant.note) }

    GardenDialog(
        onDismiss = onDismiss,
        title = "编辑植物",
        icon = GardenIcons.Pencil,
        confirm = DialogAction(
            label = "保存",
            onClick = {
                onSave(
                    plant.copy(
                        nickname = nickname.trim(),
                        waterDays = (waterDays.toIntOrNull() ?: plant.waterDays).coerceIn(1, 365),
                        fertilizeDays = (fertDays.toIntOrNull() ?: plant.fertilizeDays).coerceIn(1, 365),
                        note = note.trim()
                    )
                )
            }
        ),
        dismiss = DialogAction(label = "取消", onClick = onDismiss)
    ) {
        Column(
            modifier = Modifier.heightIn(max = 420.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it.take(20) },
                label = { Text("昵称") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintDeep, focusedLabelColor = MintDeep),
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = waterDays,
                    onValueChange = { waterDays = it.filter(Char::isDigit).take(3) },
                    label = { Text("浇水间隔(天)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintDeep, focusedLabelColor = MintDeep),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = fertDays,
                    onValueChange = { fertDays = it.filter(Char::isDigit).take(3) },
                    label = { Text("施肥间隔(天)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintDeep, focusedLabelColor = MintDeep),
                    modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(200) },
                label = { Text("备注") },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MintDeep, focusedLabelColor = MintDeep),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}