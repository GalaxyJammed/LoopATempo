package com.example.loopatempo.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.loopatempo.data.Achievement
import com.example.loopatempo.data.HabitRepository
import com.example.loopatempo.data.buildAchievements
import com.example.loopatempo.ui.components.HeaderCard
import com.example.loopatempo.ui.components.SectionDivider
import com.example.loopatempo.ui.components.ShieldCard
import com.example.loopatempo.ui.components.verticalScrollbar

@Composable
fun MilestonesScreen(
    repository: HabitRepository,
    contentPadding: PaddingValues,
    onBack: () -> Unit
) {
    val habits by repository.observeHabits().collectAsStateWithLifecycle(initialValue = emptyList())
    val checkIns by repository.observeCheckIns().collectAsStateWithLifecycle(initialValue = emptyList())
    val achievements = remember(habits, checkIns) { buildAchievements(habits, checkIns) }
    val unlockedCount = achievements.count { it.unlocked }
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(16.dp)
            .verticalScrollbar(scroll)
            .verticalScroll(scroll),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HeaderCard(title = "Milestones", onBack = onBack)

        ShieldCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SectionDivider("Progress")
                Text(
                    text = "$unlockedCount of ${achievements.size} unlocked",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                achievements.forEach { AchievementRow(it) }
            }
        }
    }
}

@Composable
private fun AchievementRow(achievement: Achievement) {
    val primary = MaterialTheme.colorScheme.primary
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(if (achievement.unlocked) primary else Color.Transparent, CircleShape)
                .border(
                    width = 2.dp,
                    color = if (achievement.unlocked) primary else MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = achievementIcon(achievement.id),
                contentDescription = null,
                tint = if (achievement.unlocked) MaterialTheme.colorScheme.onPrimary else muted,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.titleSmall,
                color = if (achievement.unlocked) MaterialTheme.colorScheme.onSurface else muted
            )
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall,
                color = muted
            )
            if (!achievement.unlocked) {
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(achievement.fraction)
                            .fillMaxHeight()
                            .background(primary)
                    )
                }
                Text(
                    text = "${achievement.progress} / ${achievement.target}",
                    style = MaterialTheme.typography.labelSmall,
                    color = muted
                )
            }
        }
        if (achievement.unlocked) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Unlocked",
                tint = primary
            )
        }
    }
}

private fun achievementIcon(id: String): ImageVector = when (id) {
    "first" -> Icons.Filled.Star
    "streak7", "streak30", "streak100" -> Icons.Filled.LocalFireDepartment
    "total50", "total200" -> Icons.Filled.CheckCircle
    "collector" -> Icons.Filled.Apps
    else -> Icons.Filled.EmojiEvents
}