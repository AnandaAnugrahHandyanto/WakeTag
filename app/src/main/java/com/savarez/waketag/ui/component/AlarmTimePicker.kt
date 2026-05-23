package com.savarez.waketag.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.savarez.waketag.ui.theme.WakeTagTheme
import kotlinx.coroutines.delay

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
        val dropdownMaxHeight = when {
            maxHeight < 700.dp -> 220.dp
            maxHeight < 900.dp -> 280.dp
            else -> 320.dp
        }
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
                    maxDropdownHeight = dropdownMaxHeight,
                    modifier = Modifier.fillMaxWidth()
                )
                NumberDropdownPicker(
                    label = "Minute",
                    value = minute,
                    range = 0..59,
                    onValueChange = onMinuteChange,
                    maxDropdownHeight = dropdownMaxHeight,
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
                    maxDropdownHeight = dropdownMaxHeight,
                    modifier = Modifier.weight(1f)
                )
                NumberDropdownPicker(
                    label = "Minute",
                    value = minute,
                    range = 0..59,
                    onValueChange = onMinuteChange,
                    maxDropdownHeight = dropdownMaxHeight,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun NumberDropdownPicker(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    maxDropdownHeight: Dp,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    LaunchedEffect(expanded, value, range, maxDropdownHeight) {
        if (!expanded) return@LaunchedEffect
        delay(16L)
        val itemHeightPx = with(density) { 48.dp.roundToPx() }
        val menuHeightPx = with(density) { maxDropdownHeight.roundToPx() }
        val index = (value - range.first).coerceIn(0, range.last - range.first)
        val centeredTarget = (index * itemHeightPx) - ((menuHeightPx - itemHeightPx) / 2)
        val target = centeredTarget.coerceIn(0, scrollState.maxValue)
        scrollState.scrollTo(target)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = "%02d".format(value),
                onValueChange = {},
                readOnly = true,
                label = { Text(text = label) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .exposedDropdownSize(matchTextFieldWidth = true)
                    .heightIn(max = maxDropdownHeight)
            ) {
                Column(modifier = Modifier.verticalScroll(scrollState)) {
                    range.forEach { option ->
                        val isSelected = option == value
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "%02d".format(option),
                                    style = if (isSelected) {
                                        MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                                    } else {
                                        MaterialTheme.typography.bodyLarge
                                    },
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            },
                            onClick = {
                                onValueChange(option)
                                expanded = false
                            }
                        )
                    }
                }
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
