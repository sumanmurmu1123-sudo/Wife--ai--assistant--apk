package com.example.v2.core.tools

import android.content.Context
import com.example.v2.core.tools.impl.*

object ToolInitializer {
    fun registerAllTools(registry: ToolRegistry, context: Context) {
        // Category 1: Voice & AI
        registry.register(GeminiLiveTool(context))
        registry.register(MicrophoneTool(context))
        registry.register(SpeechRecognitionTool(context))
        registry.register(TextToSpeechTool(context))
        registry.register(ElevenLabsTtsTool(context))
        registry.register(VoiceInterruptTool(context))
        registry.register(VoiceReconnectTool(context))

        // Category 2: Android Control
        registry.register(FlashlightTool(context))
        registry.register(ScreenshotTool(context))
        registry.register(VolumeControlTool(context))
        registry.register(HomeTool(context))
        registry.register(BackTool(context))
        registry.register(RecentAppsTool(context))
        registry.register(LockScreenTool(context))
        registry.register(NotificationShadeTool(context))
        registry.register(QuickSettingsTool(context))
        registry.register(WifiSettingsTool(context))
        registry.register(BluetoothSettingsTool(context))
        registry.register(DisplaySettingsTool(context))

        // Category 3: Automation
        registry.register(AccessibilityTool(context))
        registry.register(TapTextTool(context))
        registry.register(TapCoordinateTool(context))
        registry.register(SwipeTool(context))
        registry.register(ScrollTool(context))
        registry.register(TypeTextTool(context))
        registry.register(OpenAppTool(context))
        registry.register(CloseAppTool(context))

        // Category 4: Phone Safety
        registry.register(FindPhoneTool(context))
        registry.register(LocationTool(context))
        registry.register(DeviceStatusTool(context))
        registry.register(BatteryStatusTool(context))
        registry.register(NetworkStatusTool(context))
        registry.register(SocialReplyTool(context))

        // Category 5: PC Control
        registry.register(PcConnectTool(context))
        registry.register(PcCommandTool(context))
        registry.register(PcAppLaunchTool(context))
        registry.register(PcFileTool(context))
        registry.register(PcMediaTool(context))
        registry.register(PcShutdownTool(context))

        // Category 6: Memory
        registry.register(MemorySaveTool(context))
        registry.register(MemorySearchTool(context))
        registry.register(MemoryUpdateTool(context))
        registry.register(MemoryDeleteTool(context))

        // Category 7: Task & Automation
        registry.register(TaskCreateTool(context))
        registry.register(TaskRunTool(context))
        registry.register(TaskCancelTool(context))
        registry.register(ReminderTool(context))
        registry.register(ScheduledTaskTool(context))
        registry.register(AutomationWorkflowTool(context))

        // Category 8: Internet
        registry.register(WebSearchTool(context))
        registry.register(NewsTool(context))
        registry.register(WeatherTool(context))
        registry.register(MapsTool(context))
        registry.register(WebOpenTool(context))

        // Category 9: Government Jobs
        registry.register(JobSearchTool(context))
        registry.register(JobEligibilityTool(context))
        registry.register(JobDeadlineTool(context))
        registry.register(JobSyllabusTool(context))
        registry.register(JobNotificationsTool(context))

        // Category 10: Media & Creative
        registry.register(ImageGenerationTool(context))
        registry.register(ImageEditTool(context))
        registry.register(VideoEditorTool(context))
        registry.register(VideoTrimTool(context))
        registry.register(VideoCutTool(context))
        registry.register(VideoMergeTool(context))
        registry.register(SubtitleTool(context))

        // Category 11: System & Security
        registry.register(PermissionsTool(context))
        registry.register(ForegroundServiceTool(context))
        registry.register(OverlayTool(context))
        registry.register(AccessibilityStatusTool(context))
        registry.register(NetworkMonitorTool(context))
        registry.register(BatteryMonitorTool(context))
        registry.register(AppUpdateTool(context))
        registry.register(NotificationAccessTool(context))
        registry.register(DiagnosticsTool(context))
    }
}
