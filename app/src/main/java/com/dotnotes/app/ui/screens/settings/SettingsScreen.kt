package com.dotnotes.app.ui.screens.settings

import android.app.Activity
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
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.ListItem
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dotnotes.app.DotNotesApp
import com.dotnotes.app.sync.GoogleAuthHelper
import com.google.android.gms.auth.api.signin.GoogleSignIn

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
    val themeMode by viewModel.themeMode.collectAsState()
    val snoozeDuration by viewModel.snoozeDuration.collectAsState()
    val googleEmail by viewModel.googleEmail.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    
    val context = LocalContext.current
    var showThemeDialog by remember { mutableStateOf(false) }
    var showSnoozeDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(Exception::class.java)
                viewModel.setGoogleEmail(account.email)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            viewModel.exportBackup(context, uri) { success ->
                val msg = if (success) "Backup berhasil diekspor" else "Gagal mengekspor backup"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.importBackup(context, uri) { count ->
                val msg = if (count >= 0) "Berhasil mengimpor $count catatan" else "Gagal mengimpor file backup"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            ListItem(
                headlineContent = { Text("Theme") },
                supportingContent = { Text(themeMode.replaceFirstChar { it.uppercase() }) },
                modifier = Modifier.clickable { showThemeDialog = true }
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Snooze Duration") },
                supportingContent = { Text("$snoozeDuration minutes") },
                modifier = Modifier.clickable { showSnoozeDialog = true }
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Export Backup") },
                supportingContent = { Text("Simpan catatan ke file JSON") },
                modifier = Modifier.clickable {
                    val formatter = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    val dateStr = formatter.format(Date())
                    exportLauncher.launch("dotnotes_backup_$dateStr.json")
                }
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Import Backup") },
                supportingContent = { Text("Pulihkan catatan dari file JSON") },
                modifier = Modifier.clickable {
                    importLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                }
            )
            HorizontalDivider()

            ListItem(
                headlineContent = { Text("Google Account") },
                supportingContent = { Text(googleEmail ?: "Not logged in") },
                modifier = Modifier.clickable {
                    if (googleEmail == null) {
                        val signInClient = GoogleAuthHelper.getSignInClient(context)
                        launcher.launch(signInClient.signInIntent)
                    } else {
                        // Logout
                        val signInClient = GoogleAuthHelper.getSignInClient(context)
                        signInClient.signOut().addOnCompleteListener {
                            viewModel.setGoogleEmail(null)
                        }
                    }
                }
            )
            HorizontalDivider()

            if (googleEmail != null) {
                ListItem(
                    headlineContent = { Text("Sync with Google Drive") },
                    supportingContent = { Text("App Data Folder") },
                    trailingContent = {
                        if (isSyncing) {
                            CircularProgressIndicator()
                        } else {
                            Button(onClick = { viewModel.syncNotes(context) }) {
                                Text("Sync")
                            }
                        }
                    }
                )
                HorizontalDivider()
            }

            ListItem(
                headlineContent = { Text("About") },
                supportingContent = { Text("dotnotes v1.0.8") }
            )
            HorizontalDivider()
        }
    }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Theme") },
            text = {
                Column {
                    listOf("system", "light", "dark").forEach { mode ->
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
                            Text(mode.replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    if (showSnoozeDialog) {
        AlertDialog(
            onDismissRequest = { showSnoozeDialog = false },
            title = { Text("Snooze Duration") },
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
                            Text("$minutes minutes")
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}
