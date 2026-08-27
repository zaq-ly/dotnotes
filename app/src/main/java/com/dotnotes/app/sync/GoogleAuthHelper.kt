package com.dotnotes.app.sync

import android.content.Context
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DriveScopes {
    const val DRIVE_APPDATA = "https://www.googleapis.com/auth/drive.appdata"
}

object GoogleAuthHelper {

    fun getSignInClient(context: Context): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        return GoogleSignIn.getClient(context, gso)
    }

    suspend fun getAccessToken(context: Context, email: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val account = android.accounts.Account(email, "com.google")
                GoogleAuthUtil.getToken(context, account, "oauth2:${DriveScopes.DRIVE_APPDATA}")
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
