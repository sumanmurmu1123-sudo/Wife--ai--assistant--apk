package com.example.v2.core.permission

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.v2.core.EventBus
import com.example.v2.core.AssistantEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PermissionState {
    GRANTED, DENIED, NOT_REQUESTED, SYSTEM_DISABLED
}

data class SystemPermission(
    val id: String,
    val name: String,
    val isRequired: Boolean,
    val state: PermissionState
)

class PermissionManager(private val context: Context) {
    private val _permissions = MutableStateFlow<List<SystemPermission>>(emptyList())
    val permissions = _permissions.asStateFlow()
    
    init {
        refreshPermissions()
    }

    fun refreshPermissions() {
        val micState = if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) 
            PermissionState.GRANTED else PermissionState.DENIED
            
        val notifState = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)
                PermissionState.GRANTED else PermissionState.DENIED
        } else {
            PermissionState.GRANTED
        }

        _permissions.value = listOf(
            SystemPermission("mic", "Microphone", true, micState),
            SystemPermission("notifications", "Notifications", false, notifState)
        )
        
        EventBus.publish(AssistantEvent.PermissionChanged("mic", micState == PermissionState.GRANTED))
    }
}
