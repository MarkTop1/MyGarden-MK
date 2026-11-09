package com.codegarden.app.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.codegarden.app.data.local.PhotoStore
import com.codegarden.app.data.model.CareType
import com.codegarden.app.ui.theme.DropBlue
import com.codegarden.app.ui.theme.ErrorRed
import com.codegarden.app.ui.theme.FertilizeGreen
import com.codegarden.app.ui.theme.InkDark
import com.codegarden.app.ui.theme.InkSecondary
import com.codegarden.app.ui.theme.MintPrimary
import com.codegarden.app.ui.theme.SoilBrown
import java.io.File

/** 各养护动作的主题色 */
@Composable
fun careAccent(type: CareType): Color = when (type) {
    CareType.WATER -> DropBlue
    CareType.FERTILIZE -> FertilizeGreen
    CareType.PEST -> SoilBrown
    CareType.PRUNE -> MintPrimary
}

/** 各养护动作的图标 */
fun careGlyph(type: CareType) = when (type) {
    CareType.WATER -> GardenIcons.Drop
    CareType.FERTILIZE -> GardenIcons.Grain
    CareType.PEST -> GardenIcons.Bug
    CareType.PRUNE -> GardenIcons.Scissors
}

/** 只有浇水和施肥才有「用量」这一项 */
private fun hasAmount(type: CareType) =
    type == CareType.WATER || type == CareType.FERTILIZE

private fun amountTitle(type: CareType) = when (type) {
    CareType.WATER -> "浇水量"
    CareType.FERTILIZE -> "施肥量"
    else -> "用量"
}

private fun amountPresets(type: CareType) = when (type) {
    CareType.WATER -> listOf("100ml", "200ml", "500ml", "适量")
    CareType.FERTILIZE -> listOf("少量", "适量", "1 小勺", "1 瓶盖")
    else -> emptyList()
}

private fun amountHint(type: CareType) = when (type) {
    CareType.WATER -> "也可以自己填，如 350ml"
    CareType.FERTILIZE -> "也可以自己填，如 2 克"
    else -> ""
}

/**
 * 养护打卡二次确认弹窗。
 * 浇水 / 施肥可填用量，四种动作都可附当日照片与备注。
 */
@Composable
fun CareLogDialog(
    plantName: String,
    type: CareType,
    onDismiss: () -> Unit,
    onConfirm: (amount: String, photoPath: String?, note: String) -> Unit
) {
    val context = LocalContext.current
    var amount by rememberSaveable { mutableStateOf("") }
    var note by rememberSaveable { mutableStateOf("") }
    var photoPath by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCameraPath by rememberSaveable { mutableStateOf<String?>(null) }

    val accent = careAccent(type)

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val path = pendingCameraPath
        if (ok && path != null && File(path).length() > 0) {
            photoPath = path
        } else if (path != null) {
            runCatching { File(path).delete() }
        }
        pendingCameraPath = null
    }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) PhotoStore.savePhoto(context, uri)?.let { photoPath = it }
    }

    // 取消 / 点外部关闭时，清掉本次刚拍下但未确认的照片，避免残留垃圾文件
    fun cancel() {
        PhotoStore.deletePhoto(photoPath)
        onDismiss()
    }

    GardenDialog(
        onDismiss = { cancel() },
        title = "${type.emoji} 记录${type.label}",
        icon = careGlyph(type),
        confirm = DialogAction(
            label = "确认打卡",
            onClick = { onConfirm(amount.trim(), photoPath, note.trim()) }
        ),
        dismiss = DialogAction(label = "取消", onClick = { cancel() })
    ) {
        Column(
            modifier = Modifier
                .heightIn(max = 430.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "给「$plantName」记下今天的一次${type.label}",
                fontSize = 12.sp,
                color = InkSecondary
            )

            // 用量（仅浇水 / 施肥）
            if (hasAmount(type)) {
                Column {
                    Text(amountTitle(type), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = InkDark)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    ) {
                        amountPresets(type).forEach { preset ->
                            val selected = amount == preset
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (selected) accent.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (selected) BorderStroke(1.dp, accent) else null,
                                modifier = Modifier.clickable {
                                    amount = if (selected) "" else preset
                                }
                            ) {
                                Text(
                                    preset,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selected) accent else InkSecondary,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it.take(24) },
                        placeholder = { Text(amountHint(type), fontSize = 12.sp, color = InkSecondary) },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accent,
                            focusedLabelColor = accent,
                            cursorColor = accent
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 当日照片
            Column {
                Text("当日照片", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = InkDark)
                Spacer(Modifier.height(8.dp))
                val path = photoPath
                if (path != null) {
                    Box {
                        PlantPhoto(
                            path = path,
                            emoji = "",
                            fallbackSize = 40.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                        ) {
                            Text(
                                "移除",
                                fontSize = 12.sp,
                                color = ErrorRed,
                                modifier = Modifier
                                    .clickable {
                                        PhotoStore.deletePhoto(path)
                                        photoPath = null
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = {
                                val dir = File(context.filesDir, "plant_photos").apply { mkdirs() }
                                val file = File(dir, "care_${System.currentTimeMillis()}.jpg")
                                pendingCameraPath = file.absolutePath
                                val uri = runCatching {
                                    FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                }.getOrNull()
                                if (uri != null) takePicture.launch(uri)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📷 拍照", fontSize = 13.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                pickMedia.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🖼️ 相册", fontSize = 13.sp)
                        }
                    }
                }
            }

            // 备注
            Column {
                Text("备注", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = InkDark)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it.take(200) },
                    placeholder = {
                        Text(
                            when (type) {
                                CareType.WATER -> "土壤湿度、是否浇透…"
                                CareType.FERTILIZE -> "用的什么肥、植物反应…"
                                CareType.PEST -> "虫害类型、用药方式…"
                                CareType.PRUNE -> "剪了哪里、剪后状态…"
                            },
                            fontSize = 12.sp,
                            color = InkSecondary
                        )
                    },
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = accent,
                        focusedLabelColor = accent,
                        cursorColor = accent
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/** 历史记录里展示用量 / 照片用的小徽标 */
@Composable
fun CareAmountChip(text: String, type: CareType) {
    val accent = careAccent(type)
    Surface(shape = RoundedCornerShape(7.dp), color = accent.copy(alpha = 0.13f)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(accent, CircleShape)
            )
            Spacer(Modifier.width(4.dp))
            Text(text, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = accent)
        }
    }
}

/** 养护历史条目里的照片缩略图 */
@Composable
fun CareLogThumb(path: String, modifier: Modifier = Modifier) {
    PlantPhoto(
        path = path,
        emoji = "",
        fallbackSize = 20.sp,
        modifier = modifier
            .size(46.dp)
            .clip(RoundedCornerShape(10.dp))
    )
}
