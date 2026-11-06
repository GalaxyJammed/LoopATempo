package com.example.loopatempo.ui.icons

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector

data class IconChoice(val key: String, val icon: ImageVector)

val defaultIconChoices: List<IconChoice> = listOf(
    IconChoice("default:Star", Icons.Filled.Star),
    IconChoice("default:Favorite", Icons.Filled.Favorite),
    IconChoice("default:WaterDrop", Icons.Filled.WaterDrop),
    IconChoice("default:FitnessCenter", Icons.Filled.FitnessCenter),
    IconChoice("default:DirectionsRun", Icons.AutoMirrored.Filled.DirectionsRun),
    IconChoice("default:MenuBook", Icons.AutoMirrored.Filled.MenuBook),
    IconChoice("default:Bedtime", Icons.Filled.Bedtime),
    IconChoice("default:SelfImprovement", Icons.Filled.SelfImprovement),
    IconChoice("default:Restaurant", Icons.Filled.Restaurant),
    IconChoice("default:Code", Icons.Filled.Code),
    IconChoice("default:Brush", Icons.Filled.Brush),
    IconChoice("default:Home", Icons.Filled.Home)
)

fun defaultIconFor(key: String): ImageVector? =
    defaultIconChoices.firstOrNull { it.key == key }?.icon

@Composable
fun HabitIcon(iconKey: String?, modifier: Modifier = Modifier) {
    if (iconKey == null) return
    if (iconKey.startsWith("file:")) {
        val path = iconKey.removePrefix("file:")
        val bitmap = remember(path) { BitmapFactory.decodeFile(path)?.asImageBitmap() }
        if (bitmap != null) {
            Image(bitmap = bitmap, contentDescription = null, modifier = modifier)
        }
    } else {
        defaultIconFor(iconKey)?.let {
            Icon(
                imageVector = it,
                contentDescription = null,
                modifier = modifier,
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}