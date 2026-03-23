package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Voice Management ─────────────────────────────────────────────────

/**
 * A voice available for TTS.
 */
@Serializable
data class Voice(
    @SerialName("voice_id") val voiceId: String = "",
    val name: String = "",
    val provider: String? = null,
    val languages: List<String>? = null,
    val gender: String? = null,
    @SerialName("is_cloned") val isCloned: Boolean? = null,
    @SerialName("preview_url") val previewUrl: String? = null,
)

/**
 * Response from listing voices.
 */
@Serializable
data class VoicesResponse(
    val voices: List<Voice> = emptyList(),
)

/**
 * A file to include in a voice clone request.
 */
data class CloneVoiceFile(
    val filename: String = "",
    val data: ByteArray = ByteArray(0),
    val mimeType: String = "",
)

/**
 * Response from cloning a voice.
 */
@Serializable
data class CloneVoiceResponse(
    @SerialName("voice_id") val voiceId: String = "",
    val name: String = "",
    val status: String? = null,
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
    @SerialName("cloned_by_count") val clonedByCount: Long? = null,
    @SerialName("free_users_allowed") val freeUsersAllowed: Boolean? = null,
)

/**
 * Response from browsing the shared voice library.
 */
@Serializable
data class SharedVoicesResponse(
    val voices: List<SharedVoice> = emptyList(),
    @SerialName("has_more") val hasMore: Boolean = false,
    @SerialName("next_cursor") val nextCursor: String? = null,
)

/**
 * Query parameters for browsing the voice library.
 */
data class VoiceLibraryQuery(
    val query: String? = null,
    @SerialName("page_size") val pageSize: Int? = null,
    val cursor: String? = null,
    val gender: String? = null,
    val language: String? = null,
    @SerialName("use_case") val useCase: String? = null,
)

/**
 * Response from adding a voice from the library.
 */
@Serializable
data class AddVoiceFromLibraryResponse(
    @SerialName("voice_id") val voiceId: String = "",
)
