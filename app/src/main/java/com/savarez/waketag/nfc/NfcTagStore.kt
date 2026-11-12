package com.savarez.waketag.nfc

import android.content.Context

/**
 * Menyimpan UID satu tag NFC yang sudah didaftarkan.
 *
 * Data disimpan di SharedPreferences supaya sederhana dan tidak perlu
 * mengubah struktur database alarm.
 */
object NfcTagStore {

    private const val PREFS_NAME = "waketag_nfc"
    private const val KEY_TAG_ID = "registered_tag_id"

    fun getTagId(context: Context): String? {
        return prefs(context).getString(KEY_TAG_ID, null)
    }

    fun saveTagId(context: Context, tagId: String) {
        prefs(context).edit().putString(KEY_TAG_ID, tagId).apply()
    }

    fun clear(context: Context) {
        prefs(context).edit().remove(KEY_TAG_ID).apply()
    }

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
