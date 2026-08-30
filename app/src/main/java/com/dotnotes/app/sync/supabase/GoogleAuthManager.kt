package com.dotnotes.app.sync.supabase

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.dotnotes.app.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import kotlinx.coroutines.CancellationException
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
    private val credentialManager = CredentialManager.create(context)
    private val supabase = SupabaseClientProvider.client

    val userState: Flow<AuthUserState> = supabase.auth.sessionStatus.map {
        val user = supabase.auth.currentUserOrNull()
        if (user != null) {
            val metadata = user.userMetadata
            val name = metadata?.get("full_name")?.toString()?.trim('\"')
                ?: metadata?.get("name")?.toString()?.trim('\"')
                ?: metadata?.get("user_name")?.toString()?.trim('\"')
            val avatar = metadata?.get("avatar_url")?.toString()?.trim('\"')
                ?: metadata?.get("picture")?.toString()?.trim('\"')
                ?: metadata?.get("avatar")?.toString()?.trim('\"')
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

    suspend fun signInWithGoogle(): Result<AuthUserState> {
        if (!SupabaseClientProvider.isConfigured || BuildConfig.GOOGLE_WEB_CLIENT_ID.isBlank()) {
            return Result.failure(IllegalStateException("Supabase URL, Anon Key, atau Google Web Client ID belum terpasang"))
        }

        val targetActivity = context.findActivity() ?: (context as? Activity)
        val reqContext = targetActivity ?: context

        // 1. Coba GetGoogleIdOption (Standar Native Android 14, 15, 16)
        val primaryResult = executeCredentialRequest(reqContext, isLegacy = false)
        if (primaryResult.isSuccess) {
            return primaryResult
        }

        val primaryEx = primaryResult.exceptionOrNull()
        if (isUserCancellation(primaryEx)) {
            return Result.success(AuthUserState(isLoggedIn = false))
        }

        // 2. Fallback ke GetSignInWithGoogleOption (Google Play Services)
        val fallbackResult = executeCredentialRequest(reqContext, isLegacy = true)
        if (fallbackResult.isSuccess) {
            return fallbackResult
        }

        val fallbackEx = fallbackResult.exceptionOrNull()
        if (isUserCancellation(fallbackEx)) {
            return Result.success(AuthUserState(isLoggedIn = false))
        }

        // 3. Fallback ke Supabase OAuth Browser Flow jika Credential Manager ditolak sistem OS
        return try {
            withContext(Dispatchers.IO) {
                supabase.auth.signInWith(Google)
            }
            val user = supabase.auth.currentUserOrNull()
            Result.success(
                AuthUserState(
                    isLoggedIn = user != null,
                    email = user?.email,
                    displayName = user?.userMetadata?.get("full_name")?.toString()?.trim('\"'),
                    avatarUrl = user?.userMetadata?.get("avatar_url")?.toString()?.trim('\"')
                )
            )
        } catch (e: Exception) {
            if (isUserCancellation(e)) {
                Result.success(AuthUserState(isLoggedIn = false))
            } else {
                Result.failure(fallbackEx ?: primaryEx ?: e)
            }
        }
    }

    private suspend fun executeCredentialRequest(reqContext: Context, isLegacy: Boolean): Result<AuthUserState> {
        try {
            val requestBuilder = GetCredentialRequest.Builder()
            if (isLegacy) {
                val option = GetSignInWithGoogleOption.Builder(
                    serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
                ).build()
                requestBuilder.addCredentialOption(option)
            } else {
                val option = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                    .setAutoSelectEnabled(false)
                    .build()
                requestBuilder.addCredentialOption(option)
            }

            val response = credentialManager.getCredential(reqContext, requestBuilder.build())
            val credential = response.credential

            var idToken: String? = null
            var displayName: String? = null
            var avatarUrl: String? = null

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                idToken = googleIdTokenCredential.idToken
                displayName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.givenName
                avatarUrl = googleIdTokenCredential.profilePictureUri?.toString()
            } else {
                try {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    idToken = googleIdTokenCredential.idToken
                    displayName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.givenName
                    avatarUrl = googleIdTokenCredential.profilePictureUri?.toString()
                } catch (_: Exception) {
                    idToken = credential.data.getString("androidx.credentials.BUNDLE_KEY_ID_TOKEN")
                }
            }

            if (idToken.isNullOrBlank()) {
                return Result.failure(IllegalStateException("ID Token Google tidak ditemukan"))
            }

            return withContext(Dispatchers.IO) {
                supabase.auth.signInWith(IDToken) {
                    this.idToken = idToken
                    this.provider = Google
                }

                val user = supabase.auth.currentUserOrNull()
                val finalAvatar = avatarUrl
                    ?: user?.userMetadata?.get("avatar_url")?.toString()?.trim('\"')
                    ?: user?.userMetadata?.get("picture")?.toString()?.trim('\"')

                Result.success(
                    AuthUserState(
                        isLoggedIn = true,
                        email = user?.email,
                        displayName = displayName ?: user?.userMetadata?.get("full_name")?.toString()?.trim('\"'),
                        avatarUrl = finalAvatar
                    )
                )
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    private fun isUserCancellation(e: Throwable?): Boolean {
        if (e == null) return false
        if (e is GetCredentialCancellationException || e is CancellationException) return true
        val msg = e.message ?: ""
        val name = e.javaClass.simpleName
        return (msg.contains("cancel", ignoreCase = true) ||
                msg.contains("batal", ignoreCase = true) ||
                name.contains("Cancel", ignoreCase = true)) &&
                !msg.contains("ditolak", ignoreCase = true)
    }

    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            supabase.auth.signOut()
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun Context.findActivity(): Activity? {
        var current = this
        while (current is ContextWrapper) {
            if (current is Activity) return current
            current = current.baseContext
        }
        return null
    }
}
