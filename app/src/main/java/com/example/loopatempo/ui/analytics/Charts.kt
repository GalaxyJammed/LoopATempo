package com.example.loopatempo.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun BarChart(
    values: List<Int>,
    labels: List<String>,
    modifier: Modifier = Modifier,
    highlight: Int? = null
) {
    val barColor = MaterialTheme.colorScheme.primary
    val dimColor = barColor.copy(alpha = 0.45f)

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
            if (values.isEmpty()) return@Canvas
            val max = (values.maxOrNull() ?: 0).coerceAtLeast(1)
            val slot = size.width / values.size
            val barWidth = slot * 0.6f
            values.forEachIndexed { i, value ->
                val barHeight = size.height * value / max
                drawRoundRect(
                    color = if (highlight == null || i == highlight) barColor else dimColor,
                    topLeft = Offset(i * slot + (slot - barWidth) / 2f, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(6.dp.toPx())
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
            values.zip(labels).forEach { (value, label) ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = value.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}