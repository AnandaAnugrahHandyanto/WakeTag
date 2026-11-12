package com.savarez.waketag.nfc

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.provider.Settings

/**
 * Pembungkus kecil untuk membaca tag NFC.
 *
 * Mode yang dipakai adalah "reader mode", yaitu cara Android membaca tag
 * tanpa harus membuat aplikasi menangani intent NFC secara manual.
 */
class NfcManager(context: Context) {

    private val appContext = context.applicationContext
    private val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(appContext)

    val isSupported: Boolean
        get() = nfcAdapter != null

    val isEnabled: Boolean
        get() = nfcAdapter?.isEnabled == true

    fun enableReaderMode(activity: Activity, onTagDiscovered: (String) -> Unit) {
        val adapter = nfcAdapter ?: return
        adapter.enableReaderMode(
            activity,
            { tag -> onTagDiscovered(formatTagId(tag.id)) },
            READER_FLAGS,
            null
        )
    }

    fun disableReaderMode(activity: Activity) {
        nfcAdapter?.disableReaderMode(activity)
    }

    fun openNfcSettings() {
        val intent = Intent(Settings.ACTION_NFC_SETTINGS)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        appContext.startActivity(intent)
    }

    companion object {
        private const val READER_FLAGS =
            NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NFC_F or
                NfcAdapter.FLAG_READER_NFC_V or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

        /**
         * Mengubah UID tag dari bentuk byte menjadi teks hex,
         * contoh: "04A1B2C3D4E5F6".
         */
        fun formatTagId(bytes: ByteArray): String {
            return bytes.joinToString(separator = "") { byte ->
                "%02X".format(byte.toInt() and 0xFF)
            }
        }
    }
}
