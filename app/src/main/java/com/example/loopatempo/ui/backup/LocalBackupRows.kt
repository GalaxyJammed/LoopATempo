package com.example.loopatempo.ui.backup

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.example.loopatempo.LoopATempoApplication
import com.example.loopatempo.data.LocalBackup
import com.example.loopatempo.ui.components.SettingRow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun LocalBackupRows() {
    val context = LocalContext.current
    val db = (context.applicationContext as LoopATempoApplication).database
    val scope = rememberCoroutineScope()
    var pendingImport by remember { mutableStateOf<String?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = withContext(Dispatchers.IO) { LocalBackup.exportJson(db) }
                    withContext(Dispatchers.IO) {
                        context.contentResolver.openOutputStream(uri)?.use {
                            it.write(json.toByteArray(Charsets.UTF_8))
                        }
                    }
                    Toast.makeText(context, "Backup exported", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                try {
                    val json = withContext(Dispatchers.IO) {
                        context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { it.readText() }
                    }
                    if (json != null && LocalBackup.isLoopATempoBackup(json)) {
                        pendingImport = json
                    } else {
                        Toast.makeText(context, "This is not a LoopATempo backup", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Could not read file", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    SettingRow(
        icon = Icons.Filled.Upload,
        title = "Export backup",
        subtitle = "Save habits, check-ins, and settings to a file",
        onClick = { exportLauncher.launch("loopatempo-backup.json") },
        trailing = {}
    )
    SettingRow(
        icon = Icons.Filled.Download,
        title = "Import backup",
        subtitle = "Replaces everything on this phone",
        onClick = { importLauncher.launch(arrayOf("*/*")) },
        trailing = {}
    )

    pendingImport?.let { json ->
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text("Replace everything?") },
            text = {
                Text("Importing replaces all habits, check-ins, and settings on this phone. This can't be undone.")
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingImport = null
                    scope.launch {
                        try {
                            withContext(Dispatchers.IO) {
                                LocalBackup.restoreJson(context.applicationContext, db, json)
                            }
                            Toast.makeText(context, "Backup imported", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }) {
                    Text("Replace")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImport = null }) { Text("Cancel") }
            }
        )
    }
}