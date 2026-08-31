package com.dotnotes.app.sync.supabase

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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

    suspend fun signInWithGoogle(): Result<Unit> = withContext(Dispatchers.Main) {
        try {
            if (!SupabaseClientProvider.isConfigured) {
                return@withContext Result.failure(IllegalStateException("Supabase URL atau Anon Key belum terpasang"))
            }

            val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
            if (webClientId.isBlank()) {
                return@withContext Result.failure(IllegalStateException("GOOGLE_WEB_CLIENT_ID belum terpasang"))
            }

            val activityContext = context.findActivity() ?: context

            val rawNonce = java.util.UUID.randomUUID().toString()
            val md = java.security.MessageDigest.getInstance("SHA-256")
            val digest = md.digest(rawNonce.toByteArray())
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            // 1. Pure Native Google Credential Manager (0% Browser)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setNonce(hashedNonce)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialManager = CredentialManager.create(activityContext)
            val result = credentialManager.getCredential(activityContext, request)
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val idToken = googleIdTokenCredential.idToken

            // 2. Authenticate directly with Supabase via IDToken
            withContext(Dispatchers.IO) {
                supabase.auth.signInWith(IDToken) {
                    this.idToken = idToken
                    this.nonce = rawNonce
                    provider = Google
                }
            }

            return@withContext Result.success(Unit)
        } catch (_: GetCredentialCancellationException) {
            Log.d(TAG, "User cancelled Google Sign-In dialog")
            return@withContext Result.failure(kotlinx.coroutines.CancellationException("User cancelled login"))
        } catch (e: GetCredentialException) {
            Log.e(TAG, "Credential Manager error type: ${e.type}, message: ${e.message}", e)
            return@withContext Result.failure(Exception("Google Sign-In gagal [${e.type}]: ${e.message}", e))
        } catch (e: Exception) {
            Log.e(TAG, "Google login failed", e)
            return@withContext Result.failure(e)
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

        private fun Context.findActivity(): Activity? {
            var currentContext = this
            while (currentContext is ContextWrapper) {
                if (currentContext is Activity) {
                    return currentContext
                }
                currentContext = currentContext.baseContext
            }
            return null
        }
    }
}
