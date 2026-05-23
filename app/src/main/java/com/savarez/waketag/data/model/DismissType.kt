package com.savarez.waketag.data.model

enum class DismissType(
    val displayName: String
) {
    NORMAL("Normal"),
    NFC("NFC Tag"),
    QR("QR Code"),
    PHOTO("Photo Challenge")
}
