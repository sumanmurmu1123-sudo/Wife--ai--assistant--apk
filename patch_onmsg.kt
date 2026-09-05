            override fun onMessage(webSocket: WebSocket, bytes: okio.ByteString) {
                stopWifeThinkingState()
                // মডেল থেকে আসা রিয়েল-টাইম অডিও ডাটা স্পিকারে প্লে করা
                // (Assuming playRawAudio handles the playback)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                stopWifeThinkingState()
            }
