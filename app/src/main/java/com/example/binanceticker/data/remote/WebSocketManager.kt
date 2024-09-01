package com.example.binanceticker.data.remote

import com.example.binanceticker.domain.model.WebSocketKline
import com.example.binanceticker.domain.model.WebSocketKlineResponse
import com.example.binanceticker.domain.model.WebSocketTicker
import com.example.binanceticker.utils.Constants.BINANCE_WS_STREAM_URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
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
    enum class WebSocketState {
        CONNECTED,
        DISCONNECTED,
        RECONNECTING,
        FAILED
    }

    companion object {
        private const val MAX_RETRY_ATTEMPTS = 5
        private const val DELAY_TIME = 5000L
        private const val SUBSCRIBE = "SUBSCRIBE"
        private const val UNSUBSCRIBE = "UNSUBSCRIBE"
        private const val LIST_SUBSCRIPTIONS = "LIST_SUBSCRIPTIONS"
    }

    private var webSocket: WebSocket? = null
    private val activeStreams = ConcurrentHashMap.newKeySet<String>()
    private val requestIdCounter = AtomicInteger(1)
    private var isConnected = AtomicBoolean(false)
    private var retryAttempts = 0

    private val _webSocketState = MutableStateFlow(WebSocketState.DISCONNECTED)
    val webSocketState: StateFlow<WebSocketState> = _webSocketState

    private val _ticker = MutableSharedFlow<WebSocketTicker>()
    val ticker: SharedFlow<WebSocketTicker> = _ticker.asSharedFlow()
    private val _kline = MutableSharedFlow<WebSocketKline>()
    val kline: SharedFlow<WebSocketKline> = _kline.asSharedFlow()

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
                _webSocketState.value = WebSocketState.CONNECTED
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                handleWebSocketMessage(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Timber.e(t, "WebSocket connection failed")
                isConnected.set(false)
                _webSocketState.value = WebSocketState.FAILED
                retryConnection()
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Timber.d("WebSocket closed: $reason")
                isConnected.set(false)
                _webSocketState.value = WebSocketState.DISCONNECTED
            }
        })
    }

    private fun disconnectWebSocket() {
        try {
            webSocket?.close(1000, "Client closed")
            _webSocketState.value = WebSocketState.DISCONNECTED
        } catch (e: Exception) {
            Timber.e("Error closing WebSocket: ${e.message}")
        } finally {
            webSocket = null
            isConnected.set(false)
            activeStreams.clear()
        }
    }

    private fun retryConnection() {
        if (retryAttempts >= MAX_RETRY_ATTEMPTS) {
            Timber.e("Maximum retry attempts reached. Giving up.")
            _webSocketState.value = WebSocketState.FAILED
            return
        }

        coroutineScope.launch {
            _webSocketState.value = WebSocketState.RECONNECTING
            delay(DELAY_TIME)
            retryAttempts++
            Timber.d("Retrying connection... Attempt $retryAttempts")
            connectWebSocket()
        }
    }

    fun subscribeTicker(symbols: List<String>) {
        val streams = symbols.map { "${it.lowercase()}@ticker" }
        if (activeStreams.addAll(streams)) {
            val message = createSubscriptionMessage(SUBSCRIBE, streams)
            webSocket?.send(message)
        }
    }

    fun unsubscribeTicker(symbols: List<String>) {
        val streams = symbols.map { "${it.lowercase()}@ticker" }
        if (activeStreams.removeAll(streams.toSet())) {
            val message = createSubscriptionMessage(UNSUBSCRIBE, streams)
            webSocket?.send(message)
        }
    }

    fun subscribeKline(symbols: List<String>, interval: String) {
        val streams = symbols.map { "${it.lowercase()}@kline_${interval.lowercase()}" }
        if (activeStreams.addAll(streams)) {
            val message = createSubscriptionMessage(SUBSCRIBE, streams)
            webSocket?.send(message)
        }
    }

    fun unsubscribeKline(symbols: List<String>, interval: String) {
        val streams = symbols.map { "${it.lowercase()}@kline_${interval.lowercase()}" }
        if (activeStreams.removeAll(streams.toSet())) {
            val message = createSubscriptionMessage(UNSUBSCRIBE, streams)
            webSocket?.send(message)
        }
    }


    fun listSubscriptions() {
        val message = createSubscriptionMessage(LIST_SUBSCRIPTIONS, emptyList())
        webSocket?.send(message)
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
            jsonObject.has("code") -> {
                val code = jsonObject.getInt("code")
                val msg = jsonObject.getString("msg")
                Timber.e("Error code: $code, message: $msg")
            }
            jsonObject.has("result") -> {
                handleSubscriptionResult(jsonObject)
            }
            jsonObject.has("stream") -> {
                processData(jsonObject.getString("data"))
            }
            else -> {
                processData(message)
            }
        }
    }

    private fun handleSubscriptionResult(jsonObject: JSONObject) {
        if (jsonObject.get("result") is JSONArray) {
            val subscriptions = jsonObject.getJSONArray("result")
            Timber.d("Current subscriptions: $subscriptions")
        } else {
            Timber.d("Subscription confirmed with id: ${jsonObject.getInt("id")}")
        }
    }

    private fun processData(data: String) {
        val jsonObject = try {
            JSONObject(data)
        } catch (e: Exception) {
            Timber.e("Invalid JSON: ${e.message}")
            return
        }

        val eventType = jsonObject.optString("e")
        val symbol = jsonObject.optString("s").lowercase()

        if (symbol.isEmpty()) {
            Timber.e("WebSocket message received without a symbol: $data")
            return
        }

        when (eventType) {
            "24hrTicker" -> processTickerData(jsonObject)
            "kline" -> processKlineData(jsonObject)
            else -> Timber.w("Unhandled event type: $eventType")
        }
    }

    private fun processTickerData(dataObject: JSONObject) {
        val webSocketTicker = try {
            json.decodeFromString<WebSocketTicker>(dataObject.toString())
        } catch (e: Exception) {
            Timber.e("Failed to parse ticker data: ${e.message}, data: $dataObject")
            return
        }

        coroutineScope.launch {
            _ticker.emit(webSocketTicker)
        }
    }

    private fun processKlineData(dataObject: JSONObject) {
        val webSocketKlineResponse = try {
            json.decodeFromString<WebSocketKlineResponse>(dataObject.toString())
        } catch (e: Exception) {
            Timber.e("Failed to parse kline data: ${e.message}, data: $dataObject")
            return
        }

        coroutineScope.launch {
            _kline.emit(webSocketKlineResponse.kline)
        }
    }
}