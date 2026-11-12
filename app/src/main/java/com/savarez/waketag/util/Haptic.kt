package com.savarez.waketag.util

import android.view.HapticFeedbackConstants
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView

/**
 * Memberi getaran singkat saat tombol ditekan.
 *
 * Cara pakai:
 *   val haptic = rememberHapticFeedback()
 *   Button(onClick = { haptic(); lakukanSesuatu() })
 */
@Composable
fun rememberHapticFeedback(): () -> Unit {
    val view: View = LocalView.current
    return remember(view) {
        { view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY) }
    }
}
