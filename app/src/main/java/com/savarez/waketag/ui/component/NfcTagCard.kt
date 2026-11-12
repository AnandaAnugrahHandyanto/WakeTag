package com.savarez.waketag.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Kunci Tag NFC",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = tagId?.let { "Tag terdaftar: $it" } ?: "Belum ada tag terdaftar",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Alarm dengan metode Tag NFC hanya bisa dimatikan dengan tag ini.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            errorMessage?.let { message ->
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                    Text(text = if (tagId == null) "Daftarkan Tag" else "Ganti Tag")
                }

                if (tagId != null) {
                    TextButton(
                        onClick = {
                            haptic()
                            NfcTagStore.clear(context)
                            tagId = null
                            errorMessage = null
                        }
                    ) {
                        Text(text = "Hapus Tag")
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

@Preview(showBackground = true)
@Composable
private fun NfcTagCardPreview() {
    WakeTagTheme {
        NfcTagCard()
    }
}
