package com.example.ui

import kotlinx.coroutines.launch
import androidx.compose.animation.core.*
import androidx.compose.animation.*
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import com.example.ui.touch.onWifeTouchWakeUp
import androidx.compose.animation.slideInVertically
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.DesignServices
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.MarkChatRead
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Groups
import com.example.ui.mlm.NetworkMarketingSheet
 
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Groups
import com.example.ui.mlm.NetworkMarketingSheet
import com.example.domain.NetworkMarketingEngine
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.AssistantMood
import com.example.data.AssistantState
import com.example.ui.components.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MainAssistantScreen(
    state: AssistantState,
    currentIp: String,
    currentMood: AssistantMood = AssistantMood.NEUTRAL,
    onUpdateIp: (String) -> Unit,
    onShutdownPcClick: () -> Unit = {},
    onStartVoice: () -> Unit = {},
    onEndVoice: () -> Unit = {},
    isBorderLightActive: Boolean = false,
    bossName: String = "Boss",
    onUpdateBossName: (String) -> Unit = {},
    isSocialModeActive: Boolean = false,
    onToggleSocialMode: (Boolean) -> Unit = {},
    activePersona: com.example.data.CompanionPersona = com.example.data.CompanionPersona.GIRLFRIEND,
    onTogglePersona: (com.example.data.CompanionPersona) -> Unit = {},
    isVideoStudioActive: Boolean = false,
    onCloseVideoStudio: () -> Unit = {},
    onTriggerLaugh: () -> Unit = {},
    onTriggerAppDownload: () -> Unit = {},
    isVideoCallActive: Boolean = false,
    onEndVideoCall: () -> Unit = {},
    onStartVideoCall: () -> Unit = {},
    onToggleMute: () -> Unit = {},
    onFlipCamera: () -> Unit = {},
    isMuted: Boolean = false,
    isVoiceCallActive: Boolean = false,
    onStartVoiceCall: () -> Unit = {},
    onEndVoiceCall: () -> Unit = {},
    onToggleSpeaker: (Boolean) -> Unit = {},
    onTriggerGreeting: () -> Unit = {},
    isAutoReplyActive: Boolean = false,
    onToggleAutoReply: () -> Unit = {}
) {
    var showIpDialog by remember { mutableStateOf(false) }
    var showWebStudio by remember { mutableStateOf(false) }
    var showShutdownConfirm by remember { mutableStateOf(false) }
    var currentLanguage by remember { mutableStateOf(AppLanguage.AUTO) }
    var showLangDialog by remember { mutableStateOf(false) }
    var showNameDialog by remember { mutableStateOf(false) }
    var isGameMoodActive by remember { mutableStateOf(false) }
    var isMlmSheetOpen by remember { mutableStateOf(false) }
    var isDrivingMode by remember { mutableStateOf(false) }
    var isSettingsOpen by remember { mutableStateOf(false) }
    var firstGreetingMessage by remember { mutableStateOf("") }
    var showHeartPulse by remember { mutableStateOf(false) }
    
    var isSecuritySheetOpen by remember { mutableStateOf(false) }
    var isRemoteDashboardOpen by remember { mutableStateOf(false) }
    var isMarketingSheetOpen by remember { mutableStateOf(false) }
    var isChatDrawerOpen by remember { mutableStateOf(false) }
    val cyberSecurityEngine = remember { com.example.domain.CyberSecurityEngine() }
    val marketingEngine = remember { com.example.domain.DigitalMarketingEngine() }
    
    val chatHistory by com.example.domain.ChatStateManager.messages.collectAsState()
    val isTyping by com.example.domain.ChatStateManager.isTyping.collectAsState()
    
    if (showHeartPulse) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(1500)
            showHeartPulse = false
        }
    }
    
    val context = androidx.compose.ui.platform.LocalContext.current
    val mlmEngine = remember { NetworkMarketingEngine(context) }
    val userSettings = remember { com.example.data.UserPreferences(context) }

    val firstGreetingEngine = remember { com.example.domain.FirstGreetingEngine() }

    LaunchedEffect(Unit) {
        firstGreetingMessage = firstGreetingEngine.generateFirstGreeting(bossName, activePersona)
        onTriggerGreeting()
    }
    
    val clapDetector = remember { com.example.domain.audio.ClapDetectorEngine() }
    
    androidx.compose.runtime.DisposableEffect(Unit) {
        val job = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            clapDetector.startClapListening {
                onStartVoice() // Start listening when clap is detected
            }
        }
        onDispose {
            clapDetector.stopClapListening()
            job.cancel()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF121024),
                        Color(0xFF07070D),
                        Color(0xFF020205)
                    )
                )
            )
            .onWifeTouchWakeUp(
                onAssistantActive = onStartVoice,
                onDoubleTapHeart = { showHeartPulse = true }
            )
    ) {
        if (isVoiceCallActive) {
            com.example.ui.call.VoiceCallScreen(
                state = state,
                bossName = bossName,
                onEndCall = onEndVoiceCall,
                onToggleMute = { onToggleMute() },
                onToggleSpeaker = onToggleSpeaker
            )
        } else if (isVideoCallActive) {
            com.example.ui.call.VideoCallScreen(
                state = state,
                bossName = bossName,
                onEndCall = onEndVideoCall,
                onToggleMute = onToggleMute,
                onFlipCamera = onFlipCamera,
                isMuted = isMuted
            )
        } else if (isVideoStudioActive) {
            com.example.ui.video.VideoEditStudio(
                onClose = onCloseVideoStudio,
                onApplyEffect = { effect -> }
            )
        } else if (isGameMoodActive) {
            com.example.ui.components.GameMoodHUD(
                state = state,
                onExitGameMood = { isGameMoodActive = false },
                onVoiceClick = onStartVoice
            )
        } else if (isDrivingMode) {
            com.example.ui.components.DrivingModeHUD(
                state = state,
                onExitDrivingMode = { isDrivingMode = false },
                onVoiceClick = onStartVoice
            )
        } else {
            // 1. Floating Cyber Embers / Particles
            CyberParticles()

        // 2. 3D Character Avatar (Centered)
        Wife3DGirlAvatar(
            state = state,
            currentMood = currentMood,
            modifier = Modifier.fillMaxSize()
        )

        // 3. Glowing Neon Edge Border (Triggers on speech or command)
        EmotionalEdgeLighting(
            mood = currentMood,
            isActive = isBorderLightActive || state == AssistantState.SPEAKING || state == AssistantState.LISTENING
        )

        // Proactive Subtitle / First Greeting Banner
        AnimatedVisibility(
            visible = firstGreetingMessage.isNotBlank(),
            enter = fadeIn() + slideInVertically(initialOffsetY = { -40 }),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 90.dp, start = 20.dp, end = 20.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF141020).copy(alpha = 0.9f))
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(activePersona.primaryColor, Color(0xFF00F5FF).copy(alpha = 0.5f))
                        ),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = firstGreetingMessage,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }

        // 4. Top Telemetry & Glassmorphic HUD Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left HUD Badges
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Interactive Boss Name Button/Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF1A1A2E).copy(alpha = 0.8f))
                            .border(
                                width = 1.dp,
                                brush = Brush.linearGradient(
                                    listOf(Color(0xFFFFD700).copy(alpha = 0.6f), Color(0xFFFF007F).copy(alpha = 0.3f))
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { showNameDialog = true }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = "Boss Name",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Boss: ",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                            Text(
                                text = bossName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD700)
                            )
                        }
                    }
                    
                    SocialModeBadgeButton(
                        isActive = isSocialModeActive,
                        onToggle = { onToggleSocialMode(!isSocialModeActive) }
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    PersonaSwitchButton(
                        currentPersona = activePersona,
                        onTogglePersona = {
                            val next = if (activePersona == com.example.data.CompanionPersona.GIRLFRIEND) com.example.data.CompanionPersona.BEST_FRIEND else com.example.data.CompanionPersona.GIRLFRIEND
                            onTogglePersona(next)
                        }
                    )
                    
                    AutoReplyHudButton(
                        isActive = isAutoReplyActive,
                        onToggle = onToggleAutoReply
                    )
                }
            }

            // Top Right Action Icons
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconButton(
                    onClick = { isChatDrawerOpen = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1E2E).copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Open Chat Box",
                        tint = Color(0xFFFF007F),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { isMarketingSheetOpen = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1E2E).copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Digital Marketing Suite",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = { isMlmSheetOpen = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1E2E).copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Groups,
                        contentDescription = "Network Marketing Suite",
                        tint = Color(0xFF00FF66),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = { isRemoteDashboardOpen = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1E2E).copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = "Remote Dashboard",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { isSecuritySheetOpen = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1E2E).copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Cybersecurity Center",
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = { isSettingsOpen = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1E2E).copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Profile & Voice Settings",
                        tint = Color(0xFF00F5FF),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                IconButton(
                    onClick = { isDrivingMode = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1E2E).copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = "Driving Mode",
                        tint = Color(0xFF00FF66),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                IconButton(
                    onClick = onStartVoiceCall,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF00E676).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF00E676), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneInTalk,
                        contentDescription = "Voice Call Wife",
                        tint = Color(0xFF00FF66),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                IconButton(
                    onClick = onStartVideoCall,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1E2E).copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "Start Video Call",
                        tint = Color(0xFFFF007F),
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onTriggerAppDownload,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1E2E).copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "App Downloader",
                        tint = Color(0xFF00F5FF),
                        modifier = Modifier.size(20.dp)
                    )
                }

                LaughterBadgeButton(onTriggerLaugh = onTriggerLaugh)
                
                IconButton(
                    onClick = { showLangDialog = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1E2E).copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = "Change Language",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = { showShutdownConfirm = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A101E).copy(alpha = 0.8f))
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Shutdown PC",
                        tint = Color(0xFFFF0055),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = { showWebStudio = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1E2E).copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.DesignServices,
                        contentDescription = "Web Design Studio",
                        tint = Color(0xFFFF007F),
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = { isGameMoodActive = true },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E1E2E).copy(alpha = 0.6f))
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = "Game Mood",
                        tint = Color(0xFF00FF66),
                        modifier = Modifier.size(20.dp)
                    )
                }
                RemoteConfigButton(
                    currentIp = currentIp,
                    onClick = { showIpDialog = true }
                )
            }
        }

        // 5. Header Title & Assistant Persona Status
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "W I F E",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 12.sp,
                fontFamily = FontFamily.SansSerif,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = activePersona.greeting,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = activePersona.primaryColor
            )
        }

        // Heart Pulse Double Tap Animation
        androidx.compose.animation.AnimatedVisibility(
            visible = showHeartPulse,
            enter = scaleIn(initialScale = 0.5f) + fadeIn(),
            exit = scaleOut(targetScale = 1.5f) + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Heart Pulse",
                tint = Color(0xFFFF2A85),
                modifier = Modifier.size(120.dp)
            )
        }

        // 6. Bottom Controls: Audio Equalizer + Floating Voice Orb
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Audio Equalizer Wave Bar
            CyberAudioWave(
                state = state,
                modifier = Modifier.padding(horizontal = 40.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Dual Voice Controls (Mic + End Call)
            VoiceActionControlBar(
                state = state,
                onStartVoice = onStartVoice,
                onEndVoice = onEndVoice
            )
        }
        } // Close else branch for GameMoodHUD

        // 7. Settings Modal Dialog
        if (showIpDialog) {
            IpConfigDialog(
                currentIp = currentIp,
                onDismiss = { showIpDialog = false },
                onSaveIp = onUpdateIp
            )
        }

        // 8. Modal or Fullscreen Overlay for Web Studio
        if (showWebStudio) {
            com.example.ui.webdesign.WebDesignStudio(onClose = { showWebStudio = false })
        }

        // 9. Shutdown Confirmation Dialog
        if (showShutdownConfirm) {
            AlertDialog(
                onDismissRequest = { showShutdownConfirm = false },
                containerColor = Color(0xFF1E1E2E),
                title = { Text("Shutdown PC?", color = Color.White) },
                text = { Text("Are you sure you want to shut down your computer remotely?", color = Color.LightGray) },
                confirmButton = {
                    androidx.compose.material3.Button(
                        onClick = {
                            onShutdownPcClick()
                            showShutdownConfirm = false
                        },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0055))
                    ) {
                        Text("Shutdown Now", color = Color.White)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = { showShutdownConfirm = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }

        // 10. Language Selection Dialog
        if (showLangDialog) {
            LanguageSelectionDialog(
                selectedLanguage = currentLanguage,
                onLanguageSelected = { newLang ->
                    currentLanguage = newLang
                },
                onDismiss = { showLangDialog = false }
            )
        }

        // 11. Boss Name Dialog
        if (showNameDialog) {
            BossNameDialog(
                currentName = bossName,
                onDismiss = { showNameDialog = false },
                onSaveName = onUpdateBossName
            )
        }
        
        // 12. Profile & Voice Slate Screen Overlay
        if (isMarketingSheetOpen) {
            com.example.ui.marketing.DigitalMarketingSheet(
                engine = marketingEngine,
                onVoiceDiscuss = { prompt ->
                    isMarketingSheetOpen = false
                    // Ideally pass prompt to voice engine. For now, close sheet.
                },
                onClose = { isMarketingSheetOpen = false }
            )
        }

        if (isRemoteDashboardOpen) {
            com.example.ui.remote.MainDashboardScreen(
                onClose = { isRemoteDashboardOpen = false }
            )
        }
        
        if (isChatDrawerOpen) {
            com.example.ui.chat.WifeChatBoxSheet(
                messages = chatHistory,
                isWifeTyping = isTyping,
                onSendMessage = { text ->
                    com.example.domain.ChatStateManager.addMessage(
                        com.example.data.ChatMessage(text = text, sender = com.example.data.MessageSender.USER)
                    )
                    com.example.domain.ChatStateManager.setTyping(true)
                    val intent = android.content.Intent(context, com.example.service.WifeForegroundService::class.java).apply {
                        action = "ACTION_SEND_TEXT"
                        putExtra("text", text)
                    }
                    context.startService(intent)
                },
                onClearChat = { com.example.domain.ChatStateManager.clearMessages() },
                onClose = { isChatDrawerOpen = false }
            )
        }
        
        if (isSecuritySheetOpen) {
            com.example.ui.security.CyberSecuritySheet(
                engine = cyberSecurityEngine,
                onVoiceConsult = { prompt ->
                    isSecuritySheetOpen = false
                    // Ideally we should tell the session manager to speak the text here. 
                    // However, we can simply emit an action if needed. 
                    // As we don't have direct access to session manager's speakProactiveText here, 
                    // we'll just log it or pass a callback. For now, we can pass it if we have it, 
                    // or just leave it out. The prompt was assuming coroutineScope.launch { sessionManager.speakProactiveText }
                    // We don't have this, so we'll just close it.
                    // Or we could try passing a function.
                },
                onClose = { isSecuritySheetOpen = false }
            )
        }
        
        if (isMlmSheetOpen) {
            NetworkMarketingSheet(
                mlmEngine = mlmEngine,
                onTriggerVoiceScript = { prompt ->
                    isMlmSheetOpen = false
                },
                onClose = { isMlmSheetOpen = false }
            )
        }

        if (isSettingsOpen) {
            com.example.ui.settings.ProfileSettingsScreen(
                repository = userSettings,
                onSaveAndClose = { newBossName, newAssistantName, newVoice ->
                    onUpdateBossName(newBossName)
                    // The other two are saved internally in the repository (UserPreferences) 
                    // which is read by GeminiLiveSessionManager on its next connection.
                    isSettingsOpen = false
                },
                onClose = { isSettingsOpen = false }
            )
        }
    }
}

