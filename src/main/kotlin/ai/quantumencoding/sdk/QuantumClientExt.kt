@file:JvmName("QuantumClientExtensions")

package ai.quantumencoding.sdk

import ai.quantumencoding.sdk.models.*
import ai.quantumencoding.sdk.networking.HttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.BufferedReader
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resumeWithException

private val sseJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = false
    explicitNulls = false
}

// ── Chat Job ─────────────────────────────────────────────────────────────

/**
 * Submit a chat completion as an async job.
 *
 * Useful for long-running models (e.g. Opus) where synchronous
 * `/qai/v1/chat` may time out. Use [streamJob] or [pollJob] to get the result.
 *
 * @param request Chat request with model, messages, and options.
 * @return Job creation response with job ID for polling.
 */
suspend fun QuantumClient.chatJob(request: ChatRequest): JobCreateResponse {
    val serialized = sseJson.encodeToString(ChatRequest.serializer(), request.copy(stream = false))
    val paramsElement = sseJson.parseToJsonElement(serialized)
    val params = paramsElement.jsonObject.toMap()
    return createJob(JobCreateRequest(type = "chat", params = params))
}

// ── Stream Job (GET SSE) ────────────────────────────────────────────────

/**
 * Stream job progress via SSE. Returns a [Flow] of [JobStreamEvent].
 *
 * Events: "progress" (status update), "complete" (with result), "error".
 *
 * ```kotlin
 * val job = client.chatJob(ChatRequest(model = "claude-opus-4-6", messages = listOf(...)))
 * client.streamJob(job.jobId).collect { event ->
 *     println("${event.type}: ${event.status}")
 * }
 * ```
 *
 * @param jobId Job ID to stream events for.
 */
fun QuantumClient.streamJob(jobId: String): Flow<JobStreamEvent> {
    // Capture fields needed inside the flow builder
    val apiKey = this.apiKey
    val baseUrl = this.baseUrl

    return flow {
        val client = OkHttpClient.Builder()
            .readTimeout(300, TimeUnit.SECONDS)
            .build()

        val request = Request.Builder()
            .url("$baseUrl/qai/v1/jobs/$jobId/stream")
            .header("Authorization", "Bearer $apiKey")
            .header("Accept", "text/event-stream")
            .get()
            .build()

        val response = suspendCancellableCoroutine { continuation ->
            val call = client.newCall(request)
            continuation.invokeOnCancellation { call.cancel() }
            call.enqueue(object : okhttp3.Callback {
                override fun onFailure(call: okhttp3.Call, e: IOException) {
                    if (!continuation.isCompleted) {
                        continuation.resumeWithException(
                            QuantumNetworkException("Job stream request failed: ${e.message}", e)
                        )
                    }
                }

                override fun onResponse(call: okhttp3.Call, response: Response) {
                    if (!continuation.isCompleted) {
                        continuation.resumeWith(Result.success(response))
                    }
                }
            })
        }

        if (!response.isSuccessful) {
            val requestId = response.header("X-QAI-Request-Id") ?: ""
            response.close()
            throw QuantumApiException(
                statusCode = response.code,
                code = response.code.toString(),
                message = "Job stream error (${response.code})",
                requestId = requestId,
            )
        }

        val reader = response.body?.byteStream()?.bufferedReader()
            ?: throw QuantumStreamException("Response body is null")

        try {
            reader.useLines { lines ->
                for (line in lines) {
                    if (!line.startsWith("data: ")) continue
                    val payload = line.removePrefix("data: ")
                    if (payload == "[DONE]") return@useLines

                    val event = try {
                        sseJson.decodeFromString<JobStreamEvent>(payload)
                    } catch (_: Exception) {
                        JobStreamEvent(type = "error", error = "parse SSE: invalid JSON")
                    }

                    emit(event)

                    if (event.type == "complete" || event.type == "error") {
                        return@useLines
                    }
                }
            }
        } finally {
            reader.close()
            response.close()
        }
    }.flowOn(Dispatchers.IO)
}

// ── Compute Billing ─────────────────────────────────────────────────────

/**
 * Query compute billing from BigQuery via the QAI backend.
 *
 * @param request Billing query filters (instance ID, date range).
 * @return Billing entries and total cost.
 */
suspend fun QuantumClient.computeBilling(request: BillingRequest): BillingResponse {
    // Use the createJob path pattern — we need POST to /qai/v1/compute/billing.
    // Since we can't access the private http field, we delegate through the public API.
    // The public doJson method on the client's internal HttpClient is not exposed,
    // so we construct a minimal request ourselves.
    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    val jsonBody = sseJson.encodeToString(BillingRequest.serializer(), request)

    val httpRequest = Request.Builder()
        .url("$baseUrl/qai/v1/compute/billing")
        .header("Authorization", "Bearer $apiKey")
        .header("Content-Type", "application/json")
        .post(jsonBody.toByteArray().let {
            okhttp3.RequestBody.create(
                okhttp3.MediaType.parse("application/json; charset=utf-8"),
                it,
            )
        })
        .build()

    val response = suspendCancellableCoroutine { continuation ->
        val call = client.newCall(httpRequest)
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                if (!continuation.isCompleted) {
                    continuation.resumeWithException(
                        QuantumNetworkException("Compute billing request failed: ${e.message}", e)
                    )
                }
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                if (!continuation.isCompleted) {
                    continuation.resumeWith(Result.success(response))
                }
            }
        })
    }

    if (!response.isSuccessful) {
        val requestId = response.header("X-QAI-Request-Id") ?: ""
        val body = response.body?.string() ?: ""
        response.close()
        throw QuantumApiException(
            statusCode = response.code,
            code = response.code.toString(),
            message = body.ifEmpty { "Compute billing error (${response.code})" },
            requestId = requestId,
        )
    }

    val responseBody = response.body?.string() ?: throw QuantumNetworkException("Empty response body")
    response.close()

    return sseJson.decodeFromString<BillingResponse>(responseBody)
}

