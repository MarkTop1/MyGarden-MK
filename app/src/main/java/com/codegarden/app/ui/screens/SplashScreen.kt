package com.codegarden.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codegarden.app.R
import com.codegarden.app.ui.components.FloatingLeaves
import com.codegarden.app.ui.theme.GlowMint
import com.codegarden.app.ui.theme.InkSecondary
import com.codegarden.app.ui.theme.WarmBg
import com.codegarden.app.ui.theme.WarmBgBottom
import kotlinx.coroutines.delay

/** 启动页：品牌 logo + Slogan，带叶子飘动背景，短暂停留后进入主页面 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1700)
        onFinished()
    }

    val scale = remember { Animatable(0.6f) }
    LaunchedEffect(Unit) {
        scale.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // 背景层：渐变 + 光晕 + 飘叶
        Box(modifier = Modifier.fillMaxSize()) {
            val bgTop = WarmBg
            val bgBottom = WarmBgBottom
            val glowMint = GlowMint
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                // 主背景渐变
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(bgTop, bgBottom)
                    )
                )
                // 中央光晕
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            glowMint.copy(alpha = 0.45f),
                            Color.Transparent
                        ),
                        center = Offset(w / 2, h * 0.4f),
                        radius = minOf(w, h) * 0.45f
                    ),
                    center = Offset(w / 2, h * 0.4f),
                    radius = minOf(w, h) * 0.45f
                )
            }
            FloatingLeaves()
        }

        // 内容层
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(R.drawable.logo_botanical),
                contentDescription = "我的花园",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(132.dp)
                    .scale(scale.value)
                    .clip(CircleShape)
            )
            Spacer(Modifier.height(22.dp))
            Text(
                text = "我的花园",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "一草一木，慢慢照料。",
                fontSize = 14.sp,
                color = InkSecondary
            )
        }

        Text(
            text = "离线单机 · 治愈系植物养护",
            fontSize = 11.sp,
            color = InkSecondary,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        )
    }
}
