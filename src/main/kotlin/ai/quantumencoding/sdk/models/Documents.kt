package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for document extraction.
 *
 * @property content Document content (base64 or URL).
 * @property type Content type (e.g. "pdf", "image", "url").
 * @property model Model for extraction.
 */
@Serializable
data class DocumentRequest(
    val content: String,
    val type: String? = null,
    val model: String? = null,
)

/**
 * Response from document extraction.
 */
@Serializable
data class DocumentResponse(
    val text: String = "",
    val pages: Int? = null,
    @SerialName("request_id") val requestId: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
)

/**
 * Request body for document chunking.
 *
 * @property text Text to chunk.
 * @property chunkSize Target chunk size in tokens.
 */
@Serializable
data class ChunkDocumentRequest(
    val text: String,
    @SerialName("chunk_size") val chunkSize: Int? = null,
)

/**
 * A chunk of a document.
 */
@Serializable
data class DocumentChunk(
    val text: String = "",
    val index: Int = 0,
    val tokens: Int = 0,
)

/**
 * Response from document chunking.
 */
@Serializable
data class ChunkDocumentResponse(
    val chunks: List<DocumentChunk> = emptyList(),
    @SerialName("request_id") val requestId: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
)

/**
 * Request body for document processing.
 *
 * @property text Text to process.
 * @property instructions Processing instructions.
 * @property model Model for processing.
 */
@Serializable
data class ProcessDocumentRequest(
    val text: String,
    val instructions: String? = null,
    val model: String? = null,
)

/**
 * Response from document processing.
 */
@Serializable
data class ProcessDocumentResponse(
    val result: String = "",
    @SerialName("request_id") val requestId: String = "",
    @SerialName("cost_ticks") val costTicks: Long = 0,
)
