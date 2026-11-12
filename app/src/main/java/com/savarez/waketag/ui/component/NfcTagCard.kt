package com.savarez.waketag.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.savarez.waketag.nfc.NfcManager
import com.savarez.waketag.nfc.NfcTagStore
import com.savarez.waketag.ui.theme.WakeTagTheme
import com.savarez.waketag.util.rememberHapticFeedback

/**
 * Kartu di halaman utama untuk melihat, mendaftarkan, dan menghapus tag NFC.
 */
@Composable
fun NfcTagCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val nfcManager = remember { NfcManager(context) }
    var tagId by remember { mutableStateOf(NfcTagStore.getTagId(context)) }
    var showRegisterDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val haptic = rememberHapticFeedback()
    val isRegistered = tagId != null

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "KUNCI FISIK",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "Tag NFC",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                StatusPill(registered = isRegistered)
            }

            if (tagId != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "UID TERDAFTAR",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.4.sp
                        )
                        Text(
                            text = tagId.orEmpty(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                Text(
                    text = "Belum ada tag terdaftar. Daftarkan satu tag agar alarm " +
                        "Tag NFC bisa dimatikan.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        haptic()
                        errorMessage = when {
                            !nfcManager.isSupported -> "HP ini tidak mendukung NFC."
                            !nfcManager.isEnabled -> {
                                nfcManager.openNfcSettings()
                                "NFC belum aktif. Aktifkan dulu, lalu coba lagi."
                            }
                            else -> {
                                showRegisterDialog = true
                                null
                            }
                        }
                    }
                ) {
                    Text(text = if (isRegistered) "Ganti Tag" else "Daftarkan Tag")
                }

                if (isRegistered) {
                    TextButton(
                        onClick = {
                            haptic()
                            NfcTagStore.clear(context)
                            tagId = null
                            errorMessage = null
                        }
                    ) {
                        Text(text = "Lepas")
                    }
                }
            }
        }
    }

    if (showRegisterDialog) {
        NfcRegisterDialog(
            onDismiss = { showRegisterDialog = false },
            onRegistered = { newTagId ->
                tagId = newTagId
                errorMessage = null
                showRegisterDialog = false
            }
        )
    }
}

@Composable
private fun StatusPill(registered: Boolean) {
    Surface(
        shape = CircleShape,
        color = if (registered) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(
                        color = if (registered) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline
                        },
                        shape = CircleShape
                    )
            )
            Text(
                text = if (registered) "Terdaftar" else "Belum ada",
                style = MaterialTheme.typography.labelSmall,
                color = if (registered) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NfcTagCardPreview() {
    WakeTagTheme {
        NfcTagCard()
    }
}
