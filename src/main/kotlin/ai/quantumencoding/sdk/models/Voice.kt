package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Voice Management ─────────────────────────────────────────────────

/**
 * Information about a voice.
 */
@Serializable
data class VoiceInfo(
    @SerialName("voice_id") val voiceId: String = "",
    val name: String = "",
    val provider: String = "",
    @SerialName("preview_url") val previewUrl: String? = null,
)

/**
 * Response from listing voices.
 */
@Serializable
data class VoicesResponse(
    val voices: List<VoiceInfo> = emptyList(),
)

/**
 * Request body for voice cloning (ElevenLabs).
 *
 * @property name Name for the cloned voice.
 * @property audioSamples Base64-encoded audio samples.
 * @property description Optional description.
 */
@Serializable
data class CloneVoiceRequest(
    val name: String,
    @SerialName("audio_samples") val audioSamples: List<String>,
    val description: String? = null,
)

/**
 * Response from voice cloning.
 */
@Serializable
data class CloneVoiceResponse(
    @SerialName("voice_id") val voiceId: String = "",
    val name: String = "",
)

// ── Voice Library ────────────────────────────────────────────────────

/**
 * A shared voice from the community voice library.
 */
@Serializable
data class SharedVoice(
    @SerialName("public_owner_id") val publicOwnerId: String = "",
    @SerialName("voice_id") val voiceId: String = "",
    val name: String = "",
    val category: String? = null,
    val description: String? = null,
    @SerialName("preview_url") val previewUrl: String? = null,
    val gender: String? = null,
    val age: String? = null,
    val accent: String? = null,
    val language: String? = null,
    @SerialName("use_case") val useCase: String? = null,
    val rate: Double? = null,
    @SerialName("cloned_by_count") val clonedByCount: Int? = null,
    @SerialName("free_users_allowed") val freeUsersAllowed: Boolean? = null,
)

/**
 * Response from browsing the shared voice library.
 */
@Serializable
data class SharedVoicesResponse(
    val voices: List<SharedVoice> = emptyList(),
    @SerialName("next_cursor") val nextCursor: String? = null,
    @SerialName("has_more") val hasMore: Boolean = false,
)

/**
 * Query parameters for browsing the voice library.
 */
data class VoiceLibraryQuery(
    val query: String? = null,
    val pageSize: Int? = null,
    val cursor: String? = null,
    val gender: String? = null,
    val language: String? = null,
    val useCase: String? = null,
)

/**
 * Request body for adding a shared voice from the library.
 */
@Serializable
data class AddVoiceFromLibraryRequest(
    @SerialName("public_owner_id") val publicOwnerId: String,
    @SerialName("voice_id") val voiceId: String,
    val name: String? = null,
)

/**
 * Response from adding a shared voice.
 */
@Serializable
data class AddVoiceFromLibraryResponse(
    @SerialName("voice_id") val voiceId: String = "",
)
