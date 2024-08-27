package com.example.binanceticker.data.remote

import com.example.binanceticker.domain.model.Ticker
import com.example.binanceticker.utils.Constants.BINANCE_WS_STREAM_URL
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class WebSocketManager @Inject constructor(
    private val json: Json,
    private val okHttpClient: OkHttpClient
) {
    private var webSocket: WebSocket? = null
    private val subscribedSymbols = mutableSetOf<String>()
    private val requestIdCounter = AtomicInteger(1)
    private var isConnected = AtomicBoolean(false)

    // 這裡的 MutableSharedFlow 需要定義 buffer 大小 因為：
    // tryEmit (unlike emit) is not a suspending function,
    // so it clearly cannot operate without a buffer where it can store emitted value for all the suspending subscribers to process.
    // On the other hand, emit is suspending, so it does not need buffer space, as it can always suspend in case any of the subscribers are not ready yet.
    private val _tickerData = MutableSharedFlow<Pair<String, Ticker>>(replay = 1, extraBufferCapacity = 64)
    val tickerData: MutableSharedFlow<Pair<String, Ticker>> = _tickerData

    fun connect() {
        if (!isConnected.get()) {
            connectWebSocket()
        }
    }

    fun disconnect() {
        if (isConnected.get()) {
            disconnectWebSocket()
        }
    }

    private fun connectWebSocket() {
        val request = Request.Builder()
            .url(BINANCE_WS_STREAM_URL)
            .build()

        webSocket?.close(1000, "Reconnecting")
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                handleWebSocketMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Timber.e(t, "WebSocket connection failed")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Timber.d("WebSocket closed: $reason")
                isConnected.set(false)
            }
        })

        isConnected.set(true)
    }

    private fun disconnectWebSocket() {
        webSocket?.close(1000, "Client closed")
        webSocket = null
        isConnected.set(false)
        subscribedSymbols.clear()
    }

    fun subscribe(symbol: String) {
        val stream = "${symbol.lowercase()}@ticker"
        if (subscribedSymbols.add(stream)) {
            val message = createSubscriptionMessage("SUBSCRIBE", listOf(stream))
            webSocket?.send(message)
        }
    }

    fun unsubscribe(symbol: String) {
        val stream = "${symbol.lowercase()}@ticker"
        if (subscribedSymbols.remove(stream)) {
            val message = createSubscriptionMessage("UNSUBSCRIBE", listOf(stream))
            webSocket?.send(message)
        }
    }

    private fun createSubscriptionMessage(method: String, params: List<String>): String {
        val id = requestIdCounter.getAndIncrement()
        val jsonObject = JSONObject().apply {
            put("method", method)
            put("params", JSONArray(params))
            put("id", id)
        }
        return jsonObject.toString()
    }

    private fun handleWebSocketMessage(message: String) {
        val jsonObject = JSONObject(message)

        // 如果包含 "result"，則表示這是一個訂閱確認(Subscribe/Unsubscribe)，直接忽略
        if (jsonObject.has("result")) {
            Timber.d("Subscription confirmed with id: ${jsonObject.getInt("id")}")
            return
        }

        val isCombinedStream = jsonObject.has("stream")
        val data = if (isCombinedStream) {
            jsonObject.getString("data")
        } else {
            message
        }

        val dataObject = JSONObject(data)
        val symbol: String = if (dataObject.has("s")) {
            dataObject.getString("s").lowercase()
        } else {
            Timber.e("WebSocket message received without a symbol: $message")
            return
        }

        try {
            val ticker = json.decodeFromString<Ticker>(data)
            _tickerData.tryEmit(symbol.uppercase() to ticker)
        } catch (e: Exception) {
            Timber.e("Failed to parse ticker data: ${e.message}")
        }
    }
}