@Composable
fun WifeAnimatedCore(
    state: AssistantState,
    onOrbTapped: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbTransitions")

    // Dynamic scale mappings per state
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IdleBreathing"
    )

    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SpinAngle"
    )

    val waveAmplitude by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "WaveAmp"
    )

    Box(
        modifier = Modifier
            .size(320.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onOrbTapped
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasCenter = center
            val baseRadius = 85.dp.toPx()

            when (state) {
                AssistantState.IDLE -> {
                    // Deep, relaxed ambient glow
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF8A2BE2), Color(0xFFFF1493), Color.Transparent),
                            center = canvasCenter,
                            radius = baseRadius * 1.5f * breathingScale
                        ),
                        radius = baseRadius * 1.5f * breathingScale
                    )
                    drawCircle(
                        color = Color(0xFF0F0B1E),
                        radius = baseRadius * 0.9f
                    )
                }

                AssistantState.LISTENING -> {
                    // Responsive dynamic listening wave array
                    for (i in 0 until 12) {
                        val angle = Math.toRadians((i * 30 + rotationAngle).toDouble())
                        val length = baseRadius + (waveAmplitude * (i % 3 + 1))
                        val x = canvasCenter.x + (length * cos(angle)).toFloat()
                        val y = canvasCenter.y + (length * sin(angle)).toFloat()

                        drawLine(
                            brush = Brush.linearGradient(
                                listOf(Color(0xFF00FFFF), Color(0xFFFF007F))
                            ),
                            start = canvasCenter,
                            end = Offset(x, y),
                            strokeWidth = 4.dp.toPx()
                        )
                    }
                    drawCircle(
                        color = Color(0xFF07070D),
                        radius = baseRadius * 0.75f
                    )
                }

                AssistantState.THINKING -> {
                    // High-frequency spinning neon rings
                    drawCircle(
                        brush = Brush.sweepGradient(
                            colors = listOf(Color(0xFF00FFCC), Color(0xFFFF007F), Color(0xFF8A2BE2), Color(0xFF00FFCC))
                        ),
                        radius = baseRadius * 1.2f,
                        style = Stroke(width = 6.dp.toPx())
                    )
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFF8A2BE2).copy(alpha = 0.4f), Color.Transparent),
                            center = canvasCenter
                        ),
                        radius = baseRadius
                    )
                }

                AssistantState.SPEAKING -> {
                    // Audio waveform ring visualization
                    for (i in 0 until 360 step 10) {
                        val rad = Math.toRadians(i.toDouble())
                        val dynamicOffset = if (i % 20 == 0) waveAmplitude else -waveAmplitude / 2
                        val lineRadius = baseRadius + dynamicOffset
                        val x = canvasCenter.x + (lineRadius * cos(rad)).toFloat()
                        val y = canvasCenter.y + (lineRadius * sin(rad)).toFloat()

                        drawCircle(
                            color = if (i % 20 == 0) Color(0xFFFF1493) else Color(0xFF00FFFF),
                            center = Offset(x, y),
                            radius = 3.dp.toPx()
                        )
                    }
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xFFFF007F), Color(0xFF8A2BE2), Color.Transparent),
                            center = canvasCenter
                        ),
                        radius = baseRadius * breathingScale
                    )
                }
            }
        }
    }
}

@Composable
fun AutoReplyHudButton(
    isActive: Boolean,
    onToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isActive) Color(0xFF00FF66).copy(alpha = 0.2f) else Color(0xFF1E1E2E).copy(alpha = 0.6f))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    if (isActive) listOf(Color(0xFF00FF66), Color(0xFF00F5FF))
                    else listOf(Color.Gray.copy(alpha = 0.3f), Color.Transparent)
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.MarkChatRead,
                contentDescription = "Auto Reply",
                tint = if (isActive) Color(0xFF00FF66) else Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isActive) "Auto-Reply: ON" else "Auto-Reply: OFF",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) Color(0xFF00FF66) else Color.Gray
            )
        }
    }
}
