package com.codegarden.app.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codegarden.app.ui.theme.MintContainer
import com.codegarden.app.ui.theme.MintDeep
import com.codegarden.app.ui.theme.WarmBg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/** 植物照片：异步加载本地存档照片（自动降采样防 OOM），无照片时回退品种插画 / emoji */
@Composable
fun PlantPhoto(
    path: String?,
    emoji: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    fallbackSize: TextUnit = 40.sp,
    speciesId: Int? = null,
    containerColor: Color? = null
) {
    var bitmap by remember(path) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(path) {
        bitmap = if (path.isNullOrBlank()) null else withContext(Dispatchers.IO) {
            runCatching { decodeSampled(path, 1024) }.getOrNull()
        }
    }
    Box(
        modifier = modifier.background(
            containerColor ?: if (bitmap == null && speciesId != null) WarmBg else MintContainer
        ),
        contentAlignment = Alignment.Center
    ) {
        val img = bitmap
        if (img != null) {
            Image(img, contentDescription = "植物照片", modifier = Modifier.fillMaxSize(), contentScale = contentScale)
        } else if (speciesId != null) {
            SpeciesImage(speciesId, emoji, Modifier.fillMaxSize(), fallbackSize = fallbackSize)
        } else {
            Icon(
                GardenGlyphs.icon(emoji),
                contentDescription = null,
                tint = MintDeep,
                modifier = Modifier.size(fallbackSize.value.dp)
            )
        }
    }
}

private fun decodeSampled(path: String, reqSize: Int): ImageBitmap? {
    val file = File(path)
    if (!file.exists()) return null
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    var sample = 1
    while (bounds.outWidth / sample > reqSize * 2 || bounds.outHeight / sample > reqSize * 2) {
        sample *= 2
    }
    val opts = BitmapFactory.Options().apply { inSampleSize = sample }
    return BitmapFactory.decodeFile(path, opts)?.asImageBitmap()
}