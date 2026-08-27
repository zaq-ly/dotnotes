package com.dotnotes.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.dotnotes.app.ui.i18n.EnglishStrings
import com.dotnotes.app.ui.i18n.IndonesianStrings
import com.dotnotes.app.ui.i18n.LocalStrings
import com.dotnotes.app.ui.navigation.NavGraph
import com.dotnotes.app.ui.theme.DotNotesTheme

class MainActivity : ComponentActivity() {
    private val notifPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* granted or not, app works either way */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            }
        }
    }
}
