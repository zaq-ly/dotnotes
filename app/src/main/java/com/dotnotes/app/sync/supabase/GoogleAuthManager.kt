package com.dotnotes.app.sync.supabase

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.dotnotes.app.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
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

        val rawNonce = UUID.randomUUID().toString()
        val bytes = rawNonce.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

        try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .setNonce(hashedNonce)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val targetContext = context.findActivity() ?: context
            val response = credentialManager.getCredential(targetContext, request)
            val credential = response.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                return withContext(Dispatchers.IO) {
                    supabase.auth.signInWith(IDToken) {
                        this.idToken = idToken
                        this.provider = Google
                        this.nonce = rawNonce
                    }

                    val user = supabase.auth.currentUserOrNull()
                    val avatar = googleIdTokenCredential.profilePictureUri?.toString()
                        ?: user?.userMetadata?.get("avatar_url")?.toString()?.trim('\"')
                        ?: user?.userMetadata?.get("picture")?.toString()?.trim('\"')

                    Result.success(
                        AuthUserState(
                            isLoggedIn = true,
                            email = user?.email ?: googleIdTokenCredential.id,
                            displayName = googleIdTokenCredential.displayName
                                ?: googleIdTokenCredential.givenName
                                ?: user?.userMetadata?.get("full_name")?.toString()?.trim('\"'),
                            avatarUrl = avatar
                        )
                    )
                }
            } else {
                return Result.failure(IllegalStateException("Tipe kredensial tidak didukung"))
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

    private fun Context.findActivity(): android.app.Activity? {
        var current = this
        while (current is android.content.ContextWrapper) {
            if (current is android.app.Activity) return current
            current = current.baseContext
        }
        return null
    }
}
