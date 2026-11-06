package com.example.loopatempo.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

private val DefaultThumb = Color(0x99808080)
private val Gutter = 10.dp
private val ThumbWidth = 4.dp
private val ThumbInset = 3.dp
private val MinThumb = 40.dp
private const val MIN_OVERFLOW = 0.15f

fun Modifier.verticalScrollbar(state: ScrollState, color: Color = DefaultThumb): Modifier =
    this.padding(end = Gutter).drawWithContent {
        drawContent()
        val range = state.maxValue.toFloat()
        val viewport = size.height
        if (range <= 0f || range < viewport * MIN_OVERFLOW) return@drawWithContent
        val thumbHeight = (viewport * viewport / (viewport + range)).coerceAtLeast(MinThumb.toPx())
        val thumbTop = (viewport - thumbHeight) * state.value / range
        drawThumb(color, thumbTop, thumbHeight, size.width + ThumbInset.toPx())
    }

fun Modifier.lazyScrollbar(state: LazyListState, color: Color = DefaultThumb): Modifier =
    this.padding(end = Gutter).drawWithContent {
        drawContent()
        val info = state.layoutInfo
        val total = info.totalItemsCount
        val visible = info.visibleItemsInfo.size
        if (visible == 0 || total <= visible) return@drawWithContent
        if (visible.toFloat() / total > 1f - MIN_OVERFLOW) return@drawWithContent
        val viewport = size.height
        val thumbHeight = (viewport * visible / total).coerceAtLeast(MinThumb.toPx())
        val thumbTop = (viewport - thumbHeight) * state.firstVisibleItemIndex / (total - visible)
        drawThumb(color, thumbTop, thumbHeight, size.width + ThumbInset.toPx())
    }

private fun DrawScope.drawThumb(color: Color, top: Float, height: Float, x: Float) {
    drawRoundRect(
        color = color,
        topLeft = Offset(x, top),
        size = Size(ThumbWidth.toPx(), height),
        cornerRadius = CornerRadius(ThumbWidth.toPx() / 2f)
    )
}