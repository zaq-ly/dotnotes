package com.dotnotes.app.sync.supabase

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.dotnotes.app.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
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

    suspend fun signInWithGoogle(): Result<Unit> {
        try {
            if (!SupabaseClientProvider.isConfigured) {
                return Result.failure(IllegalStateException("Supabase URL atau Anon Key belum terpasang"))
            }

            val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
            if (webClientId.isBlank()) {
                return Result.failure(IllegalStateException("GOOGLE_WEB_CLIENT_ID belum terpasang"))
            }

            // 1. Native Google Credential Manager (Displays all device Google accounts natively)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(context)
            val result = credentialManager.getCredential(context, request)
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val idToken = googleIdTokenCredential.idToken

            // 2. Authenticate with Supabase via IDToken
            withContext(Dispatchers.IO) {
                supabase.auth.signInWith(IDToken) {
                    this.idToken = idToken
                    provider = Google
                }
            }

            return Result.success(Unit)
        } catch (_: GetCredentialCancellationException) {
            return Result.failure(kotlinx.coroutines.CancellationException("User cancelled login"))
        } catch (e: Exception) {
            Log.e(TAG, "Native Google Credential Manager login failed", e)
            return Result.failure(e)
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
