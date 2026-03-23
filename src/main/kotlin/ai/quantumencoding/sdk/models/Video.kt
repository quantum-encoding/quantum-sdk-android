package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// ── Video Generation ─────────────────────────────────────────────────

/**
 * Request body for video generation.
 */
@Serializable
data class VideoRequest(
    val model: String = "",
    val prompt: String = "",
    @SerialName("duration_seconds") val durationSeconds: Int? = null,
    @SerialName("aspect_ratio") val aspectRatio: String? = null,
)

/**
 * A single generated video.
 */
@Serializable
data class GeneratedVideo(
    val base64: String = "",
    val format: String = "",
    @SerialName("size_bytes") val sizeBytes: Long = 0,
    val index: Int = 0,
)

/**
 * Response from video generation.
 */
@Serializable
data class VideoResponse(
    val videos: List<GeneratedVideo> = emptyList(),
    val model: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)

// ── Job Response (shared by HeyGen endpoints) ───────────────────────

/**
 * Response from async video job submission.
 */
@Serializable
data class JobResponse(
    @SerialName("job_id") val jobId: String = "",
    val status: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
)

// ── HeyGen Studio ────────────────────────────────────────────────────

/**
 * A clip in a studio video.
 */
@Serializable
data class StudioClip(
    @SerialName("avatar_id") val avatarId: String? = null,
    @SerialName("voice_id") val voiceId: String? = null,
    val script: String? = null,
    val background: JsonElement? = null,
)

/**
 * Request body for HeyGen studio video creation.
 */
@Serializable
data class StudioVideoRequest(
    val title: String? = null,
    val clips: List<StudioClip> = emptyList(),
    val dimension: String? = null,
    @SerialName("aspect_ratio") val aspectRatio: String? = null,
)

// ── HeyGen Translate ─────────────────────────────────────────────────

/**
 * Request body for video translation.
 */
@Serializable
data class TranslateRequest(
    @SerialName("video_url") val videoUrl: String? = null,
    @SerialName("video_base64") val videoBase64: String? = null,
    @SerialName("target_language") val targetLanguage: String = "",
    @SerialName("source_language") val sourceLanguage: String? = null,
)

// ── HeyGen Photo Avatar ──────────────────────────────────────────────

/**
 * Request body for creating a photo avatar video.
 */
@Serializable
data class PhotoAvatarRequest(
    @SerialName("photo_base64") val photoBase64: String = "",
    val script: String = "",
    @SerialName("voice_id") val voiceId: String? = null,
    @SerialName("aspect_ratio") val aspectRatio: String? = null,
)

// ── HeyGen Digital Twin ──────────────────────────────────────────────

/**
 * Request body for digital twin video generation.
 */
@Serializable
data class DigitalTwinRequest(
    @SerialName("avatar_id") val avatarId: String = "",
    val script: String = "",
    @SerialName("voice_id") val voiceId: String? = null,
    @SerialName("aspect_ratio") val aspectRatio: String? = null,
)

// ── HeyGen Avatars ───────────────────────────────────────────────────

/**
 * A HeyGen avatar.
 */
@Serializable
data class Avatar(
    @SerialName("avatar_id") val avatarId: String = "",
    val name: String? = null,
    val gender: String? = null,
    @SerialName("preview_url") val previewUrl: String? = null,
)

/**
 * Response from listing HeyGen avatars.
 */
@Serializable
data class AvatarsResponse(
    val avatars: List<Avatar> = emptyList(),
)

// ── HeyGen Templates ─────────────────────────────────────────────────

/**
 * A HeyGen video template.
 */
@Serializable
data class VideoTemplate(
    @SerialName("template_id") val templateId: String = "",
    val name: String? = null,
    @SerialName("preview_url") val previewUrl: String? = null,
)

/**
 * Response from listing HeyGen video templates.
 */
@Serializable
data class VideoTemplatesResponse(
    val templates: List<VideoTemplate> = emptyList(),
)

// ── HeyGen Voices ────────────────────────────────────────────────────

/**
 * A HeyGen voice.
 */
@Serializable
data class HeyGenVoice(
    @SerialName("voice_id") val voiceId: String = "",
    val name: String? = null,
    val language: String? = null,
    val gender: String? = null,
    val extra: JsonElement? = null,
)

/**
 * Response from listing HeyGen voices.
 */
@Serializable
data class HeyGenVoicesResponse(
    val voices: List<HeyGenVoice> = emptyList(),
)
