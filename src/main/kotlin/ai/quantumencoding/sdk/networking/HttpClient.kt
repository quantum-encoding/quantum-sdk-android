package ai.quantumencoding.sdk.networking

import ai.quantumencoding.sdk.ApiErrorBody
import ai.quantumencoding.sdk.QuantumApiException
import ai.quantumencoding.sdk.QuantumNetworkException
import ai.quantumencoding.sdk.models.ResponseMeta
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resumeWithException

/**
 * Internal HTTP client wrapping OkHttp with authentication and JSON handling.
 *
 * All network calls are suspend functions that dispatch on OkHttp's thread pool
 * and resume on the caller's coroutine dispatcher.
 */
internal class HttpClient(
    private val apiKey: String,
    private val baseUrl: String,
    httpClient: OkHttpClient?,
) {
    val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
        explicitNulls = false
    }

    private val client: OkHttpClient = httpClient ?: OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)   // Long timeout for video/audio generation
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /** The base URL for building WebSocket URLs. */
    val wsBaseUrl: String
        get() = baseUrl
            .replace("https://", "wss://")
            .replace("http://", "ws://")

    /** The API key for WebSocket auth. */
    val rawApiKey: String get() = apiKey

    /**
     * Send a JSON request and decode the JSON response.
     *
     * Uses reified type parameter for response deserialization.
     * Request body is serialized using the runtime type's serializer.
     *
     * @param method HTTP method (GET, POST, PUT, DELETE).
     * @param path API path (e.g. "/qai/v1/chat").
     * @param body Request body to serialize as JSON, or null for bodyless requests.
     * @return Pair of deserialized response body and response metadata.
     */
    suspend inline fun <reified T> doJson(
        method: String,
        path: String,
        body: Any? = null,
    ): Pair<T, ResponseMeta> {
        val requestBuilder = Request.Builder()
            .url("$baseUrl$path")
            .header("Authorization", "Bearer $apiKey")

        when {
            body != null -> {
                val jsonBody = serializeBody(body)
                val requestBody = jsonBody.toRequestBody(jsonMediaType)
                requestBuilder.method(method, requestBody)
                requestBuilder.header("Content-Type", "application/json")
            }
            method == "POST" || method == "PUT" || method == "PATCH" -> {
                requestBuilder.method(method, "{}".toRequestBody(jsonMediaType))
                requestBuilder.header("Content-Type", "application/json")
            }
            else -> {
                requestBuilder.method(method, null)
            }
        }

        val response = executeAsync(requestBuilder.build())
        val meta = extractMeta(response)

        if (!response.isSuccessful) {
            throw parseApiError(response, meta.requestId)
        }

        val responseBody = response.body?.string()
            ?: throw QuantumNetworkException("Empty response body")

        val data = json.decodeFromString<T>(responseBody)
        return data to meta
    }

    /**
     * Serialize a body object to JSON string. Handles kotlinx.serialization
     * annotated classes and plain Map instances.
     */
    @PublishedApi
    internal fun serializeBody(body: Any): String {
        return when (body) {
            is String -> body
            else -> {
                // Use the Json instance to encode; kotlinx.serialization will find
                // the serializer via the @Serializable annotation or built-in serializers.
                @Suppress("UNCHECKED_CAST")
                val serializer = json.serializersModule.serializer(body::class.java) as KSerializer<Any>
                json.encodeToString(serializer, body)
            }
        }
    }

    /**
     * Send a JSON request expecting an SSE (text/event-stream) response.
     * Returns the raw OkHttp Response for SSE parsing.
     */
    suspend fun doStreamRaw(
        path: String,
        body: Any?,
    ): Response {
        val jsonBody = if (body != null) serializeBody(body) else "{}"

        val request = Request.Builder()
            .url("$baseUrl$path")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .header("Accept", "text/event-stream")
            .post(jsonBody.toRequestBody(jsonMediaType))
            .build()

        val response = executeAsync(request)

        if (!response.isSuccessful) {
            val requestId = response.header("X-QAI-Request-Id") ?: ""
            throw parseApiError(response, requestId)
        }

        return response
    }

    /**
     * Execute an OkHttp request asynchronously, suspending until complete.
     */
    @PublishedApi
    internal suspend fun executeAsync(request: Request): Response =
        suspendCancellableCoroutine { continuation ->
            val call = client.newCall(request)

            continuation.invokeOnCancellation {
                call.cancel()
            }

            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (!continuation.isCompleted) {
                        continuation.resumeWithException(
                            QuantumNetworkException("Network request failed: ${e.message}", e)
                        )
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!continuation.isCompleted) {
                        continuation.resumeWith(Result.success(response))
                    }
                }
            })
        }

    /**
     * Extract response metadata from headers.
     */
    @PublishedApi
    internal fun extractMeta(response: Response): ResponseMeta {
        val costHeader = response.header("X-QAI-Cost-Ticks")
        return ResponseMeta(
            costTicks = costHeader?.toLongOrNull() ?: 0,
            requestId = response.header("X-QAI-Request-Id") ?: "",
            model = response.header("X-QAI-Model") ?: "",
        )
    }

    /**
     * Parse an API error response.
     */
    @PublishedApi
    internal fun parseApiError(response: Response, requestId: String): QuantumApiException {
        val bodyText = try {
            response.body?.string() ?: ""
        } catch (_: Exception) {
            ""
        }

        var code = response.message.ifEmpty { "Unknown" }
        var message = bodyText

        try {
            val parsed = json.decodeFromString<ApiErrorBody>(bodyText)
            if (parsed.error?.message != null) {
                message = parsed.error.message
                code = parsed.error.code ?: parsed.error.type ?: code
            }
        } catch (_: Exception) {
            // Body is not JSON, use raw text as message.
        }

        return QuantumApiException(
            statusCode = response.code,
            code = code,
            message = message,
            requestId = requestId.ifEmpty { null },
        )
    }
}
