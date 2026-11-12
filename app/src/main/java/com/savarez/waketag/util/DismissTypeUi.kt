package com.savarez.waketag.util

import com.savarez.waketag.data.model.DismissType

val DismissType.displayLabel: String
    get() = when (this) {
        DismissType.NORMAL -> "Normal"
        DismissType.NFC -> "Tag NFC"
    }

val DismissType.dismissHint: String
    get() = when (this) {
        DismissType.NORMAL -> "Matikan langsung"
        DismissType.NFC -> "Harus ketuk tag NFC"
    }
