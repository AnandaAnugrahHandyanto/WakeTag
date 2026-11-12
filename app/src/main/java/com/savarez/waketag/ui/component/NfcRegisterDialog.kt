package com.savarez.waketag.ui.component

import android.app.Activity
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.savarez.waketag.nfc.NfcManager
import com.savarez.waketag.nfc.NfcTagStore

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
        title = { Text(text = "Daftarkan Tag NFC") },
        text = {
            Text(
                text = "Tempelkan tag NFC ke bagian belakang HP. " +
                    "Tag akan langsung terdaftar setelah terbaca."
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Batal")
            }
        }
    )
}
