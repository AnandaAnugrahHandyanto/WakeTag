package com.savarez.waketag.util

import com.savarez.waketag.data.model.DismissType

val DismissType.displayLabel: String
    get() = when (this) {
        DismissType.NORMAL -> "Normal"
        DismissType.NFC -> "NFC"
        DismissType.QR -> "QR"
        DismissType.PHOTO -> "Photo"
    }

val DismissType.futureCapabilityHint: String
    get() = when (this) {
        DismissType.NORMAL -> "Dismiss directly"
        DismissType.NFC -> "NFC unlock (coming soon)"
        DismissType.QR -> "QR unlock (coming soon)"
        DismissType.PHOTO -> "Photo unlock (coming soon)"
    }
