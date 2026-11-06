package com.example.loopatempo.ui.components

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.example.loopatempo.data.loadAppIconBitmap

@Composable
fun AppIcon(packageName: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val bitmap = remember(packageName) { loadAppIconBitmap(context, packageName) }
    if (bitmap != null) {
        Image(bitmap = bitmap, contentDescription = null, modifier = modifier)
    } else {
        Icon(imageVector = Icons.Filled.Apps, contentDescription = null, modifier = modifier)
    }
}