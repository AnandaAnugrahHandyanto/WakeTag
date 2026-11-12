package com.savarez.waketag.ui.component

import android.app.Activity
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.savarez.waketag.nfc.NfcManager
import com.savarez.waketag.nfc.NfcTagStore
import com.savarez.waketag.util.rememberHapticFeedback

/**
 * Dialog untuk mendaftarkan satu tag NFC.
 * Selama dialog terbuka, HP dalam mode membaca tag.
 */
@Composable
fun NfcRegisterDialog(
    onDismiss: () -> Unit,
    onRegistered: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val nfcManager = remember { NfcManager(context) }
    val haptic = rememberHapticFeedback()

    DisposableEffect(activity) {
        if (activity != null) {
            nfcManager.enableReaderMode(activity) { tagId ->
                activity.runOnUiThread {
                    NfcTagStore.saveTagId(context, tagId)
                    onRegistered(tagId)
                }
            }
        }
        onDispose {
            if (activity != null) {
                nfcManager.disableReaderMode(activity)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Daftarkan tag NFC",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Satu tag untuk semua alarm NFC.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape
                                )
                        )
                        Text(
                            text = "Menunggu tag...",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Text(
                        text = "Tempelkan tag NFC ke bagian belakang HP. " +
                            "Tag akan langsung terdaftar setelah terbaca.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { haptic(); onDismiss() }) {
                Text(text = "Batal")
            }
        }
    )
}
