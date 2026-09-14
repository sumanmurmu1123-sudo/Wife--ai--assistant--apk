import re

with open('app/src/main/java/com/example/v2/audio/AudioCaptureManager.kt', 'r') as f:
    content = f.read()

old_flow = """        audioRecord?.startRecording()
        
        val buffer = ByteArray(bufferSize)
        while (coroutineContext.isActive) {
            val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
            if (read > 0) {
                emit(buffer.copyOf(read))
            }
        }
    }.flowOn(Dispatchers.IO)"""

new_flow = """        audioRecord?.startRecording()
        
        try {
            val buffer = ByteArray(bufferSize)
            while (coroutineContext.isActive) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    emit(buffer.copyOf(read))
                }
            }
        } finally {
            stopCapture()
        }
    }.flowOn(Dispatchers.IO)"""

if "finally {" not in content:
    content = content.replace(old_flow, new_flow)

with open('app/src/main/java/com/example/v2/audio/AudioCaptureManager.kt', 'w') as f:
    f.write(content)
