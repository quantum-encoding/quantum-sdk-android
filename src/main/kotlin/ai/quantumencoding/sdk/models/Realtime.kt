package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Configuration for a realtime voice session.
 */
data class RealtimeConfig(
    val voice: String = "Sal",
    val instructions: String = "",
    val sampleRate: Int = 24000,
    val tools: List<JsonElement> = emptyList(),
    val model: String = "",
)

/**
 * Parsed incoming event from the realtime API.
 */
sealed class RealtimeEvent {
    data object SessionReady : RealtimeEvent()
    data class AudioDelta(val delta: String) : RealtimeEvent()
    data class TranscriptDelta(val delta: String, val source: String) : RealtimeEvent()
    data class TranscriptDone(val transcript: String, val source: String) : RealtimeEvent()
    data object SpeechStarted : RealtimeEvent()
    data object SpeechStopped : RealtimeEvent()
    data class FunctionCall(val name: String, val callId: String, val arguments: String) : RealtimeEvent()
    data object ResponseDone : RealtimeEvent()
    data class Error(val message: String) : RealtimeEvent()
    data class Unknown(val raw: JsonElement) : RealtimeEvent()
}

/**
 * Response from creating a realtime session (ephemeral token flow).
 */
@Serializable
data class RealtimeSession(
    @SerialName("ephemeral_token") val ephemeralToken: String = "",
    val url: String = "",
    @SerialName("signed_url") val signedUrl: String = "",
    @SerialName("session_id") val sessionId: String = "",
    val provider: String = "",
)

/**
 * Backwards-compatible alias for [RealtimeSession].
 */
typealias RealtimeSessionResponse = RealtimeSession
