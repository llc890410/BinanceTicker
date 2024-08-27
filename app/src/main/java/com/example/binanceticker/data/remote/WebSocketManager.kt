package com.example.binanceticker.data.remote

import com.example.binanceticker.domain.model.Ticker
import com.example.binanceticker.utils.Constants.BINANCE_WS_STREAM_URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import javax.inject.Inject

class WebSocketManager @Inject constructor(
    private val json: Json,
    private val okHttpClient: OkHttpClient,
    private val coroutineScope: CoroutineScope
) {

    companion object {
        private const val MAX_RETRY_ATTEMPTS = 5
        private const val DELAY_TIME = 5000L
    }

    private var webSocket: WebSocket? = null
    private val subscribedSymbols = ConcurrentHashMap.newKeySet<String>()
    private val requestIdCounter = AtomicInteger(1)
    private var isConnected = AtomicBoolean(false)
    private var retryAttempts = 0

    // 這裡的 MutableSharedFlow 需要定義 buffer 大小 因為：
    // tryEmit (unlike emit) is not a suspending function,
    // so it clearly cannot operate without a buffer where it can store emitted value for all the suspending subscribers to process.
    // On the other hand, emit is suspending, so it does not need buffer space, as it can always suspend in case any of the subscribers are not ready yet.
    private val _tickerData = MutableSharedFlow<Pair<String, Ticker>>(replay = 1, extraBufferCapacity = 64)
    val tickerData: SharedFlow<Pair<String, Ticker>> = _tickerData.asSharedFlow()

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
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Timber.d("WebSocket connected")
                isConnected.set(true)
                retryAttempts = 0
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleWebSocketMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Timber.e(t, "WebSocket connection failed")
                isConnected.set(false)
                retryConnection()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Timber.d("WebSocket closed: $reason")
                isConnected.set(false)
            }
        })
    }

    private fun disconnectWebSocket() {
        try {
            webSocket?.close(1000, "Client closed")
        } catch (e: Exception) {
            Timber.e("Error closing WebSocket: ${e.message}")
        } finally {
            webSocket = null
            isConnected.set(false)
            subscribedSymbols.clear()
        }
    }

    private fun retryConnection() {
        if (retryAttempts >= MAX_RETRY_ATTEMPTS) {
            Timber.e("Maximum retry attempts reached. Giving up.")
            return
        }

        coroutineScope.launch {
            delay(DELAY_TIME)
            retryAttempts++
            Timber.d("Retrying connection... Attempt $retryAttempts")
            connectWebSocket()
        }
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
        val jsonObject = try {
            JSONObject(message)
        } catch (e: Exception) {
            Timber.e("Invalid JSON: ${e.message}")
            return
        }

        when {
            jsonObject.has("result") -> { // 如果包含 "result"，則表示這是一個訂閱確認(Subscribe/Unsubscribe)，直接忽略
                Timber.d("Subscription confirmed with id: ${jsonObject.getInt("id")}")
                return
            }
            jsonObject.has("stream") -> {
                val data = jsonObject.getString("data")
                processTickerData(data)
            }
            else -> {
                processTickerData(message)
            }
        }
    }

    private fun processTickerData(data: String) {
        val dataObject = try {
            JSONObject(data)
        } catch (e: Exception) {
            Timber.e("Invalid data JSON: ${e.message}, data: $data")
            return
        }

        val symbol: String = dataObject.optString("s").lowercase()
        if (symbol.isEmpty()) {
            Timber.e("WebSocket message received without a symbol: $data")
            return
        }

        val ticker = try {
            json.decodeFromString<Ticker>(data)
        } catch (e: Exception) {
            Timber.e("Failed to parse ticker data: ${e.message}, data: $data")
            return
        }

        _tickerData.tryEmit(symbol.uppercase() to ticker)
    }
}