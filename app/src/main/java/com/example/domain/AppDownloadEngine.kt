package com.example.domain

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AppDownloadEngine(
    private val context: Context,
    private val toolEngine: ToolExecutionEngine
) {

    /**
     * 1. Search and open Google Play Store for an app
     */
    suspend fun downloadFromPlayStore(appName: String): String = withContext(Dispatchers.Main) {
        val query = appName.trim()
        val marketUri = Uri.parse("market://search?q=${Uri.encode(query)}")
        val webUri = Uri.parse("https://play.google.com/store/search?q=${Uri.encode(query)}&c=apps")

        val intent = Intent(Intent.ACTION_VIEW, marketUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return@withContext try {
            context.startActivity(intent)
            "Opening Play Store to download '$query' for you, Boss!"
        } catch (e: Exception) {
            // Fallback to browser
            val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            "Opening Google Play in your browser to download '$query', Boss."
        }
    }

    /**
     * 2. Direct APK URL Downloader & Installer
     */
    suspend fun downloadAndInstallApk(apkUrl: String, apkName: String = "App"): String = withContext(Dispatchers.IO) {
        if (!apkUrl.startsWith("http")) {
            return@withContext "Please provide a valid download link, Boss."
        }

        val fileName = "${apkName.replace(" ", "_")}_${System.currentTimeMillis()}.apk"

        val request = DownloadManager.Request(Uri.parse(apkUrl)).apply {
            setTitle("Downloading $apkName")
            setDescription("Wife Assistant is downloading APK...")
            setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
            setMimeType("application/vnd.android.package-archive")
            setAllowedOverMetered(true)
        }

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)

        // Register receiver to prompt install when download finishes
        val onComplete = object : BroadcastReceiver() {
            override fun onReceive(ctxt: Context?, intent: Intent?) {
                val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
                if (id == downloadId) {
                    val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
                    if (file.exists()) {
                        promptApkInstallation(file)
                    }
                    context.unregisterReceiver(this)
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE), Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(onComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        }

        return@withContext "I started downloading the APK for $apkName. I'll prompt you to install it once it's done, Boss!"
    }

    private fun promptApkInstallation(file: File) {
        val apkUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(installIntent)
    }

    /**
     * 3. Remote PC Software Installer (via winget)
     */
    suspend fun downloadSoftwareOnPc(softwareName: String): String {
        val wingetCmd = "winget install --id $softwareName -e --silent --accept-package-agreements --accept-source-agreements"
        toolEngine.sendPcRemoteCommand("system", wingetCmd)
        return "Initiated remote installation of '$softwareName' on your PC via Windows Package Manager, Boss!"
    }
}
