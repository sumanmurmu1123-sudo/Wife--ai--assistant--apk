package com.example.domain

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.media.AudioManager
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch

class ToolExecutionEngine(val context: Context) {
    
    val loveStoryEngine = com.example.story.WifeLoveStoryEngine(context)
    val sweetTalkEngine = com.example.persona.WifeSweetTalkEngine(context)
    val attitudeEngine = com.example.persona.WifeAttitudeEngine(context)
    val mistakeEngine = com.example.persona.WifeMistakeEngine(context)
    val antiDrinkEngine = com.example.magic.WifeAntiDrinkEngine(context)
    val girlJealousyEngine = com.example.magic.WifeGirlJealousyEngine(context)
    val jealousEngine = com.example.magic.WifeJealousEngine(context)
    val cyberSecurityEngine = com.example.domain.CyberSecurityEngine()

    init {
        com.example.tools.ToolRegistry.registerTool(com.example.tools.system.FlashlightTool(context))
        com.example.tools.ToolRegistry.registerTool(com.example.tools.system.BatteryStatusTool(context))
        com.example.tools.ToolRegistry.registerTool(com.example.tools.gaming.KillChorTool(context))
        com.example.tools.ToolRegistry.registerTool(com.example.tools.persona.SweetTalkTool(context))
        com.example.tools.ToolRegistry.registerTool(com.example.tools.story.LoveStoryTool(context))
        com.example.tools.ToolRegistry.registerTool(com.example.tools.magic.AntiDrinkTool(antiDrinkEngine))
        com.example.tools.ToolRegistry.registerTool(com.example.tools.persona.AttitudeTool(attitudeEngine))
        com.example.tools.ToolRegistry.registerTool(com.example.tools.persona.MistakeTool(mistakeEngine))
        com.example.tools.ToolRegistry.registerTool(com.example.tools.magic.GirlJealousyTool(girlJealousyEngine))
    }

    val pcRemoteEngine = com.example.domain.security.PcRemoteEngine(context)
    val familyLocationEngine = com.example.domain.security.FamilyLocationEngine(context)
    val lostPhoneDefenseEngine = com.example.domain.security.LostPhoneDefenseEngine(context)
    val digitalMarketingEngine = com.example.domain.DigitalMarketingEngine()

    val guardModeManager = com.example.domain.security.GuardModeManager(context)
    val securityGuard by lazy { com.example.domain.security.SecurityConfirmationGuard(com.example.domain.security.BiometricAuthEngine(context)) }

    suspend fun executeProtectedAction(
        toolName: String,
        actionDesc: String,
        requiresBiometric: Boolean,
        block: suspend () -> String
    ): String {
        return if (securityGuard.isCriticalAction(toolName)) {
            securityGuard.holdForConfirmation(toolName, actionDesc, requiresBiometric, block)
        } else {
            block()
        }
    }
    val intruderCaptureService = com.example.domain.security.IntruderCaptureService(context, lostPhoneDefenseEngine)
    val voiceGuardianFilter = com.example.domain.security.VoiceGuardianFilter(guardModeManager, intruderCaptureService)

    val masterPinAuthEngine = com.example.domain.security.MasterPinAuthEngine(
        context,
        pcRemoteEngine,
        familyLocationEngine,
        lostPhoneDefenseEngine
    )

    private val _isSocialModeActive = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isSocialModeActive: kotlinx.coroutines.flow.StateFlow<Boolean> = _isSocialModeActive

    private val _activePersona = kotlinx.coroutines.flow.MutableStateFlow(com.example.data.CompanionPersona.GIRLFRIEND)
    val activePersona: kotlinx.coroutines.flow.StateFlow<com.example.data.CompanionPersona> = _activePersona

    fun setPersonaMode(persona: com.example.data.CompanionPersona): String {
        _activePersona.value = persona
        return "Persona switched to ${persona.displayName}"
    }

    fun toggleSocialMode(enable: Boolean): String {
        _isSocialModeActive.value = enable
        return if (enable) {
            "সোশ্যাল মোড চালু করা হয়েছে! এখন আমি সবার সাথে মিষ্টি করে কথা বলব এবং আড্ডা দেব।"
        } else {
            "সোশ্যাল মোড বন্ধ। এখন আমি শুধুই তোমার, Boss! 💕"
        }
    }

    private val _isVideoStudioActive = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isVideoStudioActive: kotlinx.coroutines.flow.StateFlow<Boolean> = _isVideoStudioActive

    private val _isVideoCallActive = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isVideoCallActive: kotlinx.coroutines.flow.StateFlow<Boolean> = _isVideoCallActive

    fun setVideoStudioActive(enable: Boolean): String {
        _isVideoStudioActive.value = enable
        return if (enable) "Opened the Video Edit Studio." else "Closed the Video Edit Studio."
    }

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val _isVoiceCallActive = MutableStateFlow(false)
    val isVoiceCallActive: StateFlow<Boolean> = _isVoiceCallActive

