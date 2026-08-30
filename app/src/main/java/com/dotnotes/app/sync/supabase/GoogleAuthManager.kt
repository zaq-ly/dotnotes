package com.dotnotes.app.sync.supabase

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
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
        if (activity == null) {
            Log.w(TAG, "No Activity context, skipping Credential Manager → browser fallback")
            return signInWithBrowserOAuth()
        }

        // 1. Coba GetSignInWithGoogleOption dulu (paling stabil di semua Android, termasuk 16)
        val signInResult = executeCredentialRequest(activity, useSignInButton = true)
        if (signInResult.isSuccess) return signInResult

        // 2. Fallback ke GetGoogleIdOption (native bottom sheet)
        val nativeResult = executeCredentialRequest(activity, useSignInButton = false)
        if (nativeResult.isSuccess) return nativeResult

        // 3. Cek apakah user yang cancel — jangan fallback ke browser kalau user sengaja cancel
        val lastError = nativeResult.exceptionOrNull()
        if (isUserCancellation(lastError)) {
            Log.d(TAG, "User cancelled sign-in")
            return Result.success(AuthUserState(isLoggedIn = false))
        }

        // 4. Fallback ke Supabase OAuth Browser Flow
        Log.d(TAG, "Credential Manager failed, falling back to browser OAuth. Error: ${lastError?.message}")
        return signInWithBrowserOAuth()
    }

    private suspend fun signInWithBrowserOAuth(): Result<AuthUserState> {
        return try {
            withContext(Dispatchers.IO) {
                supabase.auth.signInWith(Google)
            }
            val user = supabase.auth.currentUserOrNull()
            Result.success(
                AuthUserState(
                    isLoggedIn = user != null,
                    email = user?.email,
                    displayName = user?.userMetadata?.get("full_name")?.toString()?.trim('"'),
                    avatarUrl = user?.userMetadata?.get("avatar_url")?.toString()?.trim('"')
                )
            )
        } catch (e: Exception) {
            if (isUserCancellation(e)) {
                Result.success(AuthUserState(isLoggedIn = false))
            } else {
                Log.e(TAG, "Browser OAuth failed", e)
                Result.failure(e)
            }
        }
    }

    private suspend fun executeCredentialRequest(activity: Activity, useSignInButton: Boolean): Result<AuthUserState> {
        try {
            val nonce = generateNonce()
            val requestBuilder = GetCredentialRequest.Builder()

            if (useSignInButton) {
                val option = GetSignInWithGoogleOption.Builder(
                    serverClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
                ).setNonce(nonce).build()
                requestBuilder.addCredentialOption(option)
            } else {
                val option = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                    .setAutoSelectEnabled(false)
                    .setNonce(nonce)
                    .build()
                requestBuilder.addCredentialOption(option)
            }

            // PENTING: harus Activity context, bukan Application context
            val response = credentialManager.getCredential(activity, requestBuilder.build())
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
            }
        } catch (e: NoCredentialException) {
            Log.d(TAG, "NoCredentialException (useSignInButton=$useSignInButton): ${e.message}")
            return Result.failure(e)
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "User cancelled (useSignInButton=$useSignInButton)")
            return Result.failure(e)
        } catch (e: GetCredentialException) {
            Log.w(TAG, "GetCredentialException (useSignInButton=$useSignInButton): ${e.type} - ${e.message}")
            return Result.failure(e)
        } catch (e: CancellationException) {
            throw e // jangan swallow coroutine cancellation
        } catch (e: Exception) {
            Log.w(TAG, "Credential request failed (useSignInButton=$useSignInButton)", e)
            return Result.failure(e)
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

    private fun generateNonce(): String {
        val bytes = UUID.randomUUID().toString().toByteArray()
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.joinToString("") { "%02x".format(it) }
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
