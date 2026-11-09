package com.codegarden.app.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.codegarden.app.data.local.PhotoStore
import com.codegarden.app.ui.theme.InkSecondary
import com.codegarden.app.ui.theme.MintDeep
import java.io.File

/** 照片选择卡片：拍照 / 相册二选一，存档到应用内部存储。识别能力预留，当前为拍照存档 + 手动确认品种 */
@Composable
fun PhotoPickerCard(photoPath: String?, emoji: String, onPhoto: (String?) -> Unit) {
    val context = LocalContext.current
    var pendingCameraPath by rememberSaveable { mutableStateOf<String?>(null) }

    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val path = pendingCameraPath
        if (success && path != null && File(path).length() > 0) {
            onPhoto(path)
        } else if (path != null) {
            runCatching { File(path).delete() }
        }
        pendingCameraPath = null
    }

    val pickMedia = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) {
            PhotoStore.savePhoto(context, uri)?.let(onPhoto)
        }
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("📷 植物照片", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("拍一张或选一张，记录它现在的样子", fontSize = 11.sp, color = InkSecondary)
            Spacer(Modifier.height(12.dp))

            if (photoPath != null) {
                Box {
                    PlantPhoto(
                        path = photoPath,
                        emoji = emoji,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
                    ) {
                        TextButton(onClick = {
                            PhotoStore.deletePhoto(photoPath)
                            onPhoto(null)
                        }) {
                            Text("移除", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = {
                        val dir = File(context.filesDir, "plant_photos").apply { mkdirs() }
                        val file = File(dir, "cam_${System.currentTimeMillis()}.jpg")
                        pendingCameraPath = file.absolutePath
                        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                        takePicture.launch(uri)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("📷 拍照", fontSize = 13.sp)
                }
                OutlinedButton(
                    onClick = {
                        pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("🖼️ 从相册选择", fontSize = 13.sp)
                }
            }

            Spacer(Modifier.height(10.dp))
            Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("✨", fontSize = 14.sp)
                    Spacer(Modifier.size(8.dp))
                    Text(
                        "拍照识别品种 · 敬请期待（当前请手动确认品种）",
                        fontSize = 11.sp,
                        color = InkSecondary
                    )
                }
            }
        }
    }
}