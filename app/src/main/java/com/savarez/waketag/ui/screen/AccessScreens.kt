package com.savarez.waketag.ui.screen

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.savarez.waketag.ui.component.WakeTagBackdrop
import com.savarez.waketag.util.rememberHapticFeedback

private val AccessCardShape = RoundedCornerShape(28.dp)

@Composable
fun WakeTagSplashScreen() {
    val transition = rememberInfiniteTransition(label = "splashOrb")
    val orbScale by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Reverse),
        label = "orbScale"
    )
    val orbAlpha by transition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(tween(1600), RepeatMode.Reverse),
        label = "orbAlpha"
    )

    WakeTagBackdrop {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(132.dp)
                        .scale(orbScale)
                        .alpha(orbAlpha)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.22f), CircleShape)
                )
                Surface(
                    modifier = Modifier.size(82.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    tonalElevation = 8.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "WT",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            Spacer(Modifier.height(26.dp))
            Text(
                text = "WAKE TAG",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                letterSpacing = 3.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Wake up. Move with purpose.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun WakeTagLoginScreen(onLogin: () -> Unit) {
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showError by rememberSaveable { mutableStateOf(false) }
    val haptic = rememberHapticFeedback()

    WakeTagBackdrop {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 22.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = "WAKE TAG",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.4.sp
                )
                Text(
                    text = "Mulai pagi dengan sengaja.",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Masuk untuk mengatur alarm dan ritual bangunmu.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(24.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = AccessCardShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; showError = false },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Nama pengguna") },
                        placeholder = { Text("Contoh: Ananda") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; showError = false },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Kata sandi") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    if (showError) {
                        Text(
                            text = "Isi nama pengguna dan kata sandi terlebih dahulu.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Button(
                        onClick = {
                            haptic()
                            if (username.isBlank() || password.isBlank()) {
                                showError = true
                            } else {
                                onLogin()
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("Masuk")
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Data WakeTag tetap tersimpan lokal di perangkat ini.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
