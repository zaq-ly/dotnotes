package com.dotnotes.app.sync.supabase

import android.content.Context
import android.util.Log
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

    suspend fun signInWithGoogle(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!SupabaseClientProvider.isConfigured) {
                return@withContext Result.failure(IllegalStateException("Supabase URL atau Anon Key belum terpasang"))
            }

            Log.d(TAG, "Launching Supabase Google OAuth via CustomTabs with account picker prompt...")
            supabase.auth.signInWith(Google) {
                queryParams["prompt"] = "select_account"
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
