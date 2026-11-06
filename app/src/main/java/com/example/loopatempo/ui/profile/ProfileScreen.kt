package com.example.loopatempo.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.loopatempo.data.HabitRepository
import com.example.loopatempo.ui.components.HeaderCard
import com.example.loopatempo.ui.components.SettingRow
import com.example.loopatempo.ui.components.verticalScrollbar
import com.example.loopatempo.ui.permissions.FixScreen
import com.example.loopatempo.ui.permissions.MissingPermissionsButton
import com.example.loopatempo.ui.settings.SettingsScreen

private enum class ProfilePage { HOME, MILESTONES, ABOUT, SETTINGS, FIX }

@Composable
fun ProfileScreen(repository: HabitRepository, contentPadding: PaddingValues) {
    var page by remember { mutableStateOf(ProfilePage.HOME) }
    BackHandler(enabled = page != ProfilePage.HOME) { page = ProfilePage.HOME }

    when (page) {
        ProfilePage.HOME -> ProfileHome(
            contentPadding = contentPadding,
            onMilestones = { page = ProfilePage.MILESTONES },
            onAbout = { page = ProfilePage.ABOUT },
            onSettings = { page = ProfilePage.SETTINGS },
            onFix = { page = ProfilePage.FIX }
        )
        ProfilePage.MILESTONES -> MilestonesScreen(
            repository = repository,
            contentPadding = contentPadding,
            onBack = { page = ProfilePage.HOME }
        )
        ProfilePage.ABOUT -> AboutScreen(
            contentPadding = contentPadding,
            onBack = { page = ProfilePage.HOME }
        )
        ProfilePage.SETTINGS -> SettingsScreen(
            repository = repository,
            contentPadding = contentPadding,
            onBack = { page = ProfilePage.HOME }
        )
        ProfilePage.FIX -> FixScreen(
            contentPadding = contentPadding,
            onBack = { page = ProfilePage.HOME }
        )
    }
}

@Composable
private fun ProfileHome(
    contentPadding: PaddingValues,
    onMilestones: () -> Unit,
    onAbout: () -> Unit,
    onSettings: () -> Unit,
    onFix: () -> Unit
) {
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
        HeaderCard(title = "Profile")

        MissingPermissionsButton(onFix = onFix)

        SettingRow(
            icon = Icons.Filled.EmojiEvents,
            title = "Milestones",
            subtitle = "Your streaks and check-in goals",
            onClick = onMilestones,
            trailing = { Chevron() }
        )

        SettingRow(
            icon = Icons.Filled.Settings,
            title = "Settings",
            subtitle = "Appearance, tasks, reminders, and backup",
            onClick = onSettings,
            trailing = { Chevron() }
        )

        SettingRow(
            icon = Icons.Filled.Info,
            title = "About",
            subtitle = "Version, release notes, and privacy",
            onClick = onAbout,
            trailing = { Chevron() }
        )
    }
}

@Composable
private fun Chevron() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null
    )
}