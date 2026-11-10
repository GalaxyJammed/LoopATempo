package com.example.loopatempo.ui.components

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.example.loopatempo.data.WebsiteIcons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun WebsiteIcon(host: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val image: ImageBitmap? by produceState<ImageBitmap?>(initialValue = null, host) {
        value = withContext(Dispatchers.IO) {
            WebsiteIcons.load(context, host)?.asImageBitmap()
        }
    }
    val loaded = image
    if (loaded != null) {
        Image(bitmap = loaded, contentDescription = null, modifier = modifier)
    } else {
        Icon(
            imageVector = Icons.Filled.Language,
            contentDescription = null,
            modifier = modifier,
            tint = MaterialTheme.colorScheme.primary
        )
    }
}