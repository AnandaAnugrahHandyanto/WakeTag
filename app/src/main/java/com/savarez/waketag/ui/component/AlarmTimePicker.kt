package com.savarez.waketag.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.savarez.waketag.ui.theme.WakeTagTheme

@Composable
fun AlarmTimePicker(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val compact = maxWidth < 360.dp
        if (compact) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NumberDropdownPicker(
                    label = "Hour",
                    value = hour,
                    range = 0..23,
                    onValueChange = onHourChange,
                    modifier = Modifier.fillMaxWidth()
                )
                NumberDropdownPicker(
                    label = "Minute",
                    value = minute,
                    range = 0..59,
                    onValueChange = onMinuteChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                NumberDropdownPicker(
                    label = "Hour",
                    value = hour,
                    range = 0..23,
                    onValueChange = onHourChange,
                    modifier = Modifier.weight(1f)
                )
                NumberDropdownPicker(
                    label = "Minute",
                    value = minute,
                    range = 0..59,
                    onValueChange = onMinuteChange,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NumberDropdownPicker(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge
        )

        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "%02d".format(value))
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            range.forEach { option ->
                DropdownMenuItem(
                    text = { Text(text = "%02d".format(option)) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AlarmTimePickerPreview() {
    WakeTagTheme {
        AlarmTimePicker(
            hour = 7,
            minute = 30,
            onHourChange = {},
            onMinuteChange = {}
        )
    }
}