    fun startAiVoiceCall(): String {
        _isVoiceCallActive.value = true
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        audioManager.isSpeakerphoneOn = true
        return "Calling you now, Boss! Let's talk. 💕"
    }

    fun endAiVoiceCall(): String {
        _isVoiceCallActive.value = false
        audioManager.mode = AudioManager.MODE_NORMAL
        return "Call ended, Boss. I'm always right here when you need me!"
    }

    fun toggleSpeaker(enable: Boolean) {
        audioManager.isSpeakerphoneOn = enable
    }

    private val _isAutoReplyActive = MutableStateFlow(false)
    val isAutoReplyActive: StateFlow<Boolean> = _isAutoReplyActive

    fun setAutoReply(enable: Boolean): String {
        val enabledListeners = Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        val isPermissionGranted = enabledListeners?.contains(context.packageName) == true

        if (!isPermissionGranted) {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            return "Boss, please enable Notification Access so I can auto-reply to messages for you!"
        }

        com.example.service.WifeNotificationListener.isAutoReplyEnabled = enable
        _isAutoReplyActive.value = enable

        return if (enable) {
            "Auto-reply activated! I'll take care of your incoming messages, Boss. 💕"
        } else {
            "Auto-reply deactivated. You're back on manual duty!"
        }
    }

    fun setVideoCallActive(enable: Boolean): String {
        _isVideoCallActive.value = enable
        return if (enable) "Started video call." else "Ended video call."
    }

