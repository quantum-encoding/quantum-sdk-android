package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Vision Request ───────────────────────────────────────────────────

/**
 * Request body for vision analysis endpoints.
 */
@Serializable
data class VisionRequest(
    @SerialName("image_base64") val imageBase64: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val model: String? = null,
    val profile: String? = null,
    val context: VisionContext? = null,
)

/**
 * Domain context for relevance analysis.
 */
@Serializable
data class VisionContext(
    @SerialName("installation_type") val installationType: String? = null,
    val phase: String? = null,
    @SerialName("expected_items") val expectedItems: List<String>? = null,
)

// ── Vision Response ──────────────────────────────────────────────────

/**
 * Full vision analysis response.
 */
@Serializable
data class VisionResponse(
    val caption: String? = null,
    val tags: List<String> = emptyList(),
    val objects: List<DetectedObject> = emptyList(),
    val quality: QualityAssessment? = null,
    val relevance: RelevanceCheck? = null,
    val ocr: OcrResult? = null,
    val model: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)

/**
 * A detected object with bounding box.
 */
@Serializable
data class DetectedObject(
    val label: String = "",
    val confidence: Double = 0.0,
    @SerialName("bounding_box") val boundingBox: List<Int> = emptyList(),
)

/**
 * Image quality assessment.
 */
@Serializable
data class QualityAssessment(
    val overall: String = "",
    val score: Double = 0.0,
    val blur: String = "",
    val darkness: String = "",
    val resolution: String = "",
    val exposure: String = "",
    val issues: List<String> = emptyList(),
)

/**
 * Relevance check against expected content.
 */
@Serializable
data class RelevanceCheck(
    val relevant: Boolean = false,
    val score: Double = 0.0,
    @SerialName("expected_items") val expectedItems: List<String> = emptyList(),
    @SerialName("found_items") val foundItems: List<String> = emptyList(),
    @SerialName("missing_items") val missingItems: List<String> = emptyList(),
    @SerialName("unexpected_items") val unexpectedItems: List<String> = emptyList(),
    val notes: String? = null,
)

/**
 * OCR / text extraction result.
 */
@Serializable
data class OcrResult(
    val text: String? = null,
    val metadata: Map<String, String> = emptyMap(),
    val overlays: List<TextOverlay> = emptyList(),
)

/**
 * A detected text region in the image.
 */
@Serializable
data class TextOverlay(
    val text: String = "",
    @SerialName("bounding_box") val boundingBox: List<Int>? = null,
    @SerialName("type") val overlayType: String? = null,
)
