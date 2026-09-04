package com.dotnotes.app.ui.screens.settings

import android.content.Intent
import android.widget.Toast
import com.dotnotes.app.BuildConfig
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dotnotes.app.DotNotesApp
import com.dotnotes.app.R
import com.dotnotes.app.sync.supabase.SyncResult
import com.dotnotes.app.ui.i18n.LocalStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val authUser by viewModel.authUserState.collectAsState()
    val isLoggingIn by viewModel.isLoggingIn.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val lastSyncTime by viewModel.lastSyncTime.collectAsState()
    val reminderSoundUri by viewModel.reminderSoundUri.collectAsState()
    val reminderSoundTitle by viewModel.reminderSoundTitle.collectAsState()
    val alarmSoundUri by viewModel.alarmSoundUri.collectAsState()
    val alarmSoundTitle by viewModel.alarmSoundTitle.collectAsState()
    
    val context = LocalContext.current
    var showThemeDialog by remember { mutableStateOf(false) }
    var showSnoozeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var signInError by remember { mutableStateOf<String?>(null) }

    val reminderSoundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            val title = if (uri != null) {
                RingtoneManager.getRingtone(context, uri)?.getTitle(context) ?: strings.defaultSound
            } else {
                strings.defaultSound
            }
            viewModel.setReminderSound(uri?.toString().orEmpty(), title)
        }
    }

    val alarmSoundPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val uri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            val title = if (uri != null) {
                RingtoneManager.getRingtone(context, uri)?.getTitle(context) ?: strings.defaultSound
            } else {
                strings.defaultSound
            }
            viewModel.setAlarmSound(uri?.toString().orEmpty(), title)
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleGoogleSignInResult(context, result.data) { success, err ->
            if (success) {
                Toast.makeText(context, strings.signInWithGoogle, Toast.LENGTH_SHORT).show()
            } else if (err != null) {
                signInError = err
                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.settings, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 24.dp)
        ) {
            // ==========================================
            // 1. AKUN GOOGLE (PALING ATAS)
            // ==========================================
            SectionHeader(title = strings.googleAccount)

            if (!authUser.isLoggedIn) {
                // Not Logged In Card with Google Branding
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.outlinedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_google_logo),
                                contentDescription = "Google",
                                modifier = Modifier.size(24.dp),
                                tint = Color.Unspecified
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = strings.googleAccount,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = strings.googleSignInPrompt,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(Modifier.height(14.dp))

                        // Custom Styled Google Sign-In Button
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(enabled = !isLoggingIn) {
                                    try {
                                        googleSignInLauncher.launch(viewModel.getGoogleSignInIntent(context))
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp, horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                if (isLoggingIn) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = strings.signingIn,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_google_logo),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                        tint = Color.Unspecified
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        text = strings.signInWithGoogle,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Logged In Card with Profile
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        if (!authUser.avatarUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = authUser.avatarUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                val initial = authUser.displayName?.firstOrNull()?.uppercase()
                                    ?: authUser.email?.firstOrNull()?.uppercase()
                                    ?: "U"
                                Text(
                                    text = initial,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }

                        Spacer(Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = authUser.displayName ?: strings.googleAccount,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            if (!authUser.email.isNullOrBlank()) {
                                Text(
                                    text = authUser.email!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }

                        TextButton(onClick = {
                            viewModel.signOut(context) {
                                Toast.makeText(context, strings.signOut, Toast.LENGTH_SHORT).show()
                            }
                        }) {
                            Text(
                                text = strings.signOut,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // ==========================================
            // 2. PREFERENSI APLIKASI
            // ==========================================
            SectionHeader(title = strings.preferences)

            // Language
            ListItem(
                leadingContent = {
                    Icon(Icons.Default.Language, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                headlineContent = { Text(strings.language) },
                supportingContent = { Text(if (language == "id") strings.indonesian else strings.english) },
                modifier = Modifier.clickable { showLanguageDialog = true }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Theme
            ListItem(
                leadingContent = {
                    Icon(Icons.Default.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
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
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Snooze Duration
            ListItem(
                leadingContent = {
                    Icon(Icons.Default.Snooze, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                headlineContent = { Text(strings.snoozeDuration) },
                supportingContent = { Text("$snoozeDuration ${strings.minutes}") },
                modifier = Modifier.clickable { showSnoozeDialog = true }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Reminder Sound
            ListItem(
                leadingContent = {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                headlineContent = { Text(strings.reminderSound) },
                supportingContent = { Text(reminderSoundTitle) },
                modifier = Modifier.clickable {
                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, strings.reminderSound)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                        val currentUri = reminderSoundUri.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }
                            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentUri)
                    }
                    reminderSoundPickerLauncher.launch(intent)
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Alarm Sound
            ListItem(
                leadingContent = {
                    Icon(Icons.Default.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                headlineContent = { Text(strings.alarmSound) },
                supportingContent = { Text(alarmSoundTitle) },
                modifier = Modifier.clickable {
                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, strings.alarmSound)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false)
                        val currentUri = alarmSoundUri.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }
                            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                        putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, currentUri)
                    }
                    alarmSoundPickerLauncher.launch(intent)
                }
            )

            Spacer(Modifier.height(16.dp))

            // ==========================================
            // 3. TENTANG & PEMBARUAN
            // ==========================================
            SectionHeader(title = strings.aboutAndUpdates)

            // Update Notification Banner (jika update ditemukan)
            if (availableUpdate != null) {
                val update = availableUpdate!!
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Default.NewReleases,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${strings.updateAvailable} (${update.tagName})",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.weight(1f))
                            Badge(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            ) {
                                Text(strings.newBadge, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (update.changelog.isNotBlank()) {
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = update.changelog,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                maxLines = 2
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.downloadAndInstall(context, update) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(strings.updateNow)
                        }
                    }
                }
            }

            // Check for Updates item
            ListItem(
                leadingContent = {
                    Box(
                        modifier = Modifier.size(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.SystemUpdate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        if (availableUpdate != null) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-2).dp, y = 0.dp)
                                    .size(8.dp)
                                    .background(MaterialTheme.colorScheme.error, CircleShape)
                            )
                        }
                    }
                },
                headlineContent = { Text(strings.checkForUpdates) },
                supportingContent = {
                    if (availableUpdate != null) {
                        Text(
                            text = "${strings.updateAvailable} (${availableUpdate!!.tagName})",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    } else {
                        Text(strings.checkForUpdatesDesc)
                    }
                },
                trailingContent = {
                    if (isCheckingUpdate) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else if (availableUpdate != null) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ) {
                            Text(strings.newBadge, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                modifier = Modifier.clickable(enabled = !isCheckingUpdate && downloadProgress == null) {
                    viewModel.checkForUpdate(BuildConfig.VERSION_NAME) {
                        Toast.makeText(context, strings.alreadyLatest, Toast.LENGTH_SHORT).show()
                    }
                }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // About item
            ListItem(
                leadingContent = {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                headlineContent = { Text(strings.about) },
                supportingContent = { Text("dotnotes v${BuildConfig.VERSION_NAME}") }
            )
        }
    }

    // ==========================================
    // DIALOGS
    // ==========================================
    // Sign-In Error Dialog
    if (signInError != null) {
        AlertDialog(
            onDismissRequest = { signInError = null },
            shape = RoundedCornerShape(28.dp),
            title = { Text("Google Sign-In Error", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text(
                    text = signInError ?: "",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(onClick = { signInError = null }) {
                    Text("OK")
                }
            }
        )
    }
    // Language Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            shape = RoundedCornerShape(28.dp),
            title = { Text(strings.language, style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    listOf("en" to strings.english, "id" to strings.indonesian).forEach { (code, name) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.setLanguage(code)
                                    showLanguageDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            RadioButton(
                                selected = language == code,
                                onClick = {
                                    viewModel.setLanguage(code)
                                    showLanguageDialog = false
                                }
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(name, style = MaterialTheme.typography.bodyLarge)
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
            shape = RoundedCornerShape(28.dp),
            title = { Text(strings.theme, style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    listOf(
                        "system" to strings.themeSystem,
                        "light" to strings.themeLight,
                        "dark" to strings.themeDark
                    ).forEach { (mode, label) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge)
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
            shape = RoundedCornerShape(28.dp),
            title = { Text(strings.snoozeDuration, style = MaterialTheme.typography.titleLarge) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    listOf(5, 10, 15).forEach { minutes ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.setSnoozeDuration(minutes)
                                    showSnoozeDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        ) {
                            RadioButton(
                                selected = snoozeDuration == minutes,
                                onClick = {
                                    viewModel.setSnoozeDuration(minutes)
                                    showSnoozeDialog = false
                                }
                            )
                            Spacer(Modifier.width(12.dp))
                            Text("$minutes ${strings.minutes}", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Update Progress / Confirmation Dialog
    availableUpdate?.let { update ->
        if (downloadProgress != null) {
            AlertDialog(
                onDismissRequest = {},
                shape = RoundedCornerShape(28.dp),
                title = { Text("${strings.updateAvailable} (${update.tagName})", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Column {
                        Text(strings.downloadingUpdate, style = MaterialTheme.typography.bodyMedium)
                        Spacer(Modifier.height(16.dp))
                        val progress = downloadProgress ?: 0f
                        if (progress > 0f) {
                            LinearProgressIndicator(
                                progress = { progress.coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                },
                confirmButton = {}
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 0.5.sp
        ),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
