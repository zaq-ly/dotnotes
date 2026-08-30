package com.dotnotes.app.sync.supabase

import android.content.Context
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
            val avatar = metadata?.get("avatar_url")?.toString()?.trim('\"')
                ?: metadata?.get("picture")?.toString()?.trim('\"')
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
        try {
            if (!SupabaseClientProvider.isConfigured || BuildConfig.GOOGLE_WEB_CLIENT_ID.isBlank()) {
                return Result.failure(IllegalStateException("Supabase URL, Anon Key, or Google Web Client ID not configured"))
            }

            val rawNonce = UUID.randomUUID().toString()
            val bytes = rawNonce.toByteArray()
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest(bytes)
            val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

            val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(
                serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
            )
                .setNonce(hashedNonce)
                .build()

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                .setNonce(hashedNonce)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(signInWithGoogleOption)
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(context, request)
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
                    Result.success(
                        AuthUserState(
                            isLoggedIn = true,
                            email = user?.email,
                            displayName = googleIdTokenCredential.displayName ?: googleIdTokenCredential.givenName,
                            avatarUrl = googleIdTokenCredential.profilePictureUri?.toString()
                        )
                    )
                }
            } else {
                return Result.failure(IllegalStateException("Unsupported credential type"))
            }
        } catch (e: GetCredentialCancellationException) {
            return Result.failure(e)
        } catch (e: Exception) {
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
}
