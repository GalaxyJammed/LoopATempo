package com.example.loopatempo.ui.nav

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class Tab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home),
    Habits("Edit", Icons.Filled.Edit),
    Analytics("Analytics", Icons.Filled.BarChart),
    Profile("Profile", Icons.Filled.Person)
}

private class SlotBounds(val x: Float, val width: Float)

@Composable
private fun shieldOutline(): Color =
    if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0x40FFFFFF) else Color(0x40000000)

@Composable
fun BottomBar(
    selected: Tab,
    addActive: Boolean,
    onSelect: (Tab) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val slots = remember { mutableStateMapOf<Tab, SlotBounds>() }
    var centerSlot by remember { mutableStateOf<SlotBounds?>(null) }
    var barX by remember { mutableStateOf(0f) }

    val tabSlot = slots[selected]
    val highlightedTab: Tab? = if (addActive) null else selected

    val targetX: Dp = with(density) {
        if (addActive) {
            centerSlot?.let { (it.x - barX + (it.width - 56.dp.toPx()) / 2f).toDp() } ?: 0.dp
        } else {
            tabSlot?.let { (it.x - barX + 2.dp.toPx()).toDp() } ?: 0.dp
        }
    }
    val targetW: Dp = with(density) {
        if (addActive) 56.dp else tabSlot?.let { (it.width - 4.dp.toPx()).toDp() } ?: 0.dp
    }
    val targetY: Dp = if (addActive) (-8).dp else 8.dp
    val targetR: Dp = if (addActive) 28.dp else 16.dp

    val animX by animateDpAsState(targetValue = targetX, animationSpec = tween(400), label = "pillX")
    val animY by animateDpAsState(targetValue = targetY, animationSpec = tween(400), label = "pillY")
    val animW by animateDpAsState(targetValue = targetW, animationSpec = tween(400), label = "pillW")
    val animR by animateDpAsState(targetValue = targetR, animationSpec = tween(400), label = "pillR")

    val pillReady = if (addActive) centerSlot != null else tabSlot != null

    val onPositioned: (Tab, Float, Float) -> Unit = { tab, x, w -> slots[tab] = SlotBounds(x, w) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .onGloballyPositioned { barX = it.positionInRoot().x }
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                border = BorderStroke(1.dp, shieldOutline()),
                shadowElevation = 4.dp
            ) {}

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-12).dp)
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                    .border(2.dp, MaterialTheme.colorScheme.outline, CircleShape)
            )

            if (pillReady) {
                Box(
                    modifier = Modifier
                        .offset(x = animX, y = animY)
                        .size(width = animW, height = 56.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(animR))
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BarSlot(Tab.Home, highlightedTab, onSelect, onPositioned, Modifier.weight(1f))
                BarSlot(Tab.Habits, highlightedTab, onSelect, onPositioned, Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .fillMaxHeight()
                        .onGloballyPositioned { coords ->
                            centerSlot = SlotBounds(coords.positionInRoot().x, coords.size.width.toFloat())
                        }
                )
                BarSlot(Tab.Analytics, highlightedTab, onSelect, onPositioned, Modifier.weight(1f))
                BarSlot(Tab.Profile, highlightedTab, onSelect, onPositioned, Modifier.weight(1f))
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-12).dp)
                    .size(64.dp)
                    .clickable(onClick = onAdd),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add streak",
                    tint = if (addActive) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
private fun BarSlot(
    tab: Tab,
    highlightedTab: Tab?,
    onSelect: (Tab) -> Unit,
    onPositioned: (Tab, Float, Float) -> Unit,
    modifier: Modifier
) {
    val isHighlighted = tab == highlightedTab
    val tint = if (isHighlighted) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(
        modifier = modifier
            .fillMaxHeight()
            .onGloballyPositioned { coords ->
                onPositioned(tab, coords.positionInRoot().x, coords.size.width.toFloat())
            }
            .clickable { onSelect(tab) },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = tab.icon,
            contentDescription = tab.label,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
        Text(
            text = tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            maxLines = 1,
            softWrap = false
        )
    }
}