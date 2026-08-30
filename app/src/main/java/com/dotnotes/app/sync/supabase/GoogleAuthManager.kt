package com.dotnotes.app.sync.supabase

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
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

        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build()

            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(
                serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
            ).build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .addCredentialOption(signInWithGoogleOption)
                .build()

            val targetActivity = context.findActivity() ?: (context as? Activity)
            val reqContext = targetActivity ?: context
            val response = credentialManager.getCredential(reqContext, request)
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
                return Result.failure(IllegalStateException("Gagal mendapatkan Google ID Token"))
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
        } catch (e: GetCredentialCancellationException) {
            return Result.success(AuthUserState(isLoggedIn = false))
        } catch (e: CancellationException) {
            return Result.success(AuthUserState(isLoggedIn = false))
        } catch (e: Exception) {
            if (e.message?.contains("cancel", ignoreCase = true) == true ||
                e.message?.contains("batal", ignoreCase = true) == true ||
                e.javaClass.simpleName.contains("Cancel", ignoreCase = true)
            ) {
                return Result.success(AuthUserState(isLoggedIn = false))
            }
            return Result.failure(e)
        }
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
