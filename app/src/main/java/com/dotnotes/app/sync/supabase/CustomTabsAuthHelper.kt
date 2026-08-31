package com.dotnotes.app.sync.supabase

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build

object CustomTabsAuthHelper {
    private val preferredPackages = listOf(
        "com.android.chrome",
        "com.chrome.beta",
        "com.sec.android.app.sbrowser",
        "com.microsoft.emmx",
        "com.brave.browser",
        "org.mozilla.firefox"
    )

    fun getBestCustomTabsPackage(context: Context): String? {
        try {
            val pm = context.packageManager
            val activityIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://accounts.google.com"))
            val resolvedActivities = pm.queryIntentActivities(
                activityIntent,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PackageManager.MATCH_ALL else 0
            )

            val packagesSupportingCustomTabs = mutableListOf<String>()
            for (info in resolvedActivities) {
                val serviceIntent = Intent("android.support.customtabs.action.CustomTabsService").apply {
                    setPackage(info.activityInfo.packageName)
                }
                if (pm.resolveService(serviceIntent, 0) != null) {
                    packagesSupportingCustomTabs.add(info.activityInfo.packageName)
                }
            }

            for (preferred in preferredPackages) {
                if (packagesSupportingCustomTabs.contains(preferred)) {
                    return preferred
                }
            }

            return packagesSupportingCustomTabs.firstOrNull()
                ?: resolvedActivities.firstOrNull()?.activityInfo?.packageName
        } catch (_: Exception) {
            return "com.android.chrome"
        }
    }
}
