package com.savarez.waketag.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.savarez.waketag.ui.theme.WakeTagTheme

@Composable
fun HomeScreen(
    onCreateAlarmClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "WakeTag",
            style = MaterialTheme.typography.headlineLarge
        )

        Button(onClick = onCreateAlarmClick) {
            Text("Create Alarm")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    WakeTagTheme {
        HomeScreen(onCreateAlarmClick = {})
    }
}
