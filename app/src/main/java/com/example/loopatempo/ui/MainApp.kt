package com.example.loopatempo.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.loopatempo.data.HabitRepository
import com.example.loopatempo.data.ReleaseInfo
import com.example.loopatempo.ui.add.AddChoiceDialog
import com.example.loopatempo.ui.add.AppPickerSheet
import com.example.loopatempo.ui.add.CustomTaskDialog
import com.example.loopatempo.ui.analytics.AnalyticsScreen
import com.example.loopatempo.ui.filter.FilterButton
import com.example.loopatempo.ui.filter.FilterDialog
import com.example.loopatempo.ui.filter.ListOptions
import com.example.loopatempo.ui.filter.SortChoice
import com.example.loopatempo.ui.filter.decodeListOptions
import com.example.loopatempo.ui.filter.encodeListOptions
import com.example.loopatempo.ui.habits.HabitsScreen
import com.example.loopatempo.ui.home.HomeScreen
import com.example.loopatempo.ui.nav.BottomBar
import com.example.loopatempo.ui.nav.Tab
import com.example.loopatempo.ui.profile.ProfilePage
import com.example.loopatempo.ui.profile.ProfileScreen
import com.example.loopatempo.ui.update.ServiceUpdateNotice
import com.example.loopatempo.update.UpdateChecker
import java.time.LocalDate
import kotlinx.coroutines.launch
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.loopatempo.service.isWebsiteServiceEnabled

private enum class AddStep { None, Choice, Custom, App }

private const val HOME_FILTER_KEY = "filter.home"
private const val HABIT_FILTER_KEY = "filter.habits"

@Composable
fun MainApp(repository: HabitRepository) {
    val context = LocalContext.current
    var tab by remember { mutableStateOf(Tab.Home) }
    var profilePage by remember { mutableStateOf(ProfilePage.HOME) }
    var addStep by remember { mutableStateOf(AddStep.None) }
    var showFilter by remember { mutableStateOf(false) }
    var update by remember { mutableStateOf<ReleaseInfo?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current
    var serviceOn by remember { mutableStateOf(isWebsiteServiceEnabled(context)) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                serviceOn = isWebsiteServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val homeOptionsRaw by repository.observePref(HOME_FILTER_KEY).collectAsStateWithLifecycle(initialValue = null)
    val habitOptionsRaw by repository.observePref(HABIT_FILTER_KEY).collectAsStateWithLifecycle(initialValue = null)
    val homeOptions = decodeListOptions(homeOptionsRaw, ListOptions())
    val habitOptions = decodeListOptions(habitOptionsRaw, ListOptions(sort = SortChoice.NAME_AZ))
    val habits by repository.observeHabits().collectAsStateWithLifecycle(initialValue = emptyList())
    val todayEpoch = LocalDate.now().toEpochDay()
    val homeItems by repository.observeHomeItems(todayEpoch)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val allCheckIns by repository.observeCheckIns().collectAsStateWithLifecycle(initialValue = emptyList())
    val settings by repository.observeSettings().collectAsStateWithLifecycle(initialValue = null)
    val accessibilityOff = settings?.websiteAutoCheck == true && !serviceOn
    val defaultThreshold = settings?.defaultThresholdMinutes ?: 5
    val defaultReminder = settings?.reminderMinutes ?: 20 * 60
    val scope = rememberCoroutineScope()
    val trackedPackages = habits.mapNotNull { it.packageName }.toSet()
    val showFilterButton = tab == Tab.Home || tab == Tab.Habits
    val currentOptions = if (tab == Tab.Habits) habitOptions else homeOptions

    LaunchedEffect(Unit) {
        update = UpdateChecker.latestNewer(context)
    }
    ServiceUpdateNotice(repository)

    val onFilterChange: (ListOptions) -> Unit = { updated ->
        scope.launch {
            if (tab == Tab.Habits) {
                repository.savePref(HABIT_FILTER_KEY, encodeListOptions(updated))
            } else {
                repository.savePref(HOME_FILTER_KEY, encodeListOptions(updated))
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomBar(
                selected = tab,
                addActive = addStep != AddStep.None,
                onSelect = { selected ->
                    if (selected == Tab.Profile) profilePage = ProfilePage.HOME
                    tab = selected
                },
                onAdd = { addStep = AddStep.Choice },
                updateAvailable = update != null || accessibilityOff,
                onUpdateClick = {
                    profilePage = if (update != null) ProfilePage.ABOUT else ProfilePage.SETTINGS
                    tab = Tab.Profile
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when (tab) {
                Tab.Home -> HomeScreen(repository, homeItems, innerPadding, homeOptions)
                Tab.Habits -> HabitsScreen(
                    repository,
                    homeItems,
                    defaultThreshold,
                    defaultReminder,
                    settings?.reminderEnabled ?: true,
                    innerPadding,
                    habitOptions
                )
                Tab.Analytics -> AnalyticsScreen(habits, allCheckIns, innerPadding)
                Tab.Profile -> ProfileScreen(
                    repository = repository,
                    contentPadding = innerPadding,
                    page = profilePage,
                    onPageChange = { profilePage = it },
                    update = update,
                    settingsAttention = accessibilityOff
                )
            }
            if (showFilterButton) {
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    FilterButton(
                        onClick = { showFilter = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 20.dp, bottom = 8.dp)
                    )
                }
            }
        }
    }

    if (showFilter) {
        FilterDialog(
            options = currentOptions,
            allowStreak = tab == Tab.Home,
            onChange = onFilterChange,
            onDismiss = { showFilter = false }
        )
    }

    when (addStep) {
        AddStep.None -> Unit
        AddStep.Choice -> AddChoiceDialog(
            onDismiss = { addStep = AddStep.None },
            onCustom = { addStep = AddStep.Custom },
            onApp = { addStep = AddStep.App }
        )
        AddStep.Custom -> CustomTaskDialog(
            reminderEnabled = settings?.reminderEnabled ?: false,
            defaultReminderMinutes = defaultReminder,
            onDismiss = { addStep = AddStep.None },
            onAdd = { name, iconKey, reminder, website ->
                scope.launch { repository.addHabit(name, null, iconKey, reminder, website) }
                addStep = AddStep.None
            }
        )
        AddStep.App -> AppPickerSheet(
            trackedPackages = trackedPackages,
            onDismiss = { addStep = AddStep.None },
            onPick = { app ->
                scope.launch { repository.addHabit(app.label, app.packageName, null) }
                addStep = AddStep.None
            }
        )
    }
}