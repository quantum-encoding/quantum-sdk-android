package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for embedding generation.
 */
@Serializable
data class EmbedRequest(
    val model: String = "",
    val input: List<String> = emptyList(),
)

/**
 * Response from embedding generation.
 */
@Serializable
data class EmbedResponse(
    val embeddings: List<List<Double>> = emptyList(),
    val model: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)
