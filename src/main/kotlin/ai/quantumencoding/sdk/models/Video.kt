package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

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

typealias VideoStudioRequest = StudioVideoRequest
typealias VideoTranslateRequest = TranslateRequest

// ── HeyGen typed responses (with request_id) ────────────────────────

/**
 * Response from listing HeyGen avatars (includes request_id).
 */
@Serializable
data class HeyGenAvatarsResponse(
    val avatars: List<JsonElement> = emptyList(),
    @SerialName("request_id") val requestId: String = "",
)

/**
 * Response from listing HeyGen templates (includes request_id).
 */
@Serializable
data class HeyGenTemplatesResponse(
    val templates: List<JsonElement> = emptyList(),
    @SerialName("request_id") val requestId: String = "",
)

// ── HeyGen Template v3 (variable schema + render) ───────────────────

/**
 * A variable slot referenced by a template scene.
 */
@Serializable
data class VideoTemplateSceneVariable(
    /** Variable name (key into the template's `variables` map). */
    val name: String = "",
    /** Variable kind (e.g. "text", "image", "character", "voice"). */
    @SerialName("variable_type") val variableType: String = "",
)

/**
 * A scene in a template, in template order.
 */
@Serializable
data class VideoTemplateScene(
    /** Scene identifier (usable in a generate request's `sceneIds`). */
    @SerialName("scene_id") val sceneId: String = "",
    /** Scene script with placeholders unreplaced (e.g. "Introducing {{headline}}..."). */
    val script: String = "",
    /** Variables referenced by this scene. */
    val variables: List<VideoTemplateSceneVariable> = emptyList(),
)

/**
 * Detailed template info: variable schema + scenes.
 *
 * Each `variables[name]` value is a discriminated union on its `"type"` field
 * ("text" | "image" | "video" | "audio" | "voice" | "character"; unknown
 * future types round-trip verbatim), returned in the exact shape a generate
 * request accepts — replace defaults and submit back.
 */
@Serializable
data class VideoTemplateDetail(
    /** Template identifier. */
    val id: String = "",
    /** Template name. */
    val name: String = "",
    /** Aspect ratio (e.g. "16:9"). */
    @SerialName("aspect_ratio") val aspectRatio: String = "",
    /**
     * Variable schema keyed by variable name (union values kept as raw JSON
     * so unknown future variable types round-trip verbatim).
     */
    val variables: Map<String, JsonElement> = emptyMap(),
    /** Scenes in template order. */
    val scenes: List<VideoTemplateScene> = emptyList(),
)

/**
 * Response from inspecting a template's variable schema (unbilled).
 */
@Serializable
data class VideoTemplateDetailResponse(
    /** The template detail. */
    val template: VideoTemplateDetail = VideoTemplateDetail(),
    /** Unique request identifier. */
    @SerialName("request_id") val requestId: String = "",
)

/**
 * Output dimension for a template render. Both values must be even,
 * each 128–4096, and keep the template aspect ratio.
 */
@Serializable
data class VideoTemplateDimension(
    val width: Int,
    val height: Int,
)

/**
 * Subtitle position for burned-in captions.
 */
@Serializable
data class VideoSubtitlePosition(
    val x: Double,
    val y: Double,
)

/**
 * Subtitle options for a template render (implies captions).
 */
@Serializable
data class VideoTemplateSubtitles(
    /** Subtitle preset (e.g. "classic", "bold", "bright"). Required. */
    @SerialName("preset_name") val presetName: String,
    /** Alignment (default 2). */
    val alignment: Int? = null,
    /** Disable word highlighting. */
    @SerialName("disable_highlight") val disableHighlight: Boolean? = null,
    /** Font size. */
    @SerialName("font_size") val fontSize: Int? = null,
    /** Subtitle position. */
    val position: VideoSubtitlePosition? = null,
)

/**
 * Request body for rendering a video from a template (async job type
 * "video/template-v3").
 */
