package dev.cosmicduck.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for image generation.
 *
 * @property model Model for image generation (e.g. "grok-imagine-image", "dall-e-3").
 * @property prompt Text prompt describing the image.
 * @property n Number of images to generate.
 * @property size Image size (e.g. "1024x1024").
 * @property quality Quality level (e.g. "standard", "hd").
 */
@Serializable
data class ImageRequest(
    val model: String,
    val prompt: String,
    val n: Int? = null,
    val size: String? = null,
    val quality: String? = null,
)

/**
 * A generated image result.
 */
@Serializable
data class GeneratedImage(
    val url: String? = null,
    @SerialName("b64_json") val b64Json: String? = null,
)

/**
 * Response from image generation.
 */
@Serializable
data class ImageResponse(
    val images: List<GeneratedImage> = emptyList(),
    val model: String = "",
    @SerialName("request_id") val requestId: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
)

/**
 * Request body for image editing.
 *
 * @property model Model for image editing.
 * @property prompt Text prompt describing the edit.
 * @property image Base64-encoded source image.
 * @property mask Optional mask image for inpainting.
 * @property size Image size.
 * @property n Number of images.
 */
@Serializable
data class ImageEditRequest(
    val model: String,
    val prompt: String,
    val image: String,
    val mask: String? = null,
    val size: String? = null,
    val n: Int? = null,
)

/**
 * Response from image editing.
 */
@Serializable
data class ImageEditResponse(
    val images: List<GeneratedImage> = emptyList(),
    val model: String = "",
    @SerialName("request_id") val requestId: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
)
