package com.savarez.waketag.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Jam melingkar seperti orbit matahari. Busur menunjukkan seberapa dekat
 * alarm berikutnya, dan titik terang menandai posisinya.
 */
@Composable
fun SunriseDial(
    progress: Float,
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    centerLabel: String? = null,
    centerCaption: String? = null,
    showTicks: Boolean = true,
    trackWidth: Dp = 5.dp
) {
    val clamped = progress.coerceIn(0f, 1f)
    val sweep = 360f * clamped
    val startAngle = -90f

    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = trackWidth.toPx()
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f - stroke - 3.dp.toPx()
            val arcTopLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2f, radius * 2f)

            if (showTicks) {
                repeat(12) { index ->
                    val angle = (index * 30f - 90f) * PI.toFloat() / 180f
                    val inner = radius - 10.dp.toPx()
                    val outer = radius - 5.dp.toPx()
                    drawLine(
                        color = accent.copy(alpha = if (index % 3 == 0) 0.34f else 0.14f),
                        start = Offset(
                            center.x + cos(angle) * inner,
                            center.y + sin(angle) * inner
                        ),
                        end = Offset(
                            center.x + cos(angle) * outer,
                            center.y + sin(angle) * outer
                        ),
                        strokeWidth = 1.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            drawArc(
                color = accent.copy(alpha = 0.16f),
                startAngle = startAngle,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = arcTopLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )

            if (sweep > 0.6f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            accent.copy(alpha = 0.35f),
                            accent,
                            accent.copy(alpha = 0.55f)
                        ),
                        center = center
                    ),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = arcTopLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }

            val dotAngle = (startAngle + sweep) * PI.toFloat() / 180f
            val dotCenter = Offset(
                center.x + cos(dotAngle) * radius,
                center.y + sin(dotAngle) * radius
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.55f), Color.Transparent),
                    center = dotCenter,
                    radius = 14.dp.toPx()
                ),
                radius = 14.dp.toPx(),
                center = dotCenter
            )
            drawCircle(
                color = accent,
                radius = 4.dp.toPx(),
                center = dotCenter
            )
        }

        if (centerLabel != null || centerCaption != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                centerCaption?.let { caption ->
                    Text(
                        text = caption.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = contentColor.copy(alpha = 0.55f),
                        letterSpacing = 1.6.sp,
                        textAlign = TextAlign.Center
                    )
                }
                centerLabel?.let { label ->
                    Text(
                        text = label.uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor,
                        letterSpacing = 0.8.sp,
                        textAlign = TextAlign.Center,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
