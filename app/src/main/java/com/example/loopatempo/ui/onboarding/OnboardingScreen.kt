package com.example.loopatempo.ui.onboarding

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.example.loopatempo.LoopATempoApplication
import com.example.loopatempo.hasUsageAccess
import com.example.loopatempo.notify.StreakNotifier
import com.example.loopatempo.ui.components.HeaderCard
import com.example.loopatempo.ui.components.ShieldCard
import com.example.loopatempo.ui.permissions.appDetailsIntent
import kotlinx.coroutines.launch

const val ONBOARDING_KEY = "onboarding.done"

private enum class Step(val title: String) {
    WELCOME("Welcome"),
    USAGE("Usage access"),
    NOTIFICATIONS("Notifications"),
    BATTERY("Background running"),
    OPTIONAL("Optional features")
}

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var step by remember { mutableStateOf(Step.WELCOME) }
    var usageOn by remember { mutableStateOf(hasUsageAccess(context)) }
    var notificationsOn by remember { mutableStateOf(StreakNotifier.canPost(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                usageOn = hasUsageAccess(context)
                notificationsOn = StreakNotifier.canPost(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { notificationsOn = StreakNotifier.canPost(context) }

    val allowNotifications: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            context.startActivity(appDetailsIntent(context))
        }
    }

    val steps = Step.entries
    val index = step.ordinal
    val isLast = index == steps.lastIndex

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            HeaderCard(title = "Setup")
            Text(
                text = "Step ${index + 1} of ${steps.size}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            ShieldCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(step.title, style = MaterialTheme.typography.titleMedium)
                    when (step) {
                        Step.WELCOME -> Text(
                            text = "LoopATempo keeps your daily streaks going. A few permissions let it " +
                                    "work in the background. It takes about a minute, and you can skip any step.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Step.USAGE -> {
                            StatusLine(usageOn)
                            Text(
                                text = "Usage access lets LoopATempo see which apps you open, so app " +
                                        "habits can count. Nothing leaves your phone.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Button(onClick = {
                                context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
                            }) {
                                Text("Open Usage access settings")
                            }
                        }
                        Step.NOTIFICATIONS -> {
                            StatusLine(notificationsOn)
                            Text(
                                text = "Notifications are used for habit reminders and streak alerts.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Button(onClick = allowNotifications) {
                                Text("Allow notifications")
                            }
                        }
                        Step.BATTERY -> {
                            Text(
                                text = "Android may pause LoopATempo to save battery. Turning off battery " +
                                        "restrictions helps tracking keep running. Some phones also need Autostart on.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Button(onClick = {
                                context.startActivity(
                                    Intent(
                                        Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                        Uri.parse("package:${context.packageName}")
                                    )
                                )
                            }) {
                                Text("Turn off battery restrictions")
                            }
                            OutlinedButton(onClick = {
                                context.startActivity(appDetailsIntent(context))
                            }) {
                                Text("Open app details for Autostart")
                            }
                        }
                        Step.OPTIONAL -> Text(
                            text = "Keep tracking alive is on by default. It shows a silent notification " +
                                    "and checks apps every 30 seconds. Turn it off in Settings if you'd rather not see it.\n\n" +
                                    "Website auto-check is off by default. It needs Accessibility, and you can turn " +
                                    "it on in Settings → Tasks.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (index > 0) {
                    OutlinedButton(onClick = { step = steps[index - 1] }) {
                        Text("Back")
                    }
                }
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onFinish) {
                    Text("Skip")
                }
                Button(onClick = {
                    if (isLast) onFinish() else step = steps[index + 1]
                }) {
                    Text(if (isLast) "Finish" else "Next")
                }
            }
        }
    }
}

@Composable
private fun StatusLine(on: Boolean) {
    Text(
        text = if (on) "Status: On" else "Status: Off. Turn it on to continue.",
        style = MaterialTheme.typography.bodySmall,
        color = if (on) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    )
}

@Composable
fun SetupGuideButton() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val repository = (context.applicationContext as LoopATempoApplication).repository
    OutlinedButton(
        onClick = { scope.launch { repository.savePref(ONBOARDING_KEY, "NO") } },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Show setup guide again")
    }
}