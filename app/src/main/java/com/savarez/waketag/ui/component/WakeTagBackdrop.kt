package com.savarez.waketag.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Latar suasana "midnight -> sunrise": gelap di atas, hangat di cakrawala.
 * Dipakai di halaman utama dan formulir alarm agar seluruh aplikasi terasa
 * seperti satu malam yang menuju pagi.
 */
@Composable
fun WakeTagBackdrop(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    val transition = rememberInfiniteTransition(label = "waketagBackdrop")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "backdropDrift"
    )

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        colors.background,
                        colors.background,
                        colors.surface.copy(alpha = 0.45f)
                    )
                )
            )

            // Cahaya amber di sudut kiri atas, bergerak sangat pelan.
            val amberCenter = Offset(
                x = size.width * (0.12f + 0.08f * drift),
                y = size.height * 0.08f
            )
            val amberRadius = size.minDimension * 0.74f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.primary.copy(alpha = 0.17f),
                        colors.primary.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = amberCenter,
                    radius = amberRadius
                ),
                radius = amberRadius,
                center = amberCenter
            )

            // Pantulan dingin di sudut kanan bawah sebagai penyeimbang.
            val coolCenter = Offset(
                x = size.width * (0.94f - 0.05f * drift),
                y = size.height * 0.82f
            )
            val coolRadius = size.minDimension * 0.62f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        colors.tertiary.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = coolCenter,
                    radius = coolRadius
                ),
                radius = coolRadius,
                center = coolCenter
            )

            // Garis cakrawala tipis, seperti kisi jendela di pagi hari.
            val horizon = size.height * 0.70f
            repeat(4) { index ->
                val y = horizon + index * 26.dp.toPx()
                drawLine(
                    color = colors.onBackground.copy(alpha = 0.028f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1f
                )
            }
        }
        content()
    }
}
