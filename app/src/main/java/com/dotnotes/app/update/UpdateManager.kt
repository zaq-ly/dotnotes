package com.dotnotes.app.update

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
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
