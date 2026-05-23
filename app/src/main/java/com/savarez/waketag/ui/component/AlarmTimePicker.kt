package com.savarez.waketag.ui.component

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.savarez.waketag.ui.theme.WakeTagTheme

private const val ALARM_TIME_PICKER_LOG_TAG = "AlarmTimePicker"

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
private fun NumberDropdownPicker(
    label: String,
    value: Int,
    range: IntRange,
    onValueChange: (Int) -> Unit,
    maxDropdownHeight: Dp,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    var anchorWidthPx by remember { mutableIntStateOf(0) }
    val interactionSource = remember { MutableInteractionSource() }

    LaunchedEffect(expanded, label) {
        Log.d(ALARM_TIME_PICKER_LOG_TAG, "$label expanded changed: $expanded")
        if (expanded) {
            Log.d(ALARM_TIME_PICKER_LOG_TAG, "$label dropdown opened")
        }
    }

    LaunchedEffect(expanded, value, range) {
        if (!expanded) return@LaunchedEffect
        val selectedIndex = (value - range.first).coerceIn(0, range.last - range.first)
        val firstVisibleTarget = (selectedIndex - 3).coerceAtLeast(0)
        listState.scrollToItem(firstVisibleTarget)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    val nextState = !expanded
                    Log.d(ALARM_TIME_PICKER_LOG_TAG, "$label expanded set to: $nextState")
                    expanded = nextState
                }
        ) {
            OutlinedTextField(
                value = "%02d".format(value),
                onValueChange = {},
                readOnly = true,
                label = { Text(text = label) },
                trailingIcon = { Text(text = if (expanded) "▲" else "▼") },
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { anchorWidthPx = it.width }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    Log.d(ALARM_TIME_PICKER_LOG_TAG, "$label dropdown dismissed")
                    expanded = false
                },
                modifier = Modifier
                    .then(
                        if (anchorWidthPx > 0) {
                            Modifier.width(with(density) { anchorWidthPx.toDp() })
                        } else {
                            Modifier.fillMaxWidth()
                        }
                    )
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.heightIn(max = maxDropdownHeight),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    items(range.toList()) { option ->
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
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            onClick = {
                                Log.d(ALARM_TIME_PICKER_LOG_TAG, "$label selected: $option")
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
