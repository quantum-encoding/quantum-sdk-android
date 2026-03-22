package dev.cosmicduck.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

// ── Video Generation ─────────────────────────────────────────────────

/**
 * Request body for video generation.
 *
 * @property model Model for video generation.
 * @property prompt Text prompt.
 * @property duration Duration in seconds.
 * @property resolution Resolution (e.g. "720p", "1080p").
 */
@Serializable
data class VideoRequest(
    val model: String,
    val prompt: String,
    val duration: Int? = null,
    val resolution: String? = null,
)

/**
 * A generated video result.
 */
@Serializable
data class GeneratedVideo(
    val url: String = "",
    @SerialName("duration_seconds") val durationSeconds: Double = 0.0,
)

/**
 * Response from video generation.
 */
@Serializable
data class VideoResponse(
    val videos: List<GeneratedVideo> = emptyList(),
    val model: String = "",
    @SerialName("request_id") val requestId: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
)

// ── HeyGen Video ─────────────────────────────────────────────────────

/**
 * Request body for HeyGen Studio talking-head video.
 *
 * @property avatarId Avatar ID.
 * @property script Script text.
 * @property voiceId Voice ID.
 * @property clips Clips for multi-scene videos.
 */
@Serializable
data class VideoStudioRequest(
    @SerialName("avatar_id") val avatarId: String,
    val script: String,
    @SerialName("voice_id") val voiceId: String? = null,
    val clips: List<StudioClip>? = null,
)

/**
 * A clip in a multi-scene HeyGen Studio video.
 */
@Serializable
data class StudioClip(
    @SerialName("avatar_id") val avatarId: String,
    val script: String,
    @SerialName("voice_id") val voiceId: String? = null,
    val background: String? = null,
)

/**
 * Request body for HeyGen video translation.
 */
@Serializable
data class VideoTranslateRequest(
    @SerialName("video_url") val videoUrl: String,
    @SerialName("target_lang") val targetLang: String,
    @SerialName("source_lang") val sourceLang: String? = null,
)

/**
 * Request body for HeyGen photo avatar creation.
 */
@Serializable
data class PhotoAvatarRequest(
    val image: String,
)

/**
 * Request body for HeyGen digital twin creation.
 */
@Serializable
data class DigitalTwinRequest(
    val video: String,
)

/**
 * Response for async job submissions (HeyGen, etc.).
 */
@Serializable
data class AsyncJobResponse(
    @SerialName("job_id") val jobId: String = "",
    val status: String = "",
)

/**
 * A HeyGen avatar.
 */
@Serializable
data class HeyGenAvatar(
    @SerialName("avatar_id") val avatarId: String = "",
    @SerialName("avatar_name") val avatarName: String = "",
    @SerialName("preview_url") val previewUrl: String? = null,
)

/**
 * Response from listing HeyGen avatars.
 */
@Serializable
data class AvatarsResponse(
    val avatars: List<HeyGenAvatar> = emptyList(),
)

/**
 * A HeyGen template.
 */
@Serializable
data class HeyGenTemplate(
    @SerialName("template_id") val templateId: String = "",
    val name: String = "",
)

/**
 * Response from listing HeyGen templates.
 */
@Serializable
data class HeyGenTemplatesResponse(
    val templates: List<HeyGenTemplate> = emptyList(),
)

/**
 * A HeyGen voice.
 */
@Serializable
data class HeyGenVoice(
    @SerialName("voice_id") val voiceId: String = "",
    val name: String = "",
    val language: String? = null,
)

/**
 * Response from listing HeyGen voices.
 */
@Serializable
data class HeyGenVoicesResponse(
    val voices: List<HeyGenVoice> = emptyList(),
)