@Serializable
data class VideoTemplateGenerateRequest(
    /**
     * Variable overrides keyed by name (at least one required). Values use
     * the same union shapes returned by the template detail route; omitted
     * variables keep the template defaults.
     */
    val variables: Map<String, JsonElement>,
    /** Names the generated video. */
    val title: String? = null,
    /** Restrict the render to these scenes, in order (repeats allowed). */
    @SerialName("scene_ids") val sceneIds: List<String>? = null,
    /** Output dimension (must keep the template aspect ratio). */
    val dimension: VideoTemplateDimension? = null,
    /** Frames per second: 25 (default), 30, or 60. */
    val fps: Int? = null,
    /** Burn captions (default false). */
    val caption: Boolean? = null,
    /** Subtitle options (implies captions). */
    val subtitles: VideoTemplateSubtitles? = null,
    /** Background audio moves with scenes (default true). */
    @SerialName("reorder_music") val reorderMusic: Boolean? = null,
    /** Keep text vertically centered (default false). */
    @SerialName("keep_text_vertically_centered") val keepTextVerticallyCentered: Boolean? = null,
    /** Include a GIF preview in the webhook payload. */
    @SerialName("include_gif") val includeGif: Boolean? = null,
    /** Enable a public share page. */
    @SerialName("enable_sharing") val enableSharing: Boolean? = null,
    /** HeyGen folder id. */
    @SerialName("folder_id") val folderId: String? = null,
    /** Brand voice id. */
    @SerialName("brand_voice_id") val brandVoiceId: String? = null,
)

// ── HeyGen Batch Videos ─────────────────────────────────────────────

/**
 * Request body for submitting a batch of videos.
 */
@Serializable
data class VideoBatchSubmitRequest(
    /**
     * 1–100 raw HeyGen `POST /v3/videos` request bodies, passed through
     * verbatim. Each is polymorphic, discriminated by its `"type"` field
     * ("avatar" | "image" | "cinematic_avatar"), so items are kept as
     * opaque JSON objects.
     */
    val videos: List<JsonObject>,
    /** Display name for the batch in the HeyGen app. */
    val title: String? = null,
)

/**
 * Response from submitting a video batch (202 Accepted).
 */
@Serializable
data class VideoBatchSubmitResponse(
    /** Batch id — poll `videoBatchStatus` with it. */
    @SerialName("batch_id") val batchId: String = "",
    /** Always "processing" at submit. */
    val status: String = "",
    /** Count of submitted items. */
    @SerialName("total_items") val totalItems: Int = 0,
    /** Unique request identifier. */
    @SerialName("request_id") val requestId: String = "",
)

/**
 * Query parameters for the batch status page.
 */
data class VideoBatchStatusQuery(
    /** Page size (1–100; upstream default 100). */
    val limit: Int? = null,
    /** Opaque cursor from a previous response's `nextToken`. */
    val token: String? = null,
)

/**
 * Per-item error detail in a batch status page.
 */
@Serializable
data class VideoBatchItemError(
    val code: String = "",
    val message: String = "",
)

/**
 * One item of a batch status page, ordered by `itemIndex`.
 */
@Serializable
data class VideoBatchItem(
    /** Zero-based position in the submitted `videos` array. */
    @SerialName("item_index") val itemIndex: Int = 0,
    /** "queued" | "processing" | "completed" | "failed". */
    val status: String = "",
    /** Present once the item's video exists. */
    @SerialName("video_id") val videoId: String? = null,
    /** Present only when `billingStatus == "settled"` and the item completed. */
    @SerialName("video_url") val videoUrl: String? = null,
    /** Present only when the item failed. */
    val error: VideoBatchItemError? = null,
)

/**
 * Response from a batch status check (one cursor-paginated page of items).
 *
 * Billing settles the first time a GET observes a terminal batch status;
 * `videoUrl` values are withheld until `billingStatus == "settled"` —
 * keep polling until then to obtain URLs.
 */
@Serializable
data class VideoBatchStatusResponse(
    /** Batch id. */
    @SerialName("batch_id") val batchId: String = "",
    /** Batch display name (may be empty). */
    val title: String = "",
    /** Batch-level status: "processing" | "completed" | "failed". */
    val status: String = "",
    /** Count of submitted items. */
    @SerialName("total_items") val totalItems: Int = 0,
    /** Per-item-status counts across the whole batch. */
    @SerialName("counts_by_status") val countsByStatus: Map<String, Int> = emptyMap(),
    /** Batch creation time in unix seconds (upstream HeyGen timestamp). */
    @SerialName("created_at") val createdAt: Long = 0,
    /** One page of items, ordered by `itemIndex`. */
    val items: List<VideoBatchItem> = emptyList(),
    /** More item pages exist. */
    @SerialName("has_more") val hasMore: Boolean = false,
    /** Pass as `token` for the next page (may be empty). */
    @SerialName("next_token") val nextToken: String = "",
    /** "unsettled" | "settlement_pending" | "settled". */
    @SerialName("billing_status") val billingStatus: String = "",
    /** Total ticks charged for the batch; 0 until settled. */
    @SerialName("cost_ticks") val costTicks: Long = 0,
    /** Unique request identifier. */
    @SerialName("request_id") val requestId: String = "",
)
