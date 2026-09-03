package com.dotnotes.app.update

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.dotnotes.app.DotNotesApp
import com.dotnotes.app.MainActivity
import com.dotnotes.app.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class ReleaseInfo(
    val tagName: String,
    val versionName: String,
    val changelog: String,
    val apkDownloadUrl: String
)

class UpdateManager {

    suspend fun checkForUpdate(currentVersion: String): ReleaseInfo? {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL("https://api.github.com/repos/zaq-ly/dotnotes/releases/latest")
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                connection.setRequestProperty("User-Agent", "dotnotes-app")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000

                if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                    return@withContext null
                }

                val response = InputStreamReader(connection.inputStream).use { it.readText() }
                val json = JSONObject(response)
                val tagName = json.optString("tag_name", "")
                val body = json.optString("body", "")
                val assets = json.optJSONArray("assets")

                var apkUrl: String? = null
                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            apkUrl = asset.optString("browser_download_url")
                            break
                        }
                    }
                }

                val cleanRemote = tagName.removePrefix("v").removePrefix("V")
                val cleanCurrent = currentVersion.removePrefix("v").removePrefix("V")

                if (isNewerVersion(cleanRemote, cleanCurrent) && !apkUrl.isNullOrBlank()) {
                    ReleaseInfo(
                        tagName = tagName,
                        versionName = cleanRemote,
                        changelog = body,
                        apkDownloadUrl = apkUrl
                    )
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    suspend fun downloadApk(
        context: Context,
        apkUrl: String,
        onProgress: (Float) -> Unit
    ): File? {
        return withContext(Dispatchers.IO) {
            try {
                var targetUrl = apkUrl
                var connection: HttpURLConnection
                var redirectCount = 0

                while (true) {
                    val u = URL(targetUrl)
                    connection = u.openConnection() as HttpURLConnection
                    connection.setRequestProperty("User-Agent", "dotnotes-app")
                    connection.instanceFollowRedirects = true
                    connection.connectTimeout = 15000
                    connection.readTimeout = 30000

                    val code = connection.responseCode
                    if (code in 301..308 && redirectCount < 5) {
                        val location = connection.getHeaderField("Location")
                        if (!location.isNullOrBlank()) {
                            targetUrl = location
                            redirectCount++
                            continue
                        }
                    }
                    break
                }

                val totalLength = connection.contentLength
                val cacheDir = context.externalCacheDir ?: context.cacheDir
                val destination = File(cacheDir, "dotnotes_update.apk")
                if (destination.exists()) destination.delete()

                connection.inputStream.use { input ->
                    FileOutputStream(destination).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        var downloaded = 0L

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloaded += bytesRead
                            if (totalLength > 0) {
                                onProgress(downloaded.toFloat() / totalLength)
                            }
                        }
                        output.flush()
                    }
                }
                destination
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun installApk(context: Context, apkFile: File) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                if (!context.packageManager.canRequestPackageInstalls()) {
                    val settingsIntent = Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                        data = android.net.Uri.parse("package:${context.packageName}")
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(settingsIntent)
                    return
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                apkFile
            )
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/vnd.android.package-archive")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
            }
            context.startActivity(intent)

            // Gracefully move app to background so OS PackageInstaller doesn't kill an active foreground window
            if (context is android.app.Activity) {
                context.moveTaskToBack(true)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun showUpdateNotification(context: Context, release: ReleaseInfo) {
        try {
            val dismissedTag = DotNotesApp.instance.settingsDataStore.dismissedUpdateTag.first()
            if (dismissedTag == release.tagName) {
                // User already dismissed notification for this release version, don't harass
                return
            }

            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("OPEN_SETTINGS", true)
            }
            val openPending = PendingIntent.getActivity(
                context,
                9999,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val dismissIntent = Intent(context, UpdateDismissReceiver::class.java).apply {
                putExtra(UpdateDismissReceiver.EXTRA_TAG_NAME, release.tagName)
            }
            val dismissPending = PendingIntent.getBroadcast(
                context,
                9999,
                dismissIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(context, DotNotesApp.CHANNEL_UPDATE)
                .setSmallIcon(R.drawable.ic_stat_notification)
                .setContentTitle("Pembaruan Tersedia (${release.tagName})")
                .setContentText("Versi terbaru .notes telah tersedia. Ketuk untuk memperbarui.")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(if (release.changelog.isNotBlank()) release.changelog else "Versi terbaru .notes telah tersedia. Ketuk untuk memperbarui.")
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_RECOMMENDATION)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_NONE)
                .setNumber(0)
                .setContentIntent(openPending)
                .setDeleteIntent(dismissPending)
                .setAutoCancel(true)
                .build()

            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                NotificationManagerCompat.from(context).notify(9999, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun isNewerVersion(remote: String, current: String): Boolean {
        val rParts = remote.split(".").mapNotNull { it.toIntOrNull() }
        val cParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val maxLen = maxOf(rParts.size, cParts.size)
        for (i in 0 until maxLen) {
            val r = rParts.getOrElse(i) { 0 }
            val c = cParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }
}
