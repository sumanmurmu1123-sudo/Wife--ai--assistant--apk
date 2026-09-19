package com.example.v2.core.permission

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.v2.core.EventBus
import com.example.v2.core.AssistantEvent
import com.example.v2.core.StateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PermissionState {
    GRANTED, DENIED, NOT_REQUESTED, SYSTEM_DISABLED, PERMANENTLY_DENIED
}

data class SystemPermission(
    val id: String,
    val name: String,
    val isRequired: Boolean,
    val state: PermissionState,
    val manifestPermission: String
)

class PermissionManager(private val context: Context) {
    private val _permissions = MutableStateFlow<List<SystemPermission>>(emptyList())
    val permissions = _permissions.asStateFlow()
    
    init {
        refreshPermissions()
    }

    fun refreshPermissions() {
        val permissionList = mutableListOf<SystemPermission>()
        
        // Microphone
        permissionList.add(createPermission("mic", "Microphone", true, android.Manifest.permission.RECORD_AUDIO))
        
        // Notifications (Tiramisu+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionList.add(createPermission("notifications", "Notifications", false, android.Manifest.permission.POST_NOTIFICATIONS))
        }

        // Location
        permissionList.add(createPermission("location", "Location", false, android.Manifest.permission.ACCESS_FINE_LOCATION))

        // Phone/Telephony
        permissionList.add(createPermission("phone", "Telephony", false, android.Manifest.permission.CALL_PHONE))
        
        // Contacts
        permissionList.add(createPermission("contacts", "Contacts", false, android.Manifest.permission.READ_CONTACTS))

        // Camera
        permissionList.add(createPermission("camera", "Camera", false, android.Manifest.permission.CAMERA))

        _permissions.value = permissionList
        
        val micGranted = ContextCompat.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        StateManager.updateState { it.copy(micPermissionGranted = micGranted) }
        
        EventBus.publish(AssistantEvent.PermissionChanged("mic", micGranted))
    }

    private fun createPermission(id: String, name: String, required: Boolean, manifestPerm: String): SystemPermission {
        val state = if (ContextCompat.checkSelfPermission(context, manifestPerm) == PackageManager.PERMISSION_GRANTED) {
            PermissionState.GRANTED
        } else {
            PermissionState.DENIED
        }
        return SystemPermission(id, name, required, state, manifestPerm)
    }
}
