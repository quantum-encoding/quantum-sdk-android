package ai.quantumencoding.sdk.networking

import ai.quantumencoding.sdk.QuantumStreamException
import ai.quantumencoding.sdk.models.AgentEvent
import ai.quantumencoding.sdk.models.ChatUsage
import ai.quantumencoding.sdk.models.MissionEvent
import ai.quantumencoding.sdk.models.StreamDelta
import ai.quantumencoding.sdk.models.StreamEvent
import ai.quantumencoding.sdk.models.StreamToolUse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull
import okhttp3.Response
import java.io.BufferedReader

/**
 * SSE (Server-Sent Events) parser for streaming API responses.
 *
 * Reads `data: {...}` lines from the response body and emits parsed events
 * as a Kotlin [Flow].
 */
internal object SseClient {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    // ── Client-integrated stream factories ───────────────────────────

    /**
     * Create a chat stream [Flow] by sending the request via [HttpClient] and parsing SSE.
     * The HTTP call is made when the Flow is collected.
     */
    fun parseChatStreamFromClient(http: HttpClient, path: String, body: Any?): Flow<StreamEvent> = flow {
        val response = http.doStreamRaw(path, body)
        val reader = response.body?.byteStream()?.bufferedReader()
            ?: throw QuantumStreamException("Response body is null")

        try {
            reader.forEachSseLine { payload ->
                if (payload == "[DONE]") {
                    emit(StreamEvent(type = "done", done = true))
                    return@flow
                }

                val raw = try {
                    json.parseToJsonElement(payload).jsonObject
                } catch (_: Exception) {
                    emit(StreamEvent(type = "error", error = "parse SSE: invalid JSON"))
                    return@flow
                }

                emit(parseStreamEvent(raw))
            }
        } finally {
            reader.close()
            response.close()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Create an agent stream [Flow] by sending the request via [HttpClient] and parsing SSE.
     */
    fun parseAgentStreamFromClient(http: HttpClient, path: String, body: Any?): Flow<AgentEvent> = flow {
        val response = http.doStreamRaw(path, body)
        val reader = response.body?.byteStream()?.bufferedReader()
            ?: throw QuantumStreamException("Response body is null")

        try {
            reader.forEachSseLine { payload ->
                if (payload == "[DONE]") {
                    emit(AgentEvent(type = "done", done = true))
                    return@flow
                }

                val raw = try {
                    json.parseToJsonElement(payload).jsonObject
                } catch (_: Exception) {
                    emit(AgentEvent(type = "error", error = "parse SSE: invalid JSON"))
                    return@flow
                }

                emit(parseAgentEvent(raw))
            }
        } finally {
            reader.close()
            response.close()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Create a mission stream [Flow] by sending the request via [HttpClient] and parsing SSE.
     */
    fun parseMissionStreamFromClient(http: HttpClient, path: String, body: Any?): Flow<MissionEvent> = flow {
        val response = http.doStreamRaw(path, body)
        val reader = response.body?.byteStream()?.bufferedReader()
            ?: throw QuantumStreamException("Response body is null")

        try {
            reader.forEachSseLine { payload ->
                if (payload == "[DONE]") {
                    emit(MissionEvent(type = "done", done = true))
                    return@flow
                }

                val raw = try {
                    json.parseToJsonElement(payload).jsonObject
                } catch (_: Exception) {
                    emit(MissionEvent(type = "error", error = "parse SSE: invalid JSON"))
                    return@flow
                }

                emit(parseMissionEvent(raw))
            }
        } finally {
            reader.close()
            response.close()
        }
    }.flowOn(Dispatchers.IO)

    // ── Response-based stream parsers (for direct use) ───────────────

    /**
     * Parse an SSE response into a Flow of [StreamEvent] for chat streaming.
     */
    fun parseChatStream(response: Response): Flow<StreamEvent> = flow {
        val reader = response.body?.byteStream()?.bufferedReader()
            ?: throw QuantumStreamException("Response body is null")

        try {
            reader.forEachSseLine { payload ->
                if (payload == "[DONE]") {
                    emit(StreamEvent(type = "done", done = true))
                    return@flow
                }

                val raw = try {
                    json.parseToJsonElement(payload).jsonObject
                } catch (_: Exception) {
                    emit(StreamEvent(type = "error", error = "parse SSE: invalid JSON"))
                    return@flow
                }

                emit(parseStreamEvent(raw))
            }
        } finally {
            reader.close()
            response.close()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Parse an SSE response into a Flow of [AgentEvent].
     */
    fun parseAgentStream(response: Response): Flow<AgentEvent> = flow {
        val reader = response.body?.byteStream()?.bufferedReader()
            ?: throw QuantumStreamException("Response body is null")

        try {
            reader.forEachSseLine { payload ->
                if (payload == "[DONE]") {
                    emit(AgentEvent(type = "done", done = true))
                    return@flow
                }

                val raw = try {
                    json.parseToJsonElement(payload).jsonObject
                } catch (_: Exception) {
                    emit(AgentEvent(type = "error", error = "parse SSE: invalid JSON"))
                    return@flow
                }

                emit(parseAgentEvent(raw))
            }
        } finally {
            reader.close()
            response.close()
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Parse an SSE response into a Flow of [MissionEvent].
     */
    fun parseMissionStream(response: Response): Flow<MissionEvent> = flow {
        val reader = response.body?.byteStream()?.bufferedReader()
            ?: throw QuantumStreamException("Response body is null")

        try {
            reader.forEachSseLine { payload ->
                if (payload == "[DONE]") {
                    emit(MissionEvent(type = "done", done = true))
                    return@flow
                }

                val raw = try {
                    json.parseToJsonElement(payload).jsonObject
                } catch (_: Exception) {
                    emit(MissionEvent(type = "error", error = "parse SSE: invalid JSON"))
                    return@flow
                }

                emit(parseMissionEvent(raw))
            }
        } finally {
            reader.close()
            response.close()
        }
    }.flowOn(Dispatchers.IO)

    // ── Private helpers ──────────────────────────────────────────────

    /**
     * Read SSE lines from a BufferedReader, invoking [onData] for each `data: ` payload.
     */
    private inline fun BufferedReader.forEachSseLine(onData: (String) -> Unit) {
        var line: String?
        while (readLine().also { line = it } != null) {
            val l = line ?: continue
            if (l.startsWith("data: ")) {
                onData(l.substring(6))
            }
        }
    }

    /**
     * Parse a raw JSON object into a [StreamEvent].
     */
    private fun parseStreamEvent(raw: JsonObject): StreamEvent {
        val type = raw["type"]?.jsonPrimitive?.content ?: "unknown"

        return when (type) {
            "content_delta", "thinking_delta" -> {
                val delta = raw["delta"]?.jsonObject?.let { d ->
                    StreamDelta(text = d["text"]?.jsonPrimitive?.content)
                }
                StreamEvent(type = type, delta = delta)
            }

            "tool_use" -> {
                val toolUse = StreamToolUse(
                    id = raw["id"]?.jsonPrimitive?.content ?: "",
                    name = raw["name"]?.jsonPrimitive?.content ?: "",
                    input = raw["input"]?.jsonObject ?: JsonObject(emptyMap()),
                )
                StreamEvent(type = type, toolUse = toolUse)
            }

            "usage" -> {
                val usage = ChatUsage(
                    inputTokens = raw["input_tokens"]?.jsonPrimitive?.intOrNull ?: 0,
                    outputTokens = raw["output_tokens"]?.jsonPrimitive?.intOrNull ?: 0,
                    costTicks = raw["cost_ticks"]?.jsonPrimitive?.longOrNull ?: 0,
                )
                StreamEvent(type = type, usage = usage)
            }

            "error" -> {
                val message = raw["message"]?.jsonPrimitive?.content
                StreamEvent(type = type, error = message)
            }

            else -> StreamEvent(type = type)
        }
    }

    /**
     * Parse a raw JSON object into an [AgentEvent].
     */
    private fun parseAgentEvent(raw: JsonObject): AgentEvent {
        val type = raw["type"]?.jsonPrimitive?.content ?: "unknown"
        val done = type == "done"

        val usage = if (raw.containsKey("input_tokens") || raw.containsKey("output_tokens")) {
            ChatUsage(
                inputTokens = raw["input_tokens"]?.jsonPrimitive?.intOrNull ?: 0,
                outputTokens = raw["output_tokens"]?.jsonPrimitive?.intOrNull ?: 0,
                costTicks = raw["cost_ticks"]?.jsonPrimitive?.longOrNull ?: 0,
            )
        } else null

        val toolUse = raw["tool_use"]?.jsonObject?.let { t ->
            StreamToolUse(
                id = t["id"]?.jsonPrimitive?.content ?: "",
                name = t["name"]?.jsonPrimitive?.content ?: "",
                input = t["input"]?.jsonObject ?: JsonObject(emptyMap()),
            )
        }

        return AgentEvent(
            type = type,
            done = done,
            worker = raw["worker"]?.jsonPrimitive?.content,
            content = raw["content"]?.jsonPrimitive?.content,
            toolUse = toolUse,
            usage = usage,
            error = raw["error"]?.jsonPrimitive?.content,
        )
    }

    /**
     * Parse a raw JSON object into a [MissionEvent].
     */
    private fun parseMissionEvent(raw: JsonObject): MissionEvent {
        val type = raw["type"]?.jsonPrimitive?.content ?: "unknown"
        val done = type == "done"

        val usage = if (raw.containsKey("input_tokens") || raw.containsKey("output_tokens")) {
            ChatUsage(
                inputTokens = raw["input_tokens"]?.jsonPrimitive?.intOrNull ?: 0,
                outputTokens = raw["output_tokens"]?.jsonPrimitive?.intOrNull ?: 0,
                costTicks = raw["cost_ticks"]?.jsonPrimitive?.longOrNull ?: 0,
            )
        } else null

        val toolUse = raw["tool_use"]?.jsonObject?.let { t ->
            StreamToolUse(
                id = t["id"]?.jsonPrimitive?.content ?: "",
                name = t["name"]?.jsonPrimitive?.content ?: "",
                input = t["input"]?.jsonObject ?: JsonObject(emptyMap()),
            )
        }

        return MissionEvent(
            type = type,
            done = done,
            worker = raw["worker"]?.jsonPrimitive?.content,
            content = raw["content"]?.jsonPrimitive?.content,
            toolUse = toolUse,
            usage = usage,
            error = raw["error"]?.jsonPrimitive?.content,
        )
    }
}
