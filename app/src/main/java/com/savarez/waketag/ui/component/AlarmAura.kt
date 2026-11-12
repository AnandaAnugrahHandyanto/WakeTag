package com.savarez.waketag.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

/**
 * Aura lembut yang berdenyut di belakang jam. Dipakai sebagai fokus visual
 * pada panel alarm berikutnya dan layar saat alarm berbunyi.
 */
@Composable
fun AlarmAura(
    color: Color,
    modifier: Modifier = Modifier,
    pulse: Boolean = true,
    ringCount: Int = 3
) {
    val transition = rememberInfiniteTransition(label = "alarmAura")
    val pulseScale by transition.animateFloat(
        initialValue = if (pulse) 0.94f else 1f,
        targetValue = if (pulse) 1.06f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3_800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraScale"
    )
    val pulseAlpha by transition.animateFloat(
        initialValue = if (pulse) 0.78f else 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3_800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "auraAlpha"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxRadius = size.minDimension / 2f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = 0.26f * pulseAlpha),
                    color.copy(alpha = 0.08f * pulseAlpha),
                    Color.Transparent
                ),
                center = center,
                radius = maxRadius
            ),
            radius = maxRadius,
            center = center
        )

        repeat(ringCount) { index ->
            val ringRadius = maxRadius * (0.40f + index * 0.19f) * pulseScale
            val ringAlpha = (0.20f - index * 0.045f).coerceAtLeast(0.03f) * pulseAlpha
            drawCircle(
                color = color.copy(alpha = ringAlpha),
                radius = ringRadius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )
        }
    }
}
