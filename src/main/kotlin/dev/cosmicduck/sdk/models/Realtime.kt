package dev.cosmicduck.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Configuration for a realtime voice session.
 *
 * @property voice Voice to use (e.g. "Sal", "Eve", "Vesper"). Default: "Sal".
 * @property instructions System instructions for the AI.
 * @property sampleRate PCM sample rate in Hz. Default: 24000.
 */
data class RealtimeConfig(
    val voice: String = "Sal",
    val instructions: String = "",
    val sampleRate: Int = 24000,
)

/**
 * Response from creating a realtime session (ephemeral token flow).
 *
 * @property ephemeralToken Ephemeral token for direct WebSocket connection.
 * @property url WebSocket URL to connect to.
 * @property signedUrl Signed WebSocket URL (ElevenLabs).
 * @property sessionId Session ID for billing.
 * @property provider Provider name (e.g. "xai", "elevenlabs").
 */
@Serializable
data class RealtimeSession(
    @SerialName("ephemeral_token") val ephemeralToken: String = "",
    val url: String = "",
    @SerialName("signed_url") val signedUrl: String? = null,
    @SerialName("session_id") val sessionId: String = "",
    val provider: String? = null,
)

/**
 * Internal response wrapper for token refresh.
 */
@Serializable
internal data class RealtimeRefreshResponse(
    @SerialName("ephemeral_token") val ephemeralToken: String = "",
)