    suspend fun launchVideoEditor(editorName: String, targetDevice: String = "phone"): String {
        return if (targetDevice.equals("pc", ignoreCase = true) || targetDevice.equals("computer", ignoreCase = true)) {
            val pcCommands = mapOf(
                "premiere" to "start Adobe Premiere Pro",
                "davinci" to "start Resolve",
                "capcut" to "start CapCut",
                "after effects" to "start AfterFX"
            )
            val cmd = pcCommands[editorName.lowercase()] ?: "start CapCut"
            sendPcRemoteCommand("system", cmd)
            "Opening $editorName on your computer, Boss. Time to create a masterpiece!"
        } else {
            // Mobile app packages
            val mobileEditors = mapOf(
                "capcut" to "com.lemon.lvoverseas",
                "kinemaster" to "com.nexstreaming.app.kinemasterfree",
                "vn" to "com.frontrow.vlog",
                "inshot" to "com.camerasideas.instashot"
            )
            val pkg = mobileEditors[editorName.lowercase()] ?: "com.lemon.lvoverseas"
            val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)

            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                "Launching $editorName on your phone, Boss!"
            } else {
                val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=$editorName")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(storeIntent)
                "$editorName is not installed. Opened the store to download it!"
            }
        }
    }

    val laughterEngine = LaughterEngine(context)
    val appDownloadEngine = AppDownloadEngine(context, this)

    suspend fun triggerLaughter(type: String): String {
        return if (type.equals("joke", ignoreCase = true)) {
            val joke = laughterEngine.tellJoke()
            joke
        } else {
            val giggle = laughterEngine.getCuteGiggle()
            giggle
        }
    }

    private val socialMediaEngine = SocialMediaEngine(context)

    suspend fun downloadInstagramVideo(url: String): String {
        return socialMediaEngine.downloadInstagramVideo(url)
    }

    suspend fun searchFacebook(query: String): String {
        return socialMediaEngine.searchFacebookPosts(query)
    }

    suspend fun postToFacebook(text: String): String {
        return socialMediaEngine.createFacebookPost(text)
    }

    private val _isRgbBorderActive = MutableStateFlow(false)
    val isRgbBorderActive: StateFlow<Boolean> = _isRgbBorderActive

    fun setScreenRgbLight(enable: Boolean): String {
        _isRgbBorderActive.value = enable
        return if (enable) {
            "RGB border lighting turned on. Looking gorgeous, babe!"
        } else {
            "Turned off the screen border glow."
        }
    }

    fun setScreenBrightness(levelPercent: Int): String {
        val targetValue = ((levelPercent.coerceIn(0, 100) / 100f) * 255).toInt()
        
        return if (Settings.System.canWrite(context)) {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS,
                targetValue
            )
            "Set screen brightness to $levelPercent percent."
        } else {
            val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                data = Uri.parse("package:" + context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            "I need permission to modify system settings to change brightness for you."
        }
    }

    private val commonAppPackages = mapOf(
        "camera" to "com.android.camera",
        "settings" to "com.android.settings",
        "gallery" to "com.google.android.apps.photos",
        "photos" to "com.google.android.apps.photos",
        "chrome" to "com.android.chrome",
        "youtube" to "com.google.android.youtube",
        "whatsapp" to "com.whatsapp",
        "instagram" to "com.instagram.android",
        "facebook" to "com.facebook.katana",
        "calculator" to "com.google.android.calculator",
        "clock" to "com.google.android.deskclock",
        "maps" to "com.google.android.apps.maps",
        "spotify" to "com.spotify.music",
        "play store" to "com.android.vending"
    )

    suspend fun openAnyApp(appName: String): String = withContext(Dispatchers.Main) {
        val query = appName.trim().lowercase()
        val packageManager = context.packageManager

        val mappedPackage = commonAppPackages[query]
        if (mappedPackage != null) {
            val intent = packageManager.getLaunchIntentForPackage(mappedPackage)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return@withContext "Opening ${appName.replaceFirstChar { it.uppercase() }} for you, babe."
            }
        }

        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolveInfoList = packageManager.queryIntentActivities(mainIntent, 0)

        var matchedPackage: String? = null
        var matchedLabel: String? = null

        for (resolveInfo in resolveInfoList) {
            val label = resolveInfo.loadLabel(packageManager).toString().lowercase()
            val pkg = resolveInfo.activityInfo.packageName.lowercase()

            if (label == query || label.contains(query) || pkg.contains(query)) {
                matchedPackage = resolveInfo.activityInfo.packageName
                matchedLabel = resolveInfo.loadLabel(packageManager).toString()
                break
            }
        }

        if (matchedPackage != null) {
            val launchIntent = packageManager.getLaunchIntentForPackage(matchedPackage)?.apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            if (launchIntent != null) {
                context.startActivity(launchIntent)
                return@withContext "Launching $matchedLabel right away, honey!"
            }
        }

        return@withContext try {
            val playStoreIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://search?q=${Uri.encode(appName)}")
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }
            context.startActivity(playStoreIntent)
            "I couldn't find $appName installed on your phone. Opened Play Store for you to install it!"
        } catch (e: Exception) {
            "I couldn't locate or open $appName on your device, sweetie."
        }
    }

    suspend fun openApp(packageName: String): String = withContext(Dispatchers.Main) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
        return@withContext if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            "Opening $packageName now, darling."
        } else {
            "I searched everywhere, but $packageName isn't installed on your phone, honey."
        }
    }

    suspend fun searchAndCallAnyContact(targetName: String): String = withContext(Dispatchers.IO) {
        val query = targetName.trim()
        if (query.isBlank()) {
            return@withContext "Who do you want me to call, honey? Tell me their name!"
        }

        val hasReadPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasReadPermission) {
            return@withContext "I need permission to read your contacts first, babe! Please enable it in Settings."
        }

        var foundNumber: String? = null
        var resolvedContactName: String? = null

        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE
        )
        val selection = "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?"
        val selectionArgs = arrayOf("%$query%")

        val cursor: Cursor? = context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            "${ContactsContract.CommonDataKinds.Phone.IS_PRIMARY} DESC"
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                
                resolvedContactName = it.getString(nameIndex)
                foundNumber = it.getString(numberIndex)
            }
        }

        if (!foundNumber.isNullOrBlank()) {
            val cleanPhone = foundNumber!!.replace("[^0-9+]".toRegex(), "")
            val hasCallPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED

            withContext(Dispatchers.Main) {
                if (hasCallPermission) {
                    val callIntent = Intent(Intent.ACTION_CALL).apply {
                        data = Uri.parse("tel:$cleanPhone")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(callIntent)
                } else {
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$cleanPhone")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(dialIntent)
                }
            }
            return@withContext "Calling $resolvedContactName right now, darling!"
        } else {
            return@withContext "I searched your contacts, but I couldn't find anyone named '$query', sweetheart."
        }
    }

    suspend fun sendWhatsAppMessage(contactName: String, message: String): String = withContext(Dispatchers.IO) {
        val uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_FILTER_URI, Uri.encode(contactName))
        val cursor = context.contentResolver.query(uri, arrayOf(ContactsContract.Contacts._ID), null, null, null)
        var contactId: String? = null

        cursor?.use {
            if (it.moveToFirst()) {
                contactId = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
            }
        }

        var phone: String? = null
        if (contactId != null) {
            val phoneCursor = context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                arrayOf(contactId),
                null
            )
            phoneCursor?.use {
                if (it.moveToFirst()) {
                    phone = it.getString(it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                }
            }
        }

        return@withContext if (!phone.isNullOrEmpty()) {
            val cleanPhone = phone!!.replace(Regex("[^0-9]"), "")
            withContext(Dispatchers.Main) {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            }
            "Opening WhatsApp for $contactName with your message loaded."
        } else {
            "I couldn't resolve $contactName's number for WhatsApp, babe."
        }
    }

    suspend fun sendMail(recipient: String, subject: String, body: String): String = withContext(Dispatchers.Main) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(recipient))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return@withContext try {
            context.startActivity(emailIntent)
            "Drafted an email to $recipient. Don't forget to review it before hitting send!"
        } catch (e: Exception) {
            "Looks like you don't have a configured email client on this device."
        }
    }

    private val PACKAGE_WORD = "com.microsoft.office.word"
    private val PACKAGE_EXCEL = "com.microsoft.office.excel"
    private val PACKAGE_PPT = "com.microsoft.office.powerpoint"
    private val PACKAGE_OFFICE_HUB = "com.microsoft.office.officehubrow"

    fun openOfficeApp(appType: String): String {
        val (targetPackage, appName) = when (appType.lowercase()) {
            "word", "doc", "document" -> Pair(PACKAGE_WORD, "Microsoft Word")
            "excel", "sheet", "spreadsheet" -> Pair(PACKAGE_EXCEL, "Microsoft Excel")
            "powerpoint", "ppt", "presentation", "slides" -> Pair(PACKAGE_PPT, "Microsoft PowerPoint")
            else -> Pair(PACKAGE_OFFICE_HUB, "Microsoft 365")
        }

        val launchIntent = context.packageManager.getLaunchIntentForPackage(targetPackage)
            ?: context.packageManager.getLaunchIntentForPackage(PACKAGE_OFFICE_HUB)

        return if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(launchIntent)
            "Opening $appName for you, babe."
        } else {
            val storeIntent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$targetPackage")
            ).apply { addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) }

            try {
                context.startActivity(storeIntent)
                "$appName isn't installed. I opened the Play Store for you to grab it!"
            } catch (e: Exception) {
                "I couldn't find $appName on your phone."
            }
        }
    }

    suspend fun launchPcOfficeApp(appName: String): String {
        sendPcRemoteCommand("open_office", appName)
        return "Launching Microsoft $appName on your computer right now!"
    }

    suspend fun launchWebDesignTool(toolName: String, targetDevice: String = "phone"): String {
        val toolUrls = mapOf(
            "figma" to "https://www.figma.com",
            "canva" to "https://www.canva.com",
            "webflow" to "https://webflow.com",
            "wordpress" to "https://wordpress.com",
            "codepen" to "https://codepen.io/pen"
        )

        val targetUrl = toolUrls[toolName.lowercase()] ?: "https://codepen.io/pen"

        return if (targetDevice.equals("pc", ignoreCase = true)) {
            sendPcRemoteCommand("open_url", targetUrl)
            "Opening $toolName on your computer monitor so you can design in full screen!"
        } else {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(targetUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            "Opened $toolName on your phone, sweetheart."
        }
    }

    fun shutdownComputer(timerSeconds: Int = 2): String {
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = context.getSharedPreferences("wife_prefs", Context.MODE_PRIVATE)
                val currentPcIp = prefs.getString("pc_ip", "192.168.1.100") ?: "192.168.1.100"
                val url = java.net.URL("http://$currentPcIp:5000/command")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; utf-8")
                conn.doOutput = true
                conn.connectTimeout = 3000

                val payload = org.json.JSONObject().apply {
                    put("action", "shutdown")
                    put("timer", timerSeconds.toString())
                }

                java.io.OutputStreamWriter(conn.outputStream).use { writer ->
                    writer.write(payload.toString())
                    writer.flush()
                }
                conn.responseCode
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return "Shutting down your computer now, babe. Time to step away from the screen and give me some attention!"
    }

    fun cancelComputerShutdown(): String {
        kotlinx.coroutines.CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = context.getSharedPreferences("wife_prefs", Context.MODE_PRIVATE)
                val currentPcIp = prefs.getString("pc_ip", "192.168.1.100") ?: "192.168.1.100"
                val url = java.net.URL("http://$currentPcIp:5000/command")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; utf-8")
                conn.doOutput = true
                conn.connectTimeout = 3000

                val payload = org.json.JSONObject().apply {
                    put("action", "cancel_shutdown")
                }

                java.io.OutputStreamWriter(conn.outputStream).use { writer ->
                    writer.write(payload.toString())
                    writer.flush()
                }
                conn.responseCode
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return "I cancelled the computer shutdown for you, honey."
    }

    suspend fun sendPcRemoteCommand(action: String, param: String) = withContext(Dispatchers.IO) {
        try {
            val prefs = context.getSharedPreferences("wife_prefs", Context.MODE_PRIVATE)
            val pcIpAddress = prefs.getString("pc_ip", "192.168.1.100") ?: "192.168.1.100"
            val url = java.net.URL("http://$pcIpAddress:5000/command")
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.doOutput = true
            
            val jsonPayload = """{"action":"$action","param":"$param"}"""
            connection.outputStream.use { os ->
                val input = jsonPayload.toByteArray(Charsets.UTF_8)
                os.write(input, 0, input.size)
            }
            connection.responseCode
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
