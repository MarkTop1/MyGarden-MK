package com.codegarden.app.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codegarden.app.ui.theme.MintDeep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** 植物水彩插画：按品种 id 加载内置插画（透明底），无对应图时回退 emoji */
@Composable
fun SpeciesImage(
    speciesId: Int?,
    emoji: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    fallbackSize: TextUnit = 40.sp,
    alignment: Alignment = Alignment.Center
) {
    val context = LocalContext.current
    var bitmap by remember(speciesId) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(speciesId) {
        bitmap = if (speciesId == null) null else withContext(Dispatchers.IO) {
            SpeciesImages.load(context, speciesId)
        }
    }
    Box(modifier = modifier, contentAlignment = alignment) {
        val img = bitmap
        if (img != null) {
            Image(img, contentDescription = "植物插画", modifier = Modifier.fillMaxSize(), contentScale = contentScale, alignment = alignment)
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

private object SpeciesImages {
    private var mapping: Map<String, String>? = null
    private val cache = object : LinkedHashMap<String, ImageBitmap>(32, 0.75f, true) {
        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, ImageBitmap>): Boolean = size > 40
    }

    fun load(context: Context, speciesId: Int): ImageBitmap? {
        val key = mapping(context)[speciesId.toString()] ?: return null
        synchronized(cache) {
            cache[key]?.let { return it }
        }
        val bmp = runCatching {
            context.assets.open("plant_images/$key.webp").use { BitmapFactory.decodeStream(it) }
        }.getOrNull()?.asImageBitmap() ?: return null
        synchronized(cache) { cache[key] = bmp }
        return bmp
    }

    private fun mapping(context: Context): Map<String, String> {
        mapping?.let { return it }
        val parsed = runCatching {
            val text = context.assets.open("plant_images_map.json").use { it.readBytes().toString(Charsets.UTF_8) }
            val obj = JSONObject(text)
            obj.keys().asSequence().associateWith { obj.getString(it) }
        }.getOrDefault(emptyMap())
        mapping = parsed
        return parsed
    }
}