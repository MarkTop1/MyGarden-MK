package com.codegarden.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.codegarden.app.ui.theme.InkDark
import com.codegarden.app.ui.theme.MintLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** 应用内提示条总线，替代系统 Toast */
object GardenToast {
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()
    fun show(text: String) {
        _message.value = text
    }
    internal fun clear() {
        _message.value = null
    }
}

@Composable
fun GardenToastHost(modifier: Modifier = Modifier) {
    val msg by GardenToast.message.collectAsStateWithLifecycle()
    LaunchedEffect(msg) {
        if (msg != null) {
            delay(1800)
            GardenToast.clear()
        }
    }
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
        AnimatedVisibility(
            visible = msg != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(bottom = 112.dp, start = 32.dp, end = 32.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(InkDark.copy(alpha = 0.94f))
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Icon(GardenIcons.Sprout, null, tint = MintLight, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(8.dp))
                Text(msg.orEmpty(), fontSize = 13.sp, color = Color.White)
            }
        }
    }
}