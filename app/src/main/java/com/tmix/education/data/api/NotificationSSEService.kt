package com.tmix.education.data.api

import android.util.Log
import com.google.gson.Gson
import com.tmix.education.data.model.Notification
import okhttp3.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

/**
 * SSE (Server-Sent Events) client for real-time notifications
 * Connects to GET /notifications/stream?token={jwt}
 * Parses "event: notification" + "data: {...}" from the stream
 */
class NotificationSSEService(
    private val onNotification: (Notification) -> Unit,
    private val onError: (Exception) -> Unit = {}
) {
    companion object {
        private const val TAG = "NotificationSSE"
        private const val RECONNECT_DELAY_MS = 5000L
    }

    private val gson = Gson()
    private var call: Call? = null
    private var isRunning = false
    private var reconnectRunnable: Runnable? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    /**
     * Start SSE connection
     */
    fun connect(token: String) {
        if (isRunning) return
        isRunning = true

        val baseUrl = ApiConfig.BASE_URL.removeSuffix("api/v1/").removeSuffix("/")
        val streamUrl = "$baseUrl/api/v1/notifications/stream?token=$token"

        Log.d(TAG, "Connecting to SSE: $streamUrl")

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS) // No read timeout for SSE
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        val request = Request.Builder()
            .url(streamUrl)
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .build()

        call = client.newCall(request)
        call?.enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {
                Log.e(TAG, "SSE connection failed", e)
                if (isRunning) {
                    scheduleReconnect(token)
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    Log.e(TAG, "SSE response error: ${response.code}")
                    response.close()
                    if (isRunning) {
                        scheduleReconnect(token)
                    }
                    return
                }

                Log.d(TAG, "SSE connected successfully")

                try {
                    val body = response.body ?: return
                    val reader = BufferedReader(InputStreamReader(body.byteStream()))

                    var eventType = ""
                    var data = ""

                    var line: String?
                    while (reader.readLine().also { line = it } != null && isRunning) {
                        val currentLine = line ?: continue

                        when {
                            currentLine.startsWith("event:") -> {
                                eventType = currentLine.removePrefix("event:").trim()
                            }
                            currentLine.startsWith("data:") -> {
                                data = currentLine.removePrefix("data:").trim()
                            }
                            currentLine.isEmpty() && data.isNotEmpty() -> {
                                // End of event block — process it
                                if (eventType == "notification") {
                                    try {
                                        val notification = gson.fromJson(data, Notification::class.java)
                                        Log.d(TAG, "Received notification: ${notification.title}")
                                        handler.post { onNotification(notification) }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Failed to parse notification: $data", e)
                                    }
                                }
                                // Reset for next event
                                eventType = ""
                                data = ""
                            }
                        }
                    }
                } catch (e: Exception) {
                    if (isRunning) {
                        Log.e(TAG, "SSE stream error", e)
                        scheduleReconnect(token)
                    }
                } finally {
                    response.close()
                }
            }
        })
    }

    /**
     * Schedule reconnection after delay
     */
    private fun scheduleReconnect(token: String) {
        if (!isRunning) return

        Log.d(TAG, "Scheduling reconnect in ${RECONNECT_DELAY_MS}ms")
        reconnectRunnable?.let { handler.removeCallbacks(it) }

        reconnectRunnable = Runnable {
            if (isRunning) {
                call?.cancel()
                call = null
                connect(token)
            }
        }
        handler.postDelayed(reconnectRunnable!!, RECONNECT_DELAY_MS)
    }

    /**
     * Disconnect SSE
     */
    fun disconnect() {
        isRunning = false
        reconnectRunnable?.let { handler.removeCallbacks(it) }
        reconnectRunnable = null
        call?.cancel()
        call = null
        Log.d(TAG, "SSE disconnected")
    }
}
