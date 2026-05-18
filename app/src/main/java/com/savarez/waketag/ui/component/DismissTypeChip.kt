package com.savarez.waketag.ui.component

import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.savarez.waketag.data.model.DismissType
import com.savarez.waketag.ui.theme.WakeTagTheme

@Composable
fun DismissTypeChip(
    dismissType: DismissType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = dismissType.name) },
        modifier = modifier,
        border = FilterChipDefaults.filterChipBorder(enabled = true, selected = selected)
    )
}

@Preview(showBackground = true)
@Composable
private fun DismissTypeChipPreview() {
    WakeTagTheme {
        DismissTypeChip(
            dismissType = DismissType.NFC,
            selected = true,
            onClick = {}
        )
    }
}
