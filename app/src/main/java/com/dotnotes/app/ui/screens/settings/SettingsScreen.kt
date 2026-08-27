package com.dotnotes.app.ui.screens.settings

import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dotnotes.app.DotNotesApp
import com.dotnotes.app.ui.i18n.LocalStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(
            DotNotesApp.instance.settingsDataStore,
            DotNotesApp.instance.database.noteDao()
        )
    )
) {
    val strings = LocalStrings.current
    val themeMode by viewModel.themeMode.collectAsState()
    val snoozeDuration by viewModel.snoozeDuration.collectAsState()
    val language by viewModel.language.collectAsState()
    val isCheckingUpdate by viewModel.isCheckingUpdate.collectAsState()
    val availableUpdate by viewModel.availableUpdate.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    
    val context = LocalContext.current
    var showThemeDialog by remember { mutableStateOf(false) }
    var showSnoozeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackup(context, uri) { success ->
                val msg = if (success) strings.exportSuccess else strings.exportFailed
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importBackup(context, uri) { count ->
                val msg = if (count >= 0) String.format(strings.importSuccess, count) else strings.importFailed
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.settings) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Language
            ListItem(
                headlineContent = { Text(strings.language) },
                supportingContent = { Text(if (language == "id") strings.indonesian else strings.english) },
                modifier = Modifier.clickable { showLanguageDialog = true }
            )
            HorizontalDivider()

            // Theme
            ListItem(
                headlineContent = { Text(strings.theme) },
                supportingContent = {
                    val label = when (themeMode) {
                        "light" -> strings.themeLight
                        "dark" -> strings.themeDark
                        else -> strings.themeSystem
                    }
                    Text(label)
                },
                modifier = Modifier.clickable { showThemeDialog = true }
            )
            HorizontalDivider()

            // Snooze Duration
            ListItem(
                headlineContent = { Text(strings.snoozeDuration) },
                supportingContent = { Text("$snoozeDuration ${strings.minutes}") },
                modifier = Modifier.clickable { showSnoozeDialog = true }
            )
            HorizontalDivider()

            // Export Backup
            ListItem(
                headlineContent = { Text(strings.exportBackup) },
                supportingContent = { Text(strings.exportBackupDesc) },
                modifier = Modifier.clickable {
                    val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    val dateStr = formatter.format(Date())
                    exportLauncher.launch("dotnotes_backup_$dateStr.json")
                }
            )
            HorizontalDivider()

            // Import Backup
            ListItem(
                headlineContent = { Text(strings.importBackup) },
                supportingContent = { Text(strings.importBackupDesc) },
                modifier = Modifier.clickable {
                    importLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                }
            )
            HorizontalDivider()

            // Check for Updates
            ListItem(
                headlineContent = { Text(strings.checkForUpdates) },
                supportingContent = { Text(strings.checkForUpdatesDesc) },
                trailingContent = {
                    if (isCheckingUpdate) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                },
                modifier = Modifier.clickable(enabled = !isCheckingUpdate && downloadProgress == null) {
                    viewModel.checkForUpdate("1.1.0") {
                        Toast.makeText(context, strings.alreadyLatest, Toast.LENGTH_SHORT).show()
                    }
                }
            )
            HorizontalDivider()

            // About
            ListItem(
                headlineContent = { Text(strings.about) },
                supportingContent = { Text("dotnotes v1.1.0") }
            )
            HorizontalDivider()
        }
    }

    // Language Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(strings.language) },
            text = {
                Column {
                    listOf("en" to strings.english, "id" to strings.indonesian).forEach { (code, name) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setLanguage(code)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = language == code,
                                onClick = {
                                    viewModel.setLanguage(code)
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(name)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Theme Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text(strings.theme) },
            text = {
                Column {
                    listOf(
                        "system" to strings.themeSystem,
                        "light" to strings.themeLight,
                        "dark" to strings.themeDark
                    ).forEach { (mode, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(label)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Snooze Dialog
    if (showSnoozeDialog) {
        AlertDialog(
            onDismissRequest = { showSnoozeDialog = false },
            title = { Text(strings.snoozeDuration) },
            text = {
                Column {
                    listOf(5, 10, 15).forEach { minutes ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setSnoozeDuration(minutes)
                                    showSnoozeDialog = false
                                }
                                .padding(vertical = 12.dp)
                        ) {
                            RadioButton(
                                selected = snoozeDuration == minutes,
                                onClick = {
                                    viewModel.setSnoozeDuration(minutes)
                                    showSnoozeDialog = false
                                }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("$minutes ${strings.minutes}")
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Update Dialog
    if (availableUpdate != null) {
        val update = availableUpdate!!
        AlertDialog(
            onDismissRequest = {
                if (downloadProgress == null) viewModel.dismissUpdateDialog()
            },
            title = { Text("${strings.updateAvailable} (${update.tagName})") },
            text = {
                Column {
                    if (downloadProgress != null) {
                        Text(strings.downloadingUpdate)
                        Spacer(Modifier.height(12.dp))
                        if (downloadProgress!! > 0f) {
                            LinearProgressIndicator(
                                progress = { downloadProgress!! },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        Text(
                            text = if (update.changelog.isNotBlank()) update.changelog else "Update ${update.tagName} is ready.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                if (downloadProgress == null) {
                    Button(onClick = { viewModel.downloadAndInstall(context, update) }) {
                        Text(strings.updateNow)
                    }
                }
            },
            dismissButton = {
                if (downloadProgress == null) {
                    TextButton(onClick = { viewModel.dismissUpdateDialog() }) {
                        Text(strings.later)
                    }
                }
            }
        )
    }
}