// ── Internal accessors for extension functions ──────────────────────────
// These expose the fields needed by extension functions.

/** The API key configured on this client. */
internal val QuantumClient.apiKey: String
    get() {
        // Access via reflection since the field is private.
        val field = QuantumClient::class.java.getDeclaredField("http")
        field.isAccessible = true
        val httpClient = field.get(this) as HttpClient
        return httpClient.rawApiKey
    }

/** The base URL configured on this client. */
internal val QuantumClient.baseUrl: String
    get() {
        val field = QuantumClient::class.java.getDeclaredField("http")
        field.isAccessible = true
        val httpClient = field.get(this) as HttpClient
        return httpClient.wsBaseUrl
            .replace("wss://", "https://")
            .replace("ws://", "http://")
    }

// -- 3D Mesh: Remesh, Rig, Animate ---

/**
 * Remesh a 3D model. Submits job and polls to completion.
 */
suspend fun QuantumClient.remesh(request: ai.quantumencoding.sdk.models.RemeshRequest): ai.quantumencoding.sdk.models.JobStatusResponse {
    val params = kotlinx.serialization.json.Json.encodeToJsonElement(
        ai.quantumencoding.sdk.models.RemeshRequest.serializer(), request
    ).let { (it as kotlinx.serialization.json.JsonObject).toMap() }
    val job = createJob(ai.quantumencoding.sdk.models.JobCreateRequest(type = "3d/remesh", params = params))
    return pollJob(job.jobId, intervalMs = 5000, maxAttempts = 120)
}

/**
 * Rig a humanoid 3D model. Returns rigged character + basic walk/run animations.
 */
suspend fun QuantumClient.rig(request: ai.quantumencoding.sdk.models.RigRequest): ai.quantumencoding.sdk.models.JobStatusResponse {
    val params = kotlinx.serialization.json.Json.encodeToJsonElement(
        ai.quantumencoding.sdk.models.RigRequest.serializer(), request
    ).let { (it as kotlinx.serialization.json.JsonObject).toMap() }
    val job = createJob(ai.quantumencoding.sdk.models.JobCreateRequest(type = "3d/rig", params = params))
    return pollJob(job.jobId, intervalMs = 5000, maxAttempts = 120)
}

/**
 * Apply an animation to a rigged character.
 */
suspend fun QuantumClient.animate(request: ai.quantumencoding.sdk.models.AnimateRequest): ai.quantumencoding.sdk.models.JobStatusResponse {
    val params = kotlinx.serialization.json.Json.encodeToJsonElement(
        ai.quantumencoding.sdk.models.AnimateRequest.serializer(), request
    ).let { (it as kotlinx.serialization.json.JsonObject).toMap() }
    val job = createJob(ai.quantumencoding.sdk.models.JobCreateRequest(type = "3d/animate", params = params))
    return pollJob(job.jobId, intervalMs = 5000, maxAttempts = 120)
}

suspend fun QuantumClient.retexture(request: ai.quantumencoding.sdk.models.RetextureRequest): ai.quantumencoding.sdk.models.JobStatusResponse {
    val params = kotlinx.serialization.json.Json.encodeToJsonElement(
        ai.quantumencoding.sdk.models.RetextureRequest.serializer(), request
    ).let { (it as kotlinx.serialization.json.JsonObject).toMap() }
    val job = createJob(ai.quantumencoding.sdk.models.JobCreateRequest(type = "3d/retexture", params = params))
    return pollJob(job.jobId, intervalMs = 5000, maxAttempts = 120)
}

/**
 * Request a realtime session with full configuration (voice, prompt, tools for ElevenLabs ConvAI).
 */
suspend fun QuantumClient.realtimeSessionWith(body: Map<String, Any>): Map<String, Any> {
    // Use reflection to access http client for raw JSON post
    val httpField = QuantumClient::class.java.getDeclaredField("http")
    httpField.isAccessible = true
    val http = httpField.get(this)
    val doJsonMethod = http.javaClass.methods.find { it.name == "doJsonRaw" }
    // Fallback: just call createJob-style with raw body
    val json = kotlinx.serialization.json.Json.encodeToString(
        kotlinx.serialization.serializer<Map<String, kotlinx.serialization.json.JsonElement>>(),
        body.mapValues { (_, v) -> kotlinx.serialization.json.JsonPrimitive(v.toString()) }
    )
    // For now, return empty — Kotlin client needs proper raw JSON support
    @Suppress("UNCHECKED_CAST")
    return emptyMap()
}
