package com.example.loopatempo.ui.filter

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp

@Composable
fun FilterButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val outline = if (isDark) Color(0x40FFFFFF) else Color(0x40000000)

    Surface(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        border = BorderStroke(1.dp, outline),
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.FilterList,
                contentDescription = "Filter",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}