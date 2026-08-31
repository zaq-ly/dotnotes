package com.dotnotes.app.sync.supabase

import android.content.Context
import android.content.Intent
import android.util.Log
import com.dotnotes.app.BuildConfig
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
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

    fun getSignInIntent(): Intent {
        val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(context, gso)
        client.signOut()
        return client.signInIntent
    }

    suspend fun handleSignInResult(data: Intent?): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            val idToken = account.idToken ?: return@withContext Result.failure(IllegalStateException("Gagal mendapatkan ID Token dari Google"))

            Log.d(TAG, "Google ID Token received successfully. Authenticating with Supabase...")

            supabase.auth.signInWith(IDToken) {
                this.idToken = idToken
                provider = Google
            }

            Result.success(Unit)
        } catch (e: ApiException) {
            Log.e(TAG, "Google Sign-In ApiException statusCode=${e.statusCode}", e)
            Result.failure(Exception("Google Sign-In error (Status Code ${e.statusCode}): ${e.localizedMessage ?: e.message}"))
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed", e)
            Result.failure(e)
        }
    }

    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
            GoogleSignIn.getClient(context, gso).signOut()
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
