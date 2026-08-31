package com.dotnotes.app.sync.supabase

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import com.dotnotes.app.BuildConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class AuthUserState(
    val isLoggedIn: Boolean = false,
    val email: String? = null,
    val displayName: String? = null,
    val avatarUrl: String? = null
)

class GoogleAuthManager(private val context: Context) {
    private val supabase = SupabaseClientProvider.client

    val userState: Flow<AuthUserState> = supabase.auth.sessionStatus.map {
        val user = supabase.auth.currentUserOrNull()
        if (user != null) {
            val metadata = user.userMetadata
            val name = metadata?.get("full_name")?.toString()?.trim('"')
                ?: metadata?.get("name")?.toString()?.trim('"')
                ?: metadata?.get("user_name")?.toString()?.trim('"')
            val avatar = metadata?.get("avatar_url")?.toString()?.trim('"')
                ?: metadata?.get("picture")?.toString()?.trim('"')
                ?: metadata?.get("avatar")?.toString()?.trim('"')
            AuthUserState(
                isLoggedIn = true,
                email = user.email,
                displayName = name,
                avatarUrl = avatar
            )
        } else {
            AuthUserState(isLoggedIn = false)
        }
    }

    suspend fun signInWithGoogle(): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            if (!SupabaseClientProvider.isConfigured) {
                return@withContext Result.failure(IllegalStateException("Supabase URL atau Anon Key belum terpasang"))
            }

            // 1. Generate Google OAuth URL with prompt=select_account and redirect to app
            val redirectUrl = "com.dotnotes.app://auth"
            val oAuthUrl = withContext(Dispatchers.IO) {
                supabase.auth.getOAuthUrl(
                    provider = Google,
                    redirectUrl = redirectUrl
                ) {
                    queryParams["prompt"] = "select_account"
                }
            }

            Log.d(TAG, "Launching OAuth URL via Chrome Custom Tabs: $oAuthUrl")

            // 2. Build seamless Custom Tabs intent
            val customTabsIntent = CustomTabsIntent.Builder()
                .setShowTitle(false)
                .setUrlBarHidingEnabled(true)
                .setShareState(CustomTabsIntent.SHARE_STATE_OFF)
                .setToolbarCornerRadiusDp(16)
                .build()

            val intent = customTabsIntent.intent
            intent.data = Uri.parse(oAuthUrl)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            // 3. Lock package to Google Chrome so accounts on device appear and no browser chooser is shown
            intent.setPackage("com.android.chrome")

            try {
                context.startActivity(intent)
            } catch (_: Exception) {
                // Fallback to system Custom Tabs if Chrome is not installed
                intent.setPackage(null)
                context.startActivity(intent)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Google OAuth failed", e)
            Result.failure(e)
        }
    }

    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "GoogleAuthManager"
    }
}
