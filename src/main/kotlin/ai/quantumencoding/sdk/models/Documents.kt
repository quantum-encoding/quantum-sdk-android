package ai.quantumencoding.sdk.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * Request body for document extraction.
 */
@Serializable
data class DocumentRequest(
    @SerialName("file_base64") val fileBase64: String = "",
    val filename: String = "",
    @SerialName("output_format") val outputFormat: String? = null,
)

/**
 * Response from document extraction.
 */
@Serializable
data class DocumentResponse(
    val content: String = "",
    val format: String = "",
    val meta: Map<String, JsonElement>? = null,
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)

/**
 * Request body for document chunking.
 */
@Serializable
data class ChunkRequest(
    @SerialName("file_base64") val fileBase64: String = "",
    val filename: String = "",
    @SerialName("max_chunk_tokens") val maxChunkTokens: Int? = null,
    @SerialName("overlap_tokens") val overlapTokens: Int? = null,
)

/**
 * A single document chunk.
 */
@Serializable
data class DocumentChunk(
    val index: Int = 0,
    val text: String = "",
    @SerialName("token_count") val tokenCount: Int? = null,
)

/**
 * Response from document chunking.
 */
@Serializable
data class ChunkResponse(
    val chunks: List<DocumentChunk> = emptyList(),
    @SerialName("total_chunks") val totalChunks: Int? = null,
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)

/**
 * Request body for document processing (combined extraction + analysis).
 */
@Serializable
data class ProcessRequest(
    @SerialName("file_base64") val fileBase64: String = "",
    val filename: String = "",
    val prompt: String? = null,
    val model: String? = null,
)

/**
 * Response from document processing.
 */
@Serializable
data class ProcessResponse(
    val content: String = "",
    val model: String? = null,
    @SerialName("cost_ticks") val costTicks: Long = 0,
    @SerialName("request_id") val requestId: String = "",
)

typealias ChunkDocumentRequest = ChunkRequest
typealias ChunkDocumentResponse = ChunkResponse
typealias ProcessDocumentRequest = ProcessRequest
typealias ProcessDocumentResponse = ProcessResponse
