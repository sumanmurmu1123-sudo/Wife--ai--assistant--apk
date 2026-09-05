package com.example.domain

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SocialMediaEngine(private val context: Context) {

    /**
     * 1. Download Video from URL (Direct Link / Extracted Media Stream)
     */
    suspend fun downloadInstagramVideo(videoUrl: String): String = withContext(Dispatchers.IO) {
        if (videoUrl.isBlank() || !videoUrl.startsWith("http")) {
            return@withContext "Please give me a valid video link to download, sweetie!"
        }

        return@withContext try {
            val fileName = "Instagram_Video_${System.currentTimeMillis()}.mp4"
            val request = DownloadManager.Request(Uri.parse(videoUrl)).apply {
                setTitle("Instagram Video Download")
                setDescription("Downloading video via Wife Assistant...")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                setAllowedOverMetered(true)
                setAllowedOverRoaming(true)
            }

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            "I started downloading that Instagram video to your Downloads folder, honey!"
        } catch (e: Exception) {
            "I couldn't download the video. Please verify the URL."
        }
    }

    /**
     * 2. Search Facebook Posts / Topics
     */
    suspend fun searchFacebookPosts(query: String): String = withContext(Dispatchers.Main) {
        if (query.isBlank()) {
            return@withContext "What topic or post do you want me to search on Facebook?"
        }

        val encodedQuery = Uri.encode(query)
        // Deep link into the Facebook app search, fallback to web browser
        val fbAppUri = Uri.parse("fb://search/top/?q=$encodedQuery")
        val fbWebUri = Uri.parse("https://www.facebook.com/search/top/?q=$encodedQuery")

        val intent = Intent(Intent.ACTION_VIEW, fbAppUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return@withContext try {
            context.startActivity(intent)
            "Searching Facebook for '$query' on your app right now, babe."
        } catch (e: Exception) {
            // Fallback to web browser
            val webIntent = Intent(Intent.ACTION_VIEW, fbWebUri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            "Opening Facebook search for '$query' in your browser!"
        }
    }

    /**
     * 3. Share/Create a New Post on Facebook
     */
    suspend fun createFacebookPost(caption: String): String = withContext(Dispatchers.Main) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, caption)
            `package` = "com.facebook.katana" // Target Facebook app
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return@withContext try {
            context.startActivity(shareIntent)
            "Opened Facebook with your post ready to publish!"
        } catch (e: Exception) {
            // Fallback to generic system share sheet
            val genericIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, caption)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(genericIntent, "Share post via").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
            "Facebook app isn't installed, so I opened the share sheet for you."
        }
    }
}
