package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for image generation.
 */
@Serializable
data class ImageRequest(
    val model: String = "",
    val prompt: String = "",
    val count: Int? = null,
    val size: String? = null,
    @SerialName("aspect_ratio") val aspectRatio: String? = null,
    val quality: String? = null,
    @SerialName("output_format") val outputFormat: String? = null,
    val style: String? = null,
    val background: String? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val topology: String? = null,
    @SerialName("target_polycount") val targetPolycount: Int? = null,
    @SerialName("symmetry_mode") val symmetryMode: String? = null,
    @SerialName("pose_mode") val poseMode: String? = null,
    @SerialName("enable_pbr") val enablePbr: Boolean? = null,
)

/**
 * A single generated image.
 */
@Serializable
data class GeneratedImage(
    val base64: String = "",
    val format: String = "",
    val index: Int = 0,
)

/**
 * Response from image generation.
 */
@Serializable
data class ImageResponse(
    val images: List<GeneratedImage> = emptyList(),
    val model: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)

/**
 * Request body for image editing.
 */
@Serializable
data class ImageEditRequest(
    val model: String = "",
    val prompt: String = "",
    @SerialName("input_images") val inputImages: List<String> = emptyList(),
    val count: Int? = null,
    val size: String? = null,
)

/**
 * Response from image editing (same shape as generation).
 */
typealias ImageEditResponse = ImageResponse
