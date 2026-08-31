package com.dotnotes.app

import android.Manifest
import android.app.AlarmManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.dotnotes.app.sync.supabase.SupabaseClientProvider
import com.dotnotes.app.ui.i18n.EnglishStrings
import com.dotnotes.app.ui.i18n.IndonesianStrings
import com.dotnotes.app.ui.i18n.LocalStrings
import com.dotnotes.app.ui.navigation.NavGraph
import com.dotnotes.app.ui.navigation.Routes
import com.dotnotes.app.ui.theme.DotNotesTheme
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.handleDeeplinks

class MainActivity : ComponentActivity() {
    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        checkExactAlarmPermission()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        SupabaseClientProvider.client.handleDeeplinks(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SupabaseClientProvider.client.handleDeeplinks(intent)
        enableEdgeToEdge()
        requestNotificationPermission()

        setContent {
            val settingsDataStore = DotNotesApp.instance.settingsDataStore
            val themeMode by settingsDataStore.themeMode.collectAsState(initial = "system")
            val language by settingsDataStore.language.collectAsState(initial = "en")

            val isDark = when (themeMode) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            val strings = remember(language) {
                if (language == "id") IndonesianStrings else EnglishStrings
            }

            CompositionLocalProvider(LocalStrings provides strings) {
                DotNotesTheme(darkTheme = isDark) {
                    val navController = rememberNavController()
                    LaunchedEffect(Unit) {
                        if (intent?.getBooleanExtra("OPEN_SETTINGS", false) == true) {
                            navController.navigate(Routes.SETTINGS)
                        }
                    }
                    NavGraph(navController = navController)
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                checkExactAlarmPermission()
            }
        } else {
            checkExactAlarmPermission()
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                }
                try {
                    startActivity(intent)
                } catch (_: Exception) {
                }
            }
        }
    }
}
