package com.example.sync

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.PrintWriter
import java.net.InetSocketAddress
import java.net.Socket

object WifePcSyncClient {

    private const val PORT = 5050
    private const val TIMEOUT_MS = 2000

    /**
     * পিসির হলোগ্রামে ইভেন্ট পুশ করা
     * @param pcIp পিসির লোকাল আইপি (যেমন: "192.168.0.105")
     * @param command নির্দেশ (যেমন: "slx_on", "lock", "battery_low")
     */
    suspend fun sendEventToPC(pcIp: String, command: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress(pcIp, PORT), TIMEOUT_MS)
                    val output = PrintWriter(socket.getOutputStream(), true)
                    output.println(command)
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }
}
