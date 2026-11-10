package com.example.loopatempo.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.loopatempo.data.HabitEntity
import com.example.loopatempo.data.HabitRepository
import com.example.loopatempo.data.ReleaseInfo
import com.example.loopatempo.service.isWebsiteServiceEnabled
import com.example.loopatempo.ui.add.AddChoiceDialog
import com.example.loopatempo.ui.add.AppPickerSheet
import com.example.loopatempo.ui.add.CustomTaskDialog
import com.example.loopatempo.ui.analytics.AnalyticsScreen
import com.example.loopatempo.ui.components.UndoBar
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
import com.example.loopatempo.widget.HabitWidgetProvider
import java.time.LocalDate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text

private enum class AddStep { None, Choice, Custom, App }

private const val HOME_FILTER_KEY = "filter.home"
private const val HABIT_FILTER_KEY = "filter.habits"
private const val UNDO_MILLIS = 5_000

@Composable
fun MainApp(repository: HabitRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current
    var tab by remember { mutableStateOf(Tab.Home) }
    var profilePage by remember { mutableStateOf(ProfilePage.HOME) }
    var addStep by remember { mutableStateOf(AddStep.None) }
    var showFilter by remember { mutableStateOf(false) }
    var update by remember { mutableStateOf<ReleaseInfo?>(null) }
    var pendingDelete by remember { mutableStateOf<HabitEntity?>(null) }
    var serviceOn by remember { mutableStateOf(isWebsiteServiceEnabled(context)) }

    val homeOptionsRaw by repository.observePref(HOME_FILTER_KEY).collectAsStateWithLifecycle(initialValue = null)
    val habitOptionsRaw by repository.observePref(HABIT_FILTER_KEY).collectAsStateWithLifecycle(initialValue = null)
    val homeOptions = decodeListOptions(homeOptionsRaw, ListOptions())
    val habitOptions = decodeListOptions(habitOptionsRaw, ListOptions(sort = SortChoice.NAME_AZ))
    val habits by repository.observeHabits().collectAsStateWithLifecycle(initialValue = emptyList())
    val todayEpoch = LocalDate.now().toEpochDay()
    val homeItems by repository.observeHomeItems(todayEpoch)
        .collectAsStateWithLifecycle(initialValue = emptyList())
    val restDays by repository.observeRestDays(todayEpoch).collectAsStateWithLifecycle(initialValue = 0)
    val allCheckIns by repository.observeCheckIns().collectAsStateWithLifecycle(initialValue = emptyList())
    val settings by repository.observeSettings().collectAsStateWithLifecycle(initialValue = null)
    val defaultThreshold = settings?.defaultThresholdMinutes ?: 5
    val defaultReminder = settings?.reminderMinutes ?: 20 * 60
    val accessibilityOff = settings?.websiteAutoCheck == true && !serviceOn
    val trackedPackages = habits.mapNotNull { it.packageName }.toSet()
    val visibleItems = homeItems.filter { it.habit.id != pendingDelete?.id }
    val showFilterButton = tab == Tab.Home || tab == Tab.Habits
    val currentOptions = if (tab == Tab.Habits) habitOptions else homeOptions

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                serviceOn = isWebsiteServiceEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(Unit) {
        update = UpdateChecker.latestNewer(context)
    }
    var appHintTrigger by remember { mutableStateOf(0) }
    var showAppHint by remember { mutableStateOf(false) }
    LaunchedEffect(appHintTrigger) {
        if (appHintTrigger > 0) {
            showAppHint = true
            delay(1700)
            showAppHint = false
        }
    }
    LaunchedEffect(homeItems) {
        HabitWidgetProvider.refreshAll(context)
    }
    ServiceUpdateNotice(repository)

    val requestDelete: (HabitEntity) -> Unit = { habit ->
        pendingDelete?.let { previous -> scope.launch { repository.deleteHabit(previous) } }
        pendingDelete = habit
    }

    LaunchedEffect(pendingDelete?.id) {
        val target = pendingDelete ?: return@LaunchedEffect
        delay(UNDO_MILLIS.toLong())
        repository.deleteHabit(target)
        if (pendingDelete?.id == target.id) pendingDelete = null
    }

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
                Tab.Home -> HomeScreen(repository, visibleItems, restDays, innerPadding, homeOptions) {
                    appHintTrigger += 1
                }
                Tab.Habits -> HabitsScreen(
                    repository = repository,
                    habitItems = visibleItems,
                    defaultThreshold = defaultThreshold,
                    defaultReminderMinutes = defaultReminder,
                    reminderEnabled = settings?.reminderEnabled ?: true,
                    contentPadding = innerPadding,
                    options = habitOptions,
                    onRequestDelete = requestDelete
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
            pendingDelete?.let { habit ->
                Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                    UndoBar(
                        message = "Deleted \"${habit.name}\"",
                        durationMillis = UNDO_MILLIS,
                        resetKey = habit.id,
                        onUndo = { pendingDelete = null },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 8.dp)
                    )
                }
            }
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                AnimatedVisibility(
                    visible = showAppHint,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        shadowElevation = 6.dp
                    ) {
                        Text(
                            text = "To complete this task open the app!",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
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