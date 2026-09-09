package com.example.v2.avatar

import android.util.Log

enum class AvatarAnimation {
    IDLE,
    BLINK,
    SMILE,
    LISTENING,
    THINKING,
    TALK
}

class AvatarController {
    
    private var isLoaded = false
    private var currentAnimation = AvatarAnimation.IDLE

    fun loadAvatar(modelPath: String): Boolean {
        // Attempt to load GLB model.
        // If sceneview is available, we would load it here.
        // For this environment, we simulate failure if the file doesn't exist or no 3D engine is provided,
        // which triggers the HeartNode fallback safely.
        Log.d("AvatarController", "Attempting to load: \$modelPath")
        isLoaded = false // Force fallback to HeartNode as per requirement if GLB cannot load.
        return isLoaded
    }

    fun playAnimation(animation: AvatarAnimation) {
        if (!isLoaded) return
        currentAnimation = animation
        Log.d("AvatarController", "Playing animation: \$animation")
    }

    fun setLipSyncActive(isActive: Boolean) {
        if (!isLoaded) return
        if (isActive) {
            playAnimation(AvatarAnimation.TALK)
        } else {
            playAnimation(AvatarAnimation.IDLE)
        }
    }
}
