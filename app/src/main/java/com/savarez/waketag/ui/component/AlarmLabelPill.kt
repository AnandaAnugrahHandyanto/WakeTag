package com.savarez.waketag.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.savarez.waketag.ui.theme.WakeTagTheme

/**
 * Label kecil untuk menandai tujuan alarm, misalnya KULIAH, KERJA, atau PRIBADI.
 * Warnanya dibedakan per kategori supaya daftar alarm mudah dipindai.
 */
@Composable
fun AlarmLabelPill(
    label: String,
    modifier: Modifier = Modifier,
    prominent: Boolean = false
) {
    val text = label.trim()
    if (text.isEmpty()) return

    val accent = alarmLabelAccent(text)
    val background = accent.copy(alpha = 0.16f)

    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = background
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (prominent) 12.dp else 10.dp,
                vertical = if (prominent) 6.dp else 4.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(if (prominent) 7.dp else 5.dp)
                    .background(color = accent, shape = CircleShape)
            )
            Text(
                text = text.uppercase(),
                style = if (prominent) {
                    MaterialTheme.typography.labelMedium
                } else {
                    MaterialTheme.typography.labelSmall
                },
                color = accent,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = if (prominent) 1.4.sp else 1.sp
            )
        }
    }
}

/**
 * Warna aksen kategori. Pemetaan sederhana agar hasilnya konsisten tanpa
 * menambah kolom warna di database.
 */
@Composable
fun alarmLabelAccent(label: String): Color {
    val normalized = label.trim().lowercase()
    return when {
        normalized.contains("kuliah") || normalized.contains("sekolah") ->
            MaterialTheme.colorScheme.tertiary

        normalized.contains("kerja") || normalized.contains("kantor") ->
            MaterialTheme.colorScheme.primary

        normalized.contains("pribadi") || normalized.contains("pribad") ->
            MaterialTheme.colorScheme.secondary

        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Preview(showBackground = true)
@Composable
private fun AlarmLabelPillPreview() {
    WakeTagTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AlarmLabelPill(label = "Kuliah")
            AlarmLabelPill(label = "Kerja")
            AlarmLabelPill(label = "Pribadi")
            AlarmLabelPill(label = "Olahraga", prominent = true)
        }
    }
}
