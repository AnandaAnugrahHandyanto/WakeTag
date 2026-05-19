package com.savarez.waketag.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.savarez.waketag.data.model.Alarm
import com.savarez.waketag.data.model.DismissType
import com.savarez.waketag.ui.theme.WakeTagTheme
import com.savarez.waketag.util.displayLabel
import com.savarez.waketag.util.futureCapabilityHint

@Composable
fun AlarmCard(
    alarm: Alarm,
    onEnabledChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            val compact = maxWidth < 360.dp
            if (compact) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AlarmInfoContent(alarm = alarm)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (alarm.enabled) "Enabled" else "Disabled",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(
                                checked = alarm.enabled,
                                onCheckedChange = onEnabledChange
                            )
                            TextButton(onClick = onEditClick) {
                                Text("Edit")
                            }
                            TextButton(onClick = onDeleteClick) {
                                Text("Delete")
                            }
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AlarmInfoContent(
                        alarm = alarm,
                        modifier = Modifier.weight(1f)
                    )

                    Column(
                        horizontalAlignment = Alignment.End,
                        modifier = Modifier.widthIn(min = 96.dp)
                    ) {
                        Switch(
                            checked = alarm.enabled,
                            onCheckedChange = onEnabledChange
                        )
                        Text(
                            text = if (alarm.enabled) "Enabled" else "Disabled",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        TextButton(onClick = onEditClick) {
                            Text("Edit")
                        }
                        TextButton(onClick = onDeleteClick) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AlarmInfoContent(
    alarm: Alarm,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "%02d:%02d".format(alarm.hour, alarm.minute),
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = alarm.dismissType.displayLabel,
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = alarm.dismissType.futureCapabilityHint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AlarmCardPreview() {
    WakeTagTheme {
        AlarmCard(
            alarm = Alarm(
                id = 1,
                hour = 7,
                minute = 15,
                enabled = true,
                dismissType = DismissType.QR
            ),
            onEnabledChange = {},
            onEditClick = {},
            onDeleteClick = {}
        )
    }
}
