package com.dotnotes.app.sync.supabase

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
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
import java.security.MessageDigest
import java.util.UUID

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

    suspend fun signInWithGoogle(): Result<AuthUserState> {
        if (!SupabaseClientProvider.isConfigured || BuildConfig.GOOGLE_WEB_CLIENT_ID.isBlank()) {
            return Result.failure(IllegalStateException("Supabase URL, Anon Key, atau Google Web Client ID belum terpasang"))
        }

        val activity = context.findActivity()
            ?: return Result.failure(IllegalStateException("Activity context tidak ditemukan"))

        return try {
            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(
                serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
            ).setNonce(hashedNonce).build()

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(activity, request)
            val credential = response.credential

            var idToken: String? = null
            var displayName: String? = null
            var avatarUrl: String? = null

            try {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                idToken = googleIdTokenCredential.idToken
                displayName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.givenName
                avatarUrl = googleIdTokenCredential.profilePictureUri?.toString()
            } catch (e: Exception) {
                Log.w(TAG, "Error parsing GoogleIdTokenCredential", e)
            }

            if (idToken.isNullOrBlank()) {
                idToken = credential.data.getString("androidx.credentials.BUNDLE_KEY_ID_TOKEN")
                    ?: credential.data.getString("id_token")
            }

            if (idToken.isNullOrBlank()) {
                return Result.failure(IllegalStateException("ID Token Google tidak ditemukan dari akun yang dipilih"))
            }

            val finalIdToken = idToken

            withContext(Dispatchers.IO) {
                try {
                    supabase.auth.signInWith(IDToken) {
                        this.idToken = finalIdToken
                        this.provider = Google
                        this.nonce = rawNonce
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Supabase IDToken sign in with rawNonce failed, retrying without nonce...", e)
                    supabase.auth.signInWith(IDToken) {
                        this.idToken = finalIdToken
                        this.provider = Google
                    }
                }
            }

            val user = supabase.auth.currentUserOrNull()
            val finalAvatar = avatarUrl
                ?: user?.userMetadata?.get("avatar_url")?.toString()?.trim('"')
                ?: user?.userMetadata?.get("picture")?.toString()?.trim('"')

            Result.success(
                AuthUserState(
                    isLoggedIn = true,
                    email = user?.email,
                    displayName = displayName ?: user?.userMetadata?.get("full_name")?.toString()?.trim('"'),
                    avatarUrl = finalAvatar
                )
            )
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "User cancelled Google Sign In")
            Result.success(AuthUserState(isLoggedIn = false))
        } catch (e: NoCredentialException) {
            Log.w(TAG, "No Google accounts available: ${e.message}")
            Result.failure(IllegalStateException("Tidak ada akun Google yang tersedia pada perangkat ini"))
        } catch (e: GetCredentialException) {
            if (isUserCancellation(e)) {
                Log.d(TAG, "User cancelled Google Sign In: ${e.message}")
                Result.success(AuthUserState(isLoggedIn = false))
            } else {
                Log.e(TAG, "Credential Manager error: ${e.type} - ${e.message}", e)
                Result.failure(e)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign In failed", e)
            Result.failure(e)
        }
    }

    private fun isUserCancellation(e: Throwable?): Boolean {
        if (e == null) return false
        if (e is GetCredentialCancellationException || e is CancellationException) return true
        val msg = e.message ?: ""
        val name = e.javaClass.simpleName
        return name.contains("Cancel", ignoreCase = true) ||
                msg.contains("canceled", ignoreCase = true) ||
                msg.contains("cancelled", ignoreCase = true)
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

    companion object {
        private const val TAG = "GoogleAuthManager"
    }
}
