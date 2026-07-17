package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── HeyGen Avatar Realtime (Broadcast) ───────────────────────────────
//
// A realtime session makes an avatar speak live and publishes a plain HLS
// stream (720p). Sessions are PREPAID: the entire `max_duration_seconds`
// block is charged at create time and is NOT refunded on early cancel
// (cancelling only stops the upstream meter).
//
// Recommended flow:
// 1. `createAvatarRealtimeSession` → `streamId`
// 2. Poll `getAvatarRealtimeSession` (~2s) until `status == "streaming"`,
//    then play `hlsUrl`
// 3. For `text_stream` sessions, append text with `sendAvatarRealtimeText`
//    and close with `isFinal = true` (idle timeout is ~30s without new text)
// 4. `cancelAvatarRealtimeSession` as soon as you're done

/**
 * Audio input union for `audio`-type realtime sessions,
 * discriminated by [inputType] (wire field `type`).
 */
@Serializable
data class AvatarAudioInput(
    /** Input kind: "url" | "asset_id" | "base64". */
    @SerialName("type") val inputType: String,
    /** Publicly accessible HTTPS URL (when `inputType == "url"`). */
    val url: String? = null,
    /** HeyGen asset id from an asset upload (when `inputType == "asset_id"`). */
    @SerialName("asset_id") val assetId: String? = null,
    /** MIME type, e.g. "audio/mpeg" (when `inputType == "base64"`). */
    @SerialName("media_type") val mediaType: String? = null,
    /** Base64-encoded audio bytes (when `inputType == "base64"`). */
    val data: String? = null,
)

/**
 * Request body for creating a live avatar session (prepaid).
 */
@Serializable
data class AvatarRealtimeRequest(
    /** Session kind: "tts" | "audio" | "text_stream". */
    @SerialName("type") val sessionType: String,
    /** HeyGen photo-avatar / motion-avatar look id (required for all kinds). */
    @SerialName("avatar_id") val avatarId: String,
    /** Voice id — required for "tts" and "text_stream", must be omitted for "audio". */
    @SerialName("voice_id") val voiceId: String? = null,
    /** The fixed script ("tts") or the initial non-empty seed ("text_stream"). */
    val text: String? = null,
    /** Audio input — required for "audio", must be omitted for "tts"/"text_stream". */
    val audio: AvatarAudioInput? = null,
    /**
     * Prepaid block in seconds (1–3600). The whole block is charged at
     * create time; early cancel does NOT refund.
     */
    @SerialName("max_duration_seconds") val maxDurationSeconds: Int,
)

/**
 * Response from creating a live avatar session.
 */
@Serializable
data class AvatarRealtimeCreateResponse(
    /** Session id — use in the status/text/cancel calls. */
    @SerialName("stream_id") val streamId: String = "",
    /** Always "pending" at create. */
    val status: String = "",
    /** Echo of `max_duration_seconds`. */
    @SerialName("prepaid_seconds") val prepaidSeconds: Int = 0,
    /** Ticks charged for the prepaid block. */
    @SerialName("cost_ticks") val costTicks: Long = 0,
    /** Post-deduction credit balance in ticks (from the X-QAI-Balance-After header). */
    @SerialName("balance_after") val balanceAfter: Long = 0,
    /** Unique request identifier. */
    @SerialName("request_id") val requestId: String = "",
)

/**
 * Response from a session status check.
 */
@Serializable
data class AvatarRealtimeStatusResponse(
    /** Session id. */
    @SerialName("stream_id") val streamId: String = "",
    /** "pending" | "streaming" | "completed" | "error". */
    val status: String = "",
    /** HLS `.m3u8` playback URL (720p); present once streaming. */
    @SerialName("hls_url") val hlsUrl: String? = null,
    /** Failure detail when `status == "error"`. */
    @SerialName("error_message") val errorMessage: String? = null,
    /** On completed text_stream sessions: "final_marker" | "idle_timeout". */
    @SerialName("end_reason") val endReason: String? = null,
    /** Unique request identifier. */
    @SerialName("request_id") val requestId: String = "",
)

/**
 * Request body for appending a text delta to a `text_stream` session.
 */
@Serializable
data class AvatarRealtimeTextRequest(
    /**
     * Text fragment to append (a token or coalesced batch). Required unless
     * [isFinal] is true, in which case it may be empty (empty deltas are
     * omitted from the wire).
     */
    val delta: String = "",
    /**
     * True closes the text input (appending afterwards fails upstream with
     * a 410 provider_error). Wire field: `final`.
     */
    @SerialName("final") val isFinal: Boolean = false,
) {
    companion object {
        /** A delta-append request. */
        fun delta(delta: String) = AvatarRealtimeTextRequest(delta = delta, isFinal = false)

        /** A close-the-stream request (empty final marker). */
        fun finalMarker() = AvatarRealtimeTextRequest(delta = "", isFinal = true)
    }
}

/**
 * Response from appending a text delta.
 */
@Serializable
data class AvatarRealtimeTextResponse(
    /** Always true on success. */
    val ok: Boolean = false,
    /** Total text bytes buffered for the session so far. */
    @SerialName("buffered_bytes") val bufferedBytes: Long = 0,
    /** Echo of the request's `final` flag. Wire field: `final`. */
    @SerialName("final") val isFinal: Boolean = false,
    /** Unique request identifier. */
    @SerialName("request_id") val requestId: String = "",
)

/**
 * Response from cancelling a session early.
 */
@Serializable
data class AvatarRealtimeCancelResponse(
    /** Session id. */
    @SerialName("stream_id") val streamId: String = "",
    /**
     * True = this call initiated cancellation; false = the session was
     * already terminal (cancel is idempotent).
     */
    val cancelled: Boolean = false,
    /** Unique request identifier. */
    @SerialName("request_id") val requestId: String = "",
)
