package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for embedding generation.
 *
 * @property model Model for embedding generation.
 * @property input Input text(s) to embed.
 */
@Serializable
data class EmbedRequest(
    val input: List<String>,
    val model: String? = null,
)

/**
 * Response from embedding generation.
 */
@Serializable
data class EmbedResponse(
    val embeddings: List<List<Double>> = emptyList(),
    val model: String = "",
    @SerialName("request_id") val requestId: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
)
