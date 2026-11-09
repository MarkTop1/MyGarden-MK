package com.codegarden.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.codegarden.app.ui.theme.MintContainer
import com.codegarden.app.ui.theme.MintDeep

/** 圆形头像：统一的植物系线描图标（键位或旧 emoji 均可） */
@Composable
fun Avatar(emoji: String, size: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.size(size).background(MintContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            GardenGlyphs.icon(GardenGlyphs.avatarKey(emoji)),
            contentDescription = null,
            tint = MintDeep,
            modifier = Modifier.size(size * 0.5f)
        )
    }
}
