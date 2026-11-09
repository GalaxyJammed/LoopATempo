package com.example.loopatempo

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.loopatempo.data.HabitRepository
import com.example.loopatempo.reminder.ReminderPlanner
import com.example.loopatempo.service.BackgroundTracking
import com.example.loopatempo.ui.MainApp
import com.example.loopatempo.ui.components.HeaderCard
import com.example.loopatempo.ui.components.ShieldCard
import com.example.loopatempo.ui.theme.LoopATempoTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = (application as LoopATempoApplication).repository
        lifecycleScope.launch { repository.ensureSettings() }
        setContent {
            AppRoot(repository)
        }
    }
}

@Composable
fun AppRoot(repository: HabitRepository) {
    val settings by repository.observeSettings().collectAsStateWithLifecycle(initialValue = null)
    LoopATempoTheme(darkTheme = settings?.darkTheme ?: isSystemInDarkTheme()) {
        LoopATempoApp(repository)
    }
}

@Suppress("DEPRECATION")
fun hasUsageAccess(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.unsafeCheckOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName
    )
    return mode == AppOpsManager.MODE_ALLOWED
}

@Composable
fun LoopATempoApp(repository: HabitRepository) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val settings by repository.observeSettings().collectAsStateWithLifecycle(initialValue = null)
    val habits by repository.observeHabits().collectAsStateWithLifecycle(initialValue = emptyList())
    var granted by remember { mutableStateOf(hasUsageAccess(context)) }
    val keepAlive = settings?.keepAlive ?: false
    val settingsLoaded = settings != null

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                granted = hasUsageAccess(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(granted, keepAlive, settingsLoaded) {
        if (granted && settingsLoaded) {
            BackgroundTracking.apply(context, keepAlive)
        }
    }

    LaunchedEffect(settings, habits) {
        val loaded = settings
        if (loaded != null) {
            ReminderPlanner.reschedule(context, habits, loaded)
        }
    }

    if (granted) MainApp(repository) else UsageAccessScreen()
}

@Composable
fun UsageAccessScreen() {
    val context = LocalContext.current
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HeaderCard(title = "Usage access")
            ShieldCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("LoopATempo needs Usage access to see when you open other apps. Nothing leaves your phone.")
                    Button(onClick = {
                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                    }) {
                        Text("Open Usage access settings")
                    }
                }
            }
        }
    }
}