package com.dotnotes.app.sync.supabase

import com.dotnotes.app.BuildConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.engine.okhttp.OkHttp

object SupabaseClientProvider {
    val isConfigured: Boolean
        get() = BuildConfig.SUPABASE_URL.isNotBlank() && BuildConfig.SUPABASE_ANON_KEY.isNotBlank()

    val client: SupabaseClient by lazy {
        val url = if (isConfigured) BuildConfig.SUPABASE_URL else "https://placeholder.supabase.co"
        val key = if (isConfigured) BuildConfig.SUPABASE_ANON_KEY else "placeholder-key"
        createSupabaseClient(
            supabaseUrl = url,
            supabaseKey = key
        ) {
            httpEngine = OkHttp.create()
            install(Auth) {
                scheme = "com.dotnotes.app"
                host = "auth"
                defaultExternalAuthAction = io.github.jan.supabase.auth.ExternalAuthAction.CustomTabs()
            }
            install(Postgrest)
        }
    }

    val auth: Auth
        get() = client.auth

    val postgrest: Postgrest
        get() = client.postgrest
}
