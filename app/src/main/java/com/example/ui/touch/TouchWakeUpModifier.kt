package com.example.ui.touch

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.onWifeTouchWakeUp(
    onAssistantActive: () -> Unit,
    onDoubleTapHeart: () -> Unit
): Modifier = this.pointerInput(Unit) {
    detectTapGestures(
        onTap = {
            onAssistantActive()
        },
        onDoubleTap = {
            onDoubleTapHeart()
        }
    )
}